package se.arctosoft.tvchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.ArrayList;
import java.util.List;

import se.arctosoft.tvchat.adapters.ChannelAdapter;
import se.arctosoft.tvchat.data.Channel;
import se.arctosoft.tvchat.utils.MyHttpUtils;

public class ChannelActivity extends AppCompatActivity {
    private static final String TAG = "ChannelActivity";

    private RecyclerView rvChannels;
    private List<Channel> mChannels;
    private ChannelAdapter mAdapter;
    private LinearProgressIndicator progressBar;
    private Button btnProfile, btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        init();
    }

    private void init() {
        rvChannels = findViewById(R.id.rvChannels);
        progressBar = findViewById(R.id.progressBar);
        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setEnabled(false);
        btnAbout = findViewById(R.id.btnAbout);
        mChannels = new ArrayList<>();

        mAdapter = new ChannelAdapter(ChannelActivity.this, mChannels);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChannelActivity.this);
        rvChannels.setAdapter(mAdapter);
        rvChannels.setLayoutManager(linearLayoutManager);

        btnProfile.setOnClickListener(v -> startActivity(new Intent(ChannelActivity.this, ProfileActivity.class)));

        if (ParseUser.getCurrentUser() != null) { // start with existing user
            btnProfile.setEnabled(true);
            ParseUser.getCurrentUser().fetchInBackground((object, e) -> loadChannels());
        } else { // If not logged in, login as a new anonymous user
            login();
        }
    }

    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    private void login() {
        ParseAnonymousUtils.logIn((user, e) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (e != null) {
                Log.e(TAG, "Anonymous login failed: ", e);
                new Handler().postDelayed(this::login, 2000);
            } else {
                btnProfile.setEnabled(true);
                loadChannels();
            }
        });
    }

    private void loadChannels() {
        ParseQuery<Channel> query = ParseQuery.getQuery(Channel.class);
        // get the latest 50 messages, order will show up newest to oldest of this group
        query.orderByAscending("o");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground((channels, e) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (e == null) {
                progressBar.setVisibility(View.GONE);
                for (ParseObject o : channels) {
                    Log.e(TAG, "loadChannels: got " + o.keySet());
                }
                mChannels.clear();
                mChannels.addAll(channels);
                mAdapter.notifyDataSetChanged();
            } else {
                new Handler().postDelayed(this::loadChannels, 2000);
                Log.e("channel", "Error Loading Channels " + e);
            }
        });
        //getChannelSchedule();
    }

    private void getChannelSchedule() {
        new Thread(() -> {
            // https://my.iptv.community/epg_temp_dl/output-epgs/sweden.xml
            String data = MyHttpUtils.getDataHttpUriConnection("https://my.iptv.community/epg_temp_dl/output-epgs/sweden.xml");
            try {
                XmlPullParser xml = XmlPullParserFactory.newInstance().newPullParser();
                Log.e(TAG, "getChannelSchedule: got " + data);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }).start();
    }
}