package se.arctosoft.tvchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import se.arctosoft.tvchat.utils.Settings;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        init();
    }

    private void init() {
        Settings settings = new Settings(this);
        if (settings.acceptedTerms()) {
            goToMain();
            return;
        }
        Button btnShowPrivacyPolicy = findViewById(R.id.btnShowPrivacyPolicy);
        Button btnShowTerms = findViewById(R.id.btnShowTerms);
        Button btnShowRules = findViewById(R.id.btnShowRules);
        Button btnSave = findViewById(R.id.btnSave);

        btnShowPrivacyPolicy.setOnClickListener(v -> startActivity(new Intent(LauncherActivity.this, TermsActivity.class).putExtra(TermsActivity.EXTRA_RESOURCE_ID, R.string.privacy_policy)));
        btnShowTerms.setOnClickListener(v -> startActivity(new Intent(LauncherActivity.this, TermsActivity.class).putExtra(TermsActivity.EXTRA_RESOURCE_ID, R.string.terms_content)));
        btnShowRules.setOnClickListener(v -> startActivity(new Intent(LauncherActivity.this, TermsActivity.class).putExtra(TermsActivity.EXTRA_RESOURCE_ID, R.string.about_rules_content)));
        btnSave.setOnClickListener(v -> {
            settings.setAcceptedTerms(true);
            goToMain();
        });
    }

    private void goToMain() {
        finish();
        startActivity(new Intent(LauncherActivity.this, ChannelActivity.class));
    }
}