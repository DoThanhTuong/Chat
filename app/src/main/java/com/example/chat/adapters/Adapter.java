package com.example.chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.models.User;
import com.example.chat.ui.Chat;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.VH> {

    private final Context context;
    private final List<User> users;

    public Adapter(Context context, List<User> users) {
        this.context = context; this.users = users;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = users.get(pos);
        h.tvName.setText(u.getUserName());
        h.tvEmail.setText(u.getEmail());
        Glide.with(context).load(u.getAvatarUrl())
                .placeholder(R.drawable.ic_person).circleCrop().into(h.imgAvatar);

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, Chat.class);
            i.putExtra("peerUid", u.getUid());
            i.putExtra("peerName", u.getUserName());
            i.putExtra("peerAvatar", u.getAvatarUrl());

            context.startActivity(i);
        });
    }

    @Override public int getItemCount() { return users.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar; TextView tvName, tvEmail;
        VH(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }
}
