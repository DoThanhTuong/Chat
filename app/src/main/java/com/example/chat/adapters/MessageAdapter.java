package com.example.chat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    public interface Action {
        void onRecall(String messageId);
        void onSaveImage(String url);
        void onDownloadAudio(String url);
    }

    private final Context context;
    private final List<Message> data;
    private final String myUid;
    private final String peerAvatar;
    private final String myAvatar;
    private MediaPlayer currentPlayer;
    private ImageButton currentBtn;
    private final Action action;

    public MessageAdapter(Context ctx, List<Message> data, String myAvatar, String peerAvatar, Action action) {
        this.context = ctx; this.data = data;
        this.myUid = FirebaseAuth.getInstance().getUid();
        this.myAvatar = myAvatar; this.peerAvatar = peerAvatar;
        this.action = action;
    }

    @Override public int getItemViewType(int position) {
        return data.get(position).getSenderId().equals(myUid) ? TYPE_RIGHT : TYPE_LEFT;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        int layout = (vt == TYPE_RIGHT) ? R.layout.item_chat_sender : R.layout.item_chat_receiver;
        return new VH(LayoutInflater.from(context).inflate(layout, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder hv, int pos) {
        VH h = (VH) hv;
        Message m = data.get(pos);
        // khi nhấn vào màng hình chat thì bàn phím sẽ ẩn đi
        h.tvText.setOnClickListener(v -> {
            if (context instanceof View.OnClickListener) {
                ((View.OnClickListener) context).onClick(v);
            }
        });

        // avatar
        Glide.with(context).load(peerAvatar)
                .placeholder(R.drawable.ic_person).circleCrop().into(h.imgAvatar);


        // time
        h.tvTime.setText(DateFormat.format("HH:mm", m.getTimestamp()));

        // reset visibility
        h.tvText.setVisibility(View.GONE);
        h.img.setVisibility(View.GONE);
        h.voiceGroup.setVisibility(View.GONE);

        if (m.isRecalled()) {
            h.tvText.setText("Tin nhắn đã được thu hồi");
            h.tvText.setVisibility(View.VISIBLE);
            h.itemView.setOnLongClickListener(null);
            return;
        }

        if (m.getText() != null && !m.getText().isEmpty()) {
            h.tvText.setText(m.getText());
            h.tvText.setVisibility(View.VISIBLE);
        } else if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
            h.img.setVisibility(View.VISIBLE);

            String imgUrl = m.getImageUrl();

            if (imgUrl.startsWith("data:image")) {
                // Trường hợp ảnh dạng base64
                String base64Str = imgUrl.substring(imgUrl.indexOf(",") + 1);
                Bitmap bitmap = decodeBase64(base64Str);
                h.img.setImageBitmap(bitmap);
            } else {
                // Trường hợp ảnh là URL Firebase
                Glide.with(context)
                        .load(imgUrl)
                        .into(h.img);
            }



        } else if (m.getAudioUrl() != null && !m.getAudioUrl().isEmpty()) {
            h.voiceGroup.setVisibility(View.VISIBLE);
            h.tvDuration.setText(format(m.getAudioDuration()));
            h.btnPlayPause.setOnClickListener(v -> playVoice(h, m.getAudioUrl()));
        }

        h.itemView.setOnLongClickListener(v -> {
            PopupMenu menu = new PopupMenu(context, v);
            menu.getMenu().add("Lưu ảnh");
            menu.getMenu().add("Tải voice");
            if (m.getSenderId().equals(myUid)) menu.getMenu().add("Thu hồi");
            menu.setOnMenuItemClickListener(i -> {
                CharSequence t = i.getTitle();
                if ("Lưu ảnh".contentEquals(t) && m.getImageUrl()!=null) {
                    action.onSaveImage(m.getImageUrl());
                } else if ("Thu hồi".contentEquals(t)) {
                    action.onRecall(m.getId());
                } else if ("Tải voice".contentEquals(t) && m.getAudioUrl()!=null) {
                    action.onDownloadAudio(m.getAudioUrl());
                }
                return true;
            });
            menu.show();
            return true;
        });
    }
    private Bitmap decodeBase64(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }


    private void playVoice(VH h, String url) {
        try {
            if (currentPlayer != null) {
                currentPlayer.stop(); currentPlayer.release(); currentPlayer = null;
                if (currentBtn != null) currentBtn.setImageResource(android.R.drawable.ic_media_play);
            }
            currentPlayer = new MediaPlayer();
            currentBtn = h.btnPlayPause;
            currentPlayer.setDataSource(url);
            currentPlayer.setOnPreparedListener(mp -> {
                mp.start();
                h.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            });
            currentPlayer.setOnCompletionListener(mp ->
                    h.btnPlayPause.setImageResource(android.R.drawable.ic_media_play));
            currentPlayer.prepareAsync();
        } catch (Exception ignored) {}
    }

    private String format(int sec) { return (sec/60) + ":" + String.format("%02d", sec%60); }
    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar, img;
        TextView tvText, tvTime, tvDuration;
        View voiceGroup;
        ImageButton btnPlayPause;
        VH(@NonNull View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvText    = v.findViewById(R.id.tvMessage);
            img       = v.findViewById(R.id.imgMessage);
            tvTime    = v.findViewById(R.id.tvTime);
            voiceGroup= v.findViewById(R.id.voiceGroup);
            btnPlayPause = v.findViewById(R.id.btnPlayPause);
            tvDuration   = v.findViewById(R.id.tvDuration);
        }
    }
}
