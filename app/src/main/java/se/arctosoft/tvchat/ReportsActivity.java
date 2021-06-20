package se.arctosoft.tvchat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import se.arctosoft.tvchat.adapters.ReportsAdapter;
import se.arctosoft.tvchat.data.Report;

public class ReportsActivity extends AppCompatActivity {
    private static final String TAG = "ReportsActivity";

    private RecyclerView recyclerView;
    private List<Report> mReports;
    private ReportsAdapter mAdapter;

    private long lastLoadChannels;
    private final float[] lastTouchDownXY = new float[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar aB = getSupportActionBar();
        if (aB != null) {
            aB.setDisplayHomeAsUpEnabled(true);
            aB.setTitle(getString(R.string.channel_menu_reports));
        }

        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.refresh) {
            loadReports();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_reports, menu);

        return true;
    }

    private void init() {
        recyclerView = findViewById(R.id.recycler_view);
        mReports = new ArrayList<>();

        mAdapter = new ReportsAdapter(this, mReports, lastTouchDownXY);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        lastTouchDownXY[0] = event.getX();
        lastTouchDownXY[1] = event.getY();
        return super.dispatchTouchEvent(event);
    }

    private void loadReports() {
        if ((System.currentTimeMillis() - lastLoadChannels) / 1000 < 5) {
            return;
        }
        lastLoadChannels = System.currentTimeMillis();
        ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
        query.orderByAscending("createdAt");
        query.include("m").include("c");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground((reports, e) -> {
            Log.e(TAG, "loadReports: got " + (reports == null ? null : reports.size()));
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (e == null) {
                Log.e(TAG, "loadReports: got " + reports);

                mReports.clear();
                mReports.addAll(reports);
                mAdapter.notifyDataSetChanged();
            } else {
                e.printStackTrace();
                new Handler().postDelayed(this::loadReports, 2000);
                Log.e("report", "Error Loading Reports " + e);
                if (e.getMessage() != null && e.getMessage().startsWith("Invalid session")) {
                    ParseUser.logOut();
                    ParseAnonymousUtils.logInInBackground();
                }
            }
        });
        //getChannelSchedule();
    }

    @Override
    protected void onResume() {
        if (ParseUser.getCurrentUser() != null) {
            loadReports();
        }
        super.onResume();
    }
}