package com.example.chat.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgAvatar; private EditText edtName; private Button btnChoose, btnSave;
    private Uri imageUri; private String uid;

    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) { imageUri = uri; Glide.with(this).load(uri).into(imgAvatar); }
            });

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_profile);
        imgAvatar=findViewById(R.id.imgAvatarPreview);
        edtName = findViewById(R.id.edtUserName);
        btnChoose=findViewById(R.id.btnChooseAvatar);
        btnSave  = findViewById(R.id.btnSaveProfile);
        uid = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .get().addOnSuccessListener(d->{
                    edtName.setText(d.getString("userName"));
                    Glide.with(this).load(d.getString("avatarUrl"))
                            .placeholder(R.drawable.ic_person).into(imgAvatar);
                });

        btnChoose.setOnClickListener(v-> picker.launch("image/*"));

        btnSave.setOnClickListener(v-> {
            String name = edtName.getText().toString().trim();
            if (imageUri!=null){
                FirebaseStorage.getInstance().getReference("avatars/"+uid+".jpg")
                        .putFile(imageUri)
                        .addOnSuccessListener(t-> FirebaseStorage.getInstance()
                                .getReference("avatars/"+uid+".jpg").getDownloadUrl()
                                .addOnSuccessListener(url-> update(name, url.toString())))
                        .addOnFailureListener(e-> update(name, null));
            } else update(name, null);
        });
    }

    private void update(String name, String avatar) {
        if (avatar!=null)
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .update("userName", name, "avatarUrl", avatar)
                    .addOnSuccessListener(a-> finish());
        else
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .update("userName", name)
                    .addOnSuccessListener(a-> finish());
    }
}
