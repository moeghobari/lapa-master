package com.example.awesomeguy.lapa.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.awesomeguy.lapa.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.UUID;

public class FeedbackActivity extends AppCompatActivity {

    EditText subject, desc;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("feedback");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar = findViewById(R.id.feedback_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_32dp);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> goBack());

        subject = findViewById(R.id.feedback_subject);
        desc = findViewById(R.id.feedback_desc);

        Button updateBtn = findViewById(R.id.submitFeedbackBtn);
        updateBtn.setOnClickListener(v -> submitFeedback());

    }


    private void submitFeedback() {
        String _subject = subject.getText().toString();
        String _desc = desc.getText().toString();

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference current_user_db = mDatabase.push();

        current_user_db.child("subject").setValue(_subject);
        current_user_db.child("description").setValue(_desc);
        current_user_db.child("user").setValue(user_id);

        Toast.makeText(this, "Feedback Sent Successfully...", Toast.LENGTH_SHORT).show();
        subject.setText("");
        desc.setText("");
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void goBack() {
        Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
