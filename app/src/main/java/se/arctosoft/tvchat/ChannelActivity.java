package se.arctosoft.tvchat;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import se.arctosoft.tvchat.adapters.ChannelAdapter;
import se.arctosoft.tvchat.data.Channel;

public class ChannelActivity extends AppCompatActivity {
    private static final String TAG = "ChannelActivity";

    private RecyclerView rvChannels;
    private List<Channel> mChannels;
    private ChannelAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        init();
    }

    private void init() {
        rvChannels = findViewById(R.id.rvChannels);
        mChannels = new ArrayList<>();

        mAdapter = new ChannelAdapter(ChannelActivity.this, mChannels);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChannelActivity.this);
        rvChannels.setAdapter(mAdapter);
        rvChannels.setLayoutManager(linearLayoutManager);

        if (ParseUser.getCurrentUser() != null) { // start with existing user
            loadChannels();
        } else { // If not logged in, login as a new anonymous user
            login();
        }
    }

    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    private void login() {
        ParseAnonymousUtils.logIn((user, e) -> {
            if (e != null) {
                Log.e(TAG, "Anonymous login failed: ", e);
            } else {
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
            if (e == null) {
                for (ParseObject o : channels) {
                    Log.e(TAG, "loadChannels: got " + o.keySet());
                }
                mChannels.clear();
                mChannels.addAll(channels);
                mAdapter.notifyDataSetChanged();
            } else {
                Log.e("channel", "Error Loading Channels " + e);
            }
        });
        //getChannelSchedule();
    }

    private void getChannelSchedule() {
        new Thread(() -> {
            // https://my.iptv.community/epg_temp_dl/output-epgs/sweden.xml
            //
        }).start();
    }
}