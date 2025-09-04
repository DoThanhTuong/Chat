package com.example.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.R;
import com.example.chat.adapters.Adapter;
import com.example.chat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recycler;
    private final List<User> users = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recyclerUsers);
        adapter = new Adapter(this, users);
        recycler.setAdapter(adapter);

        String myUid = FirebaseAuth.getInstance().getUid();
        Log.d("tuong", "myUid: " + myUid);
        FirebaseFirestore.getInstance().collection("Users")
                .addSnapshotListener((snap, err)->{
                    users.clear();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            User u = d.toObject(User.class);
                            Log.d("tuong", "User: " + u);
                            if (u!=null && !u.getUid().equals(myUid)) {
                                Log.d("tuong", "Thành công");
                                users.add(u);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("tuong", "Users: " + users.size());
                        recycler.setLayoutManager(new LinearLayoutManager(this));

                    }

                });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu); return true;
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId()==R.id.action_profile) {
//            startActivity(new Intent(this, ProfileActivity.class)); return true;
//        } else if (item.getItemId()==R.id.action_logout) {
//
//        }
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, Login.class)); finish(); return true;

    }
}
