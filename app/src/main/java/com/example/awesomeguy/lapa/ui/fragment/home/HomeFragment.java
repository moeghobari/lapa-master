package com.example.awesomeguy.lapa.ui.fragment.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.awesomeguy.lapa.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_CANCELED;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    Context context;
    EditText name, email, mobile;
    ImageView imageView;

    private String imageID = "";
    private DatabaseReference mDatabase;

    private int GALLERY = 1, CAMERA = 2;
    private static final String IMAGE_DIRECTORY = "/LocationAPP";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("profile");

        name = root.findViewById(R.id.input_name);
        email = root.findViewById(R.id.input_email);
        mobile = root.findViewById(R.id.input_mobile);
        imageView = root.findViewById(R.id.profile_image);

        imageView.setOnClickListener(v -> showPictureDialog());

        Button updateBtn = root.findViewById(R.id.updateProfileBtn);
        updateBtn.setOnClickListener(v -> updateProfile());

        loadUserProfile();


//        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(this, s -> textView.setText(s));
        return root;
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {"Select photo from gallery", "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems, (dialog, which) -> {
            switch (which) {
                case 0:
                    choosePhotoFromGallery();
                    break;
                case 1:
                    takePhotoFromCamera();
                    break;
            }
        });
        pictureDialog.show();
    }

    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }

        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
//                    // String path = saveImage(bitmap);
//                    // Toast.makeText(ProfileActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
//                    imageView.setImageBitmap(bitmap);
                    Bitmap bm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(contentURI));
                    imageView.setImageBitmap(bm);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(thumbnail);
            saveImage(thumbnail);
            Toast.makeText(getActivity(), "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getActivity(), new String[]{f.getPath()}, new String[]{"image/jpeg"}, null);
            fo.close();

            f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void loadUserProfile() {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference profile = mDatabase.child(user_id);

        profile.addValueEventListener(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                if (map != null) {
                    try {
                        if (map.get("name") != null) {
                            name.setText(map.get("name").toString());
                        }
                        if (map.get("email") != null) {
                            email.setText(map.get("email").toString());
                        }
                        if (map.get("mobile") != null) {
                            mobile.setText(map.get("mobile").toString());
                        }
                        if (map.get("image") != null) {
                            imageID = map.get("image").toString();
                        }
                        if (map.get("imagePath") != null) {
                            String imagePath = map.get("imagePath").toString();
                            Glide.with(context).load(imagePath).into(imageView);
                        }
                    } catch (Exception e) {
                        System.out.println("something went horribly wrong...");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }


    private void updateProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if (imageView.getDrawable() != null) {
            try {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                String img_id = !imageID.equals("") ? imageID : UUID.randomUUID().toString();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + img_id + ".jpg");
                UploadTask uploadTask = ref.putBytes(data);

                uploadTask.addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return ref.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                        uploadData(Objects.requireNonNull(downloadUri));
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed ", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                uploadData(Uri.EMPTY);
                progressDialog.dismiss();
            }
        } else {
            uploadData(Uri.EMPTY);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void uploadData(Uri imageURL) {
        String _email = email.getText().toString();
        String _name = name.getText().toString();
        String _mobile = mobile.getText().toString();

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference current_user_db = mDatabase.child(user_id);

        current_user_db.child("name").setValue(_name);
        current_user_db.child("email").setValue(_email);
        current_user_db.child("mobile").setValue(_mobile);

        if (imageURL != null && !imageURL.toString().equals("")) {
            current_user_db.child("imagePath").setValue(imageURL.toString());
        } else {
            current_user_db.child("image").setValue(UUID.randomUUID().toString());
        }

        Toast.makeText(getActivity(), "Update Successful", Toast.LENGTH_SHORT).show();
        // setResult(RESULT_OK, null);
        // finish();
    }


}