package com.example.chat.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtUserName;
    private ImageView imgAvatar;
    private Button btnChoose, btnRegister;
    private Uri imageUri;

    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(this).load(uri).into(imgAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtUserName = findViewById(R.id.edtUserName);
        imgAvatar = findViewById(R.id.imgAvatarPreview);
        btnChoose = findViewById(R.id.btnChooseAvatar);
        btnRegister = findViewById(R.id.btnRegister);

        btnChoose.setOnClickListener(v -> picker.launch("image/*"));

        btnRegister.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass  = edtPassword.getText().toString().trim();
            String name  = edtUserName.getText().toString().trim();
            if (email.isEmpty()||pass.isEmpty()||name.isEmpty()){
                Toast.makeText(this,"Điền đủ thông tin",Toast.LENGTH_SHORT).show(); return;
            }
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(res -> {
                        String uid = res.getUser().getUid();
                        if (imageUri != null) {
                            FirebaseStorage.getInstance().getReference("avatars/"+uid+".jpg")
                                    .putFile(imageUri)
                                    .addOnSuccessListener(t -> FirebaseStorage.getInstance()
                                            .getReference("avatars/"+uid+".jpg")
                                            .getDownloadUrl().addOnSuccessListener(url -> {
                                                saveUser(uid, email, name, url.toString());
                                            }))
                                    .addOnFailureListener(e-> saveUser(uid, email, name, null));
                        } else {
                            saveUser(uid, email, name, null);
                        }
                    })
                    .addOnFailureListener(e-> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void saveUser(String uid, String email, String name, String avatarUrl) {
        User u = new User(uid, email, name, avatarUrl);
        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .set(u)
                .addOnSuccessListener(a->{
                    startActivity(new Intent(this, MainActivity.class)); finish();
                })
                .addOnFailureListener(e-> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

