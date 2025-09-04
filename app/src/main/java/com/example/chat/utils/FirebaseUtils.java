package com.example.chat.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtils {
    public static FirebaseAuth auth() { return FirebaseAuth.getInstance(); }
    public static FirebaseFirestore db() { return FirebaseFirestore.getInstance(); }

    public static String safeRoomId(String uid1, String uid2) {
        return (uid1.compareTo(uid2) < 0) ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }
}
