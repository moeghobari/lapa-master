package com.example.awesomeguy.lapa.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awesomeguy.lapa.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText nameEditText;
    private EditText passwordEditText;
    private AutoCompleteTextView usernameEditText;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    String TAG = "Firebase Auth";
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("profile");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        nameEditText = findViewById(R.id.name);
        passwordEditText = findViewById(R.id.password);
        usernameEditText = findViewById(R.id.username);

        final Button loginButton = findViewById(R.id.signup);
        final Button loginGoogleButton = findViewById(R.id.signup_google);
        final TextView gotoSignIn = findViewById(R.id.go_to_signin);

        mProgressDialog = new ProgressDialog(this);

        loginButton.setOnClickListener(v -> {
            showProgressIndicator("Loading...");
            SignUpViaEmailPass(nameEditText.getText().toString(), usernameEditText.getText().toString(), passwordEditText.getText().toString());
        });

        loginGoogleButton.setOnClickListener(v -> {
            showProgressIndicator("Loading...");
            SignUpViaGoogle();
        });

        gotoSignIn.setOnClickListener(v -> {
            showProgressIndicator("Loading...");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void SignUpViaEmailPass(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            String TAG = "Firebase Auth";
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
                saveProfile(user, name, email, "");
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    private void SignUpViaGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(Objects.requireNonNull(account));
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);

                String photoUrl = null;
                if ((user != null ? user.getPhotoUrl() : null) != null)
                    photoUrl = user.getPhotoUrl().toString();

                saveProfile(user, Objects.requireNonNull(user).getDisplayName(), user.getEmail(), photoUrl);
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                updateUI(null);
            }
        });
    }

    private void updateUI(FirebaseUser currentUser) {
        mProgressDialog.dismiss();
        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void saveProfile(FirebaseUser currentUser, String name, String email, String photoUrl) {
        if (currentUser != null) {
            String user_id = currentUser.getUid();
            DatabaseReference current_user_db = mDatabase.child(user_id);

            current_user_db.child("name").setValue(name);
            current_user_db.child("email").setValue(email);
            current_user_db.child("mobile").setValue("");

            if (!photoUrl.isEmpty()) current_user_db.child("imagePath").setValue(photoUrl);
        }

        nameEditText.setText("");
        passwordEditText.setText("");
        usernameEditText.setText("");
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }


    public void showProgressIndicator(final String message) {
        mProgressDialog.dismiss();
        mProgressDialog.setTitle(getString(R.string.signing_up));
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

}
