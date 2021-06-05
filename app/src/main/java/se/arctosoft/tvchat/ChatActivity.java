package se.arctosoft.tvchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.ParseACL;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.LiveQueryException;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.ParseLiveQueryClientCallbacks;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import se.arctosoft.tvchat.adapters.ChatAdapter;
import se.arctosoft.tvchat.data.Channel;
import se.arctosoft.tvchat.data.Message;
import se.arctosoft.tvchat.utils.Settings;
import se.arctosoft.tvchat.utils.Toaster;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    public static final String EXTRA_CHANNEL = "c";

    private EditText etMessage;
    private RecyclerView rvChat;
    private List<Message> mMessages;
    private ChatAdapter mAdapter;
    // Keep track of initial load to scroll to the bottom of the ListView
    private boolean mFirstLoad;
    private ParseLiveQueryClient parseLiveQueryClient = null;
    private final ParseQuery<Message> parseQuery = ParseQuery.getQuery(Message.class);
    private Channel channel;
    private final float[] lastTouchDownXY = new float[2];
    private final AtomicBoolean isCreating = new AtomicBoolean(false);
    private Settings settings;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState != null) {
            channel = savedInstanceState.getParcelable(EXTRA_CHANNEL);
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                Object c = extras.get(EXTRA_CHANNEL);
                if (!(c instanceof Channel)) {
                    finish();
                    return;
                }
                channel = (Channel) c;
            }
        }
        if (channel == null) {
            finish();
            return;
        }

        settings = new Settings(this);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar aB = getSupportActionBar();
        if (aB != null) {
            aB.setDisplayHomeAsUpEnabled(true);
            aB.setTitle(channel.getName());
        }


        if (ParseUser.getCurrentUser() != null) { // start with existing user
            setupMessagePosting();
        } else { // If not logged in, login as a new anonymous user
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.reports) {
            startActivity(new Intent(this, ReportsActivity.class));
        } else if (id == R.id.help) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(settings.isAdmin() ? R.menu.menu_chat_admin : R.menu.menu_chat, menu);

        return true;
    }

    // Setup button event handler which posts the entered message to Parse
    void setupMessagePosting() {
        // Find the text field and button
        etMessage = findViewById(R.id.etMessage);
        ImageView btSend = findViewById(R.id.btSend);
        rvChat = findViewById(R.id.rvChat);
        mMessages = new LinkedList<>();
        mFirstLoad = true;
        mAdapter = new ChatAdapter(ChatActivity.this, ParseUser.getCurrentUser().getObjectId(), mMessages, lastTouchDownXY);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setReverseLayout(true);
        rvChat.setAdapter(mAdapter);
        rvChat.setLayoutManager(linearLayoutManager);
        setupLiveQueries();
        // When send button is clicked, create message object on Parse
        btSend.setOnClickListener(v -> {
            createMessage();
        });
    }

    private void createMessage() {
        if (isCreating.compareAndSet(false, true)) {
            if (parseLiveQueryClient == null) {
                setupLiveQueries();
            }
            final String data = etMessage.getText().toString();
            if (data.trim().isEmpty()) {
                isCreating.set(false);
                return;
            }
            ParseACL acl = new ParseACL();
            acl.setPublicReadAccess(true);
            acl.setPublicWriteAccess(false);
            Message message = new Message();
            message.setACL(acl);
            message.setBody(data);
            ParseUser user = ParseUser.getCurrentUser();
            message.setUserName(user.getUsername());
            message.setUserId(user.getObjectId());
            message.setChannel(channel);
            message.saveInBackground(e -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                isCreating.set(false);
                if (e == null) {
                    etMessage.setText(null);
                    mMessages.add(0, message);
                    mAdapter.notifyItemInserted(0);
                    rvChat.scrollToPosition(0);
                    //Toast.makeText(ChatActivity.this, "Successfully created message on Parse", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to save message", e);
                    Toaster.getInstance(this).showShort(e.getMessage() != null ? e.getMessage() : "Failed to send message");
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        lastTouchDownXY[0] = event.getX();
        lastTouchDownXY[1] = event.getY();
        return super.dispatchTouchEvent(event);
    }

    private void setupLiveQueries() {
        // Load existing messages to begin with
        refreshMessages();

        // Make sure the Parse server is setup to configured for live queries
        // URL for server is determined by Parse.initialize() call.
        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("https://api.arctosoft.com/parse3"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        parseLiveQueryClient.registerListener(new ParseLiveQueryClientCallbacks() {
            @Override
            public void onLiveQueryClientConnected(ParseLiveQueryClient client) {
                Log.e(TAG, "onLiveQueryClientConnected: " + client);
            }

            @Override
            public void onLiveQueryClientDisconnected(ParseLiveQueryClient client, boolean userInitiated) {
                Log.e(TAG, "onLiveQueryClientDisconnected: " + userInitiated + ", " + client);
            }

            @Override
            public void onLiveQueryError(ParseLiveQueryClient client, LiveQueryException reason) {
                Log.e(TAG, "onLiveQueryError: " + reason);
            }

            @Override
            public void onSocketError(ParseLiveQueryClient client, Throwable reason) {
                Log.e(TAG, "onSocketError: " + reason);
                unsubscribe(client, parseQuery);
                ChatActivity.this.parseLiveQueryClient = null;
            }
        });


        // This query can even be more granular (i.e. only refresh if the entry was added by some other user)
        parseQuery.whereNotEqualTo(Message.USER_ID_KEY, ParseUser.getCurrentUser().getObjectId());
        parseQuery.whereEqualTo(Message.CHANNEL_KEY, channel);

        // Connect to Parse server
        SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

        // Listen for CREATE events
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (query, object) -> {
            Log.e(TAG, "setupLiveQueries: added " + object);
            mMessages.add(0, object);

            // RecyclerView updates need to be run on the UI thread
            runOnUiThread(() -> {
                mAdapter.notifyItemInserted(0);
                rvChat.scrollToPosition(0);
            });
        });
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.DELETE, (query, object) -> {
            Log.e(TAG, "setupLiveQueries: deleted " + object);
            // RecyclerView updates need to be run on the UI thread
            runOnUiThread(() -> {
                int index = mMessages.indexOf(object);
                if (index >= 0) {
                    mMessages.remove(object);
                    mAdapter.notifyItemRemoved(index);
                }
            });
        });
    }

    void refreshMessages() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
        query.whereEqualTo(Channel.NAME_KEY, channel);
        // get the latest 50 messages, order will show up newest to oldest of this group
        query.orderByDescending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground((messages, e) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (e == null) {
                mMessages.clear();
                mMessages.addAll(messages);
                mAdapter.notifyDataSetChanged(); // update adapter
                // Scroll to the bottom of the list on initial load
                if (mFirstLoad) {
                    rvChat.scrollToPosition(0);
                    mFirstLoad = false;
                }
            } else {
                Log.e("message", "Error Loading Messages" + e);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_CHANNEL, channel);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: " + parseLiveQueryClient);
        if (parseLiveQueryClient != null) {
            //parseLiveQueryClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " + parseLiveQueryClient);
        if (parseLiveQueryClient != null) {
            parseLiveQueryClient.connectIfNeeded();
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: " + parseLiveQueryClient);
        unsubscribe(parseLiveQueryClient, parseQuery);
        super.onDestroy();
    }

    private void unsubscribe(ParseLiveQueryClient parseLiveQueryClient, ParseQuery<Message> parseQuery) {
        if (parseLiveQueryClient != null) {
            parseLiveQueryClient.unsubscribe(parseQuery);
        }
    }
}