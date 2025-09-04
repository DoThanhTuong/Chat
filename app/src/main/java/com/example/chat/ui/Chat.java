package com.example.chat.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.adapters.MessageAdapter;
import com.example.chat.models.Message;
import com.example.chat.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class Chat extends AppCompatActivity implements MessageAdapter.Action {

    private TextView tvHeader;
    private RecyclerView recycler;
    private EditText edtMessage;
    private ImageButton btnSend, btnImage, btnVoice;
    private MessageAdapter adapter;
    private final List<Message> messages = new ArrayList<>();

    private String myUid, myName, peerUid, roomId, myAvatar, peerAvatar,peerName;
    private FirebaseFirestore db;

    // voice
    private MediaRecorder recorder; private String audioPath; private long recordStart;

    private final ActivityResultLauncher<String> reqRecord =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    g -> { if (g) startRecording(); else Toast.makeText(this,"Cần quyền micro",Toast.LENGTH_SHORT).show(); });









    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> { if (uri!=null) uploadImageAndSend(uri); });








    @Override protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_chat);

        tvHeader = findViewById(R.id.txtStartTime);
        recycler = findViewById(R.id.recyclerChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnImage = findViewById(R.id.btnImage);
        btnVoice = findViewById(R.id.btnVoice);

        db = FirebaseUtils.db();
        myUid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("Users").document(myUid)
                .get().addOnSuccessListener(doc -> {
                    myName = doc.getString("userName");
                    // Use myName here
                });
        peerUid = getIntent().getStringExtra("peerUid");
        peerAvatar = getIntent().getStringExtra("peerAvatar");
        peerName = getIntent().getStringExtra("peerName");

        roomId = FirebaseUtils.safeRoomId(myUid, peerUid);

        // lấy avatar mình
        FirebaseFirestore.getInstance().collection("Users").document(myUid)
                .get().addOnSuccessListener(d-> myAvatar = d.getString("avatarUrl"));

        adapter = new MessageAdapter(this, messages, myAvatar, peerAvatar, this);
        recycler.setAdapter(adapter);

        // hiển thị startTime
        DocumentReference chatRef = db.collection("chats").document(roomId);
        chatRef.get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.getLong("startTime") != null) {
                long ts = doc.getLong("startTime");
                tvHeader.setText("Bắt đầu trò chuyện: " + DateFormat.format("dd/MM/yyyy HH:mm", ts));
            }
        });

        // listen messages
        chatRef.collection("messages").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    messages.clear();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Message m = d.toObject(Message.class);
                            if (m != null) messages.add(m);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    recycler.setLayoutManager(new LinearLayoutManager(this));
                    recycler.scrollToPosition(Math.max(0, messages.size()-1));
                });

        btnSend.setOnClickListener(v -> sendText(chatRef));
        btnImage.setOnClickListener(v ->


                pickImage.launch("image/*")

        );
        btnVoice.setOnClickListener(v -> {
            if (recorder == null) reqRecord.launch(Manifest.permission.RECORD_AUDIO);
            else stopRecordingAndSend();
        });
    }

    private void sendText(DocumentReference chatRef) {
        String text = edtMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        ensureChatStartTime(chatRef);
        String id = db.collection("x").document().getId();
        Message m = new Message(id, myUid, peerName, text, null, null, 0, System.currentTimeMillis(), false);
        chatRef.collection("messages").document(id).set(m);
        edtMessage.setText("");
    }

    private void ensureChatStartTime(DocumentReference chatRef) {
        chatRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Map<String,Object> chat = new HashMap<>();
                chat.put("user1", myName); chat.put("user2", peerName);
                chat.put("startTime", System.currentTimeMillis());
                chatRef.set(chat);
                tvHeader.setText("Bắt đầu trò chuyện: " + DateFormat.format("dd/MM/yyyy HH:mm", System.currentTimeMillis()));
            }
        });
    }

//     Image
private void uploadImageAndSend(Uri uri) {
    Log.d("tuong", "Image URI: " + uri);
    ensureChatStartTime(db.collection("chats").document(roomId));
    Log.d("tuong", "Room ID: " + roomId);

    String id = db.collection("x").document().getId();
    Log.d("tuong", "Generated ID: " + id);

    try {
        // Đọc bitmap từ Uri
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) inputStream.close();

        // Nén ảnh thành byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos); // giảm chất lượng để giảm dung lượng
        byte[] imageBytes = baos.toByteArray();

        // Chuyển byte[] thành Base64 String (có prefix)
        String imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Lưu message với chuỗi Base64
        Message m = new Message(
                id,
                myUid,
                peerUid,
                null,
                imageBase64,  // Lưu base64 string thay vì Firebase URL
                null,
                0,
                System.currentTimeMillis(),
                false
        );

        db.collection("chats").document(roomId)
                .collection("messages").document(id).set(m)
                .addOnSuccessListener(a ->
                        Log.d("tuong", "Đã lưu message ảnh (base64 string)")
                )
                .addOnFailureListener(e ->
                        Log.e("tuong", "Lỗi lưu message", e)
                );

    } catch (Exception e) {
        Log.e("tuong", " Lỗi khi convert ảnh thành base64", e);
    }
}



    // Voice
    private void startRecording() {
        try {
            audioPath = getExternalFilesDir(null).getAbsolutePath()+"/voice_"+System.currentTimeMillis()+".m4a";
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(audioPath);
            recorder.prepare();
            recorder.start();
            recordStart = System.currentTimeMillis();
            btnVoice.setImageResource(android.R.drawable.ic_media_pause);
            Toast.makeText(this, "Đang ghi âm... bấm lại để gửi", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            recorder = null;
            Toast.makeText(this, "Không thể ghi âm", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndSend() {
        try { recorder.stop(); } catch (Exception ignored){}
        recorder.release(); recorder = null;
        btnVoice.setImageResource(R.drawable.ic_mic);
        int duration = (int) Math.max(1, (System.currentTimeMillis()-recordStart)/1000);

        String id = db.collection("x").document().getId();
        Uri fileUri = Uri.fromFile(new File(audioPath));
        StorageReference ref = FirebaseStorage.getInstance().getReference("chat_audio/"+roomId+"/"+id+".m4a");
        ref.putFile(fileUri).addOnSuccessListener(t-> ref.getDownloadUrl().addOnSuccessListener(url -> {
            ensureChatStartTime(db.collection("chats").document(roomId));
            Message m = new Message(id, myUid, peerName, null, null, url.toString(), duration, System.currentTimeMillis(), false);
            db.collection("chats").document(roomId).collection("messages").document(id).set(m);
        }));
    }

    // Adapter callbacks
    @Override public void onRecall(String messageId) {
        db.collection("chats").document(roomId).collection("messages").document(messageId)
                .update("recalled", true);
    }

    @Override public void onSaveImage(String url) {
        // Lưu vào thư viện: cách đơn giản -> chụp lại bằng Glide -> MediaStore (bạn có thể hoàn thiện thêm)
        Glide.with(this).asBitmap().load(url).into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
            @Override public void onResourceReady(Bitmap resource, com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                String savedImgURL = MediaStore.Images.Media.insertImage(getContentResolver(), resource, "img_"+System.currentTimeMillis(), "Image from chat");
                if (savedImgURL != null) {
                    Toast.makeText(Chat.this, "Đã lưu ảnh vào thư viện", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Chat.this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
        });

    }

    @Override public void onDownloadAudio(String url) {
        // Có thể dùng DownloadManager để tải về / hoặc mở stream phát thẳng
        Toast.makeText(this, "Bạn có thể mở và tải bằng DownloadManager", Toast.LENGTH_SHORT).show();
    }
}
