package se.arctosoft.tvchat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

import se.arctosoft.tvchat.utils.Dialogs;
import se.arctosoft.tvchat.utils.Toaster;

public class AboutActivity extends AppCompatActivity {
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar aB = getSupportActionBar();
        if (aB != null) {
            aB.setDisplayHomeAsUpEnabled(true);
            aB.setTitle(getString(R.string.about));
        }

        init();
    }

    private void init() {
        settings = new Settings(this);
        boolean isAdmin = settings.isAdmin();
        ImageView imgIcon = findViewById(R.id.imgIcon);

        imgIcon.setOnClickListener(new View.OnClickListener() {
            int clicked = 0;

            @Override
            public void onClick(View v) {
                if (isAdmin) {
                    Toaster.getInstance(AboutActivity.this).showShort("Admin");
                    return;
                }
                clicked++;
                if (clicked > 20) {
                    settings.setIsAdmin(false);
                    Dialogs.showLoginDialog(AboutActivity.this, pwd -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("p", pwd);
                        clicked = 0;
                        ParseCloud.callFunctionInBackground("login", map, (object, e) -> {
                            if (e != null) {
                                e.printStackTrace();
                                Toaster.getInstance(AboutActivity.this).showShort(e.getMessage() != null ? e.getMessage() : "Error");
                            } else {
                                settings.setIsAdmin(true);
                                Toaster.getInstance(AboutActivity.this).showShort("Admin");
                            }
                        });
                    });
                }
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
}