package se.arctosoft.tvchat;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.parse.ParseUser;

import se.arctosoft.tvchat.utils.Toaster;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private LinearProgressIndicator progressBar;
    private Button btnSave;
    private EditText etUsername;
    private TextView txtUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar aB = getSupportActionBar();
        if (aB != null) {
            aB.setDisplayHomeAsUpEnabled(true);
            aB.setTitle(getString(R.string.my_profile));
        }

        init(user);
    }

    private void init(ParseUser user) {
        progressBar = findViewById(R.id.progressBar);
        btnSave = findViewById(R.id.btnSave);
        etUsername = findViewById(R.id.etUsername);
        txtUserId = findViewById(R.id.txtUserId);

        etUsername.setText(user.getUsername());
        txtUserId.setText(getString(R.string.profile_user_id, user.getObjectId()));
        updateUser(user);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        etUsername.setEnabled(!loading);
        btnSave.setEnabled(!loading);
    }

    private void saveChanges() {
        setLoading(true);
        final String username = etUsername.getText().toString().trim();
        int l = username.length();
        if (l < 3 || l > 25) {
            setLoading(false);
            return;
        }
        ParseUser user = ParseUser.getCurrentUser();
        //user.put("i", new ParseFile());
        user.setUsername(username);
        user.saveInBackground(e -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (e != null) {
                e.printStackTrace();
                btnSave.setEnabled(true);
                setLoading(false);
                String message = e.getMessage();
                if (message != null && message.startsWith("Account already exists")) {
                    Toaster.getInstance(this).showShort(getString(R.string.profile_save_already_taken));
                } else {
                    Toaster.getInstance(this).showShort(getString(R.string.fel, message));
                }
            } else {
                btnSave.setEnabled(true);
                setLoading(false);
                Toaster.getInstance(this).showShort(getString(R.string.profile_saved));
                finish();
            }
        });
    }

    private void updateUser(ParseUser user) {
        user.fetchInBackground((object, e) -> {
            if (e != null) {
                e.printStackTrace();
                new Handler().postDelayed(() -> updateUser(user), 1000);
            } else {
                btnSave.setOnClickListener(v -> {
                    btnSave.setEnabled(false);
                    saveChanges();
                });
                setLoading(false);
                etUsername.setText(((ParseUser) object).getUsername());
                txtUserId.setText(getString(R.string.profile_user_id, object.getObjectId()));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_chat, menu);

        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}