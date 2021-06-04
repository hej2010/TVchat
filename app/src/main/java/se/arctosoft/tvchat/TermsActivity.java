package se.arctosoft.tvchat;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {
    public static final String EXTRA_RESOURCE_ID = "r";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        Bundle extras = getIntent().getExtras();
        int id = -1;
        if (extras != null) {
            id = extras.getInt(EXTRA_RESOURCE_ID, -1);
        }
        if (id == -1) {
            finish();
            return;
        }

        init(id);
    }

    private void init(int id) {
        TextView txtContent = findViewById(R.id.txtContent);
        txtContent.setText(getString(id));
    }
}