package com.example.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;

import java.util.Map;


public class UserName extends AppCompatActivity {
    private EditText edtName;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        edtName = findViewById(R.id.edt_user_name);
        btnSave = findViewById(R.id.btn_submit);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void saveUserInfo() {
        String name = edtName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        // Cập nhật tên người dùng trong Firestore
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", name);
        userInfo.put("uid", user.getUid());
        userInfo.put("email", user.getEmail());
        db.collection("users").document(user.getUid())
                .set(userInfo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã lưu tên người dùng thành công!", Toast.LENGTH_SHORT).show();
                    // Chuyển đến MainActivity sau khi lưu thành công
                    Intent intent = new Intent(UserName.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }
}

