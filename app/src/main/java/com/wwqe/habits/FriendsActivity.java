package com.wwqe.habits;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText friendEmailEdit;
    private Button addFriendButton;
    private LinearLayout friendsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        friendEmailEdit = findViewById(R.id.friendEmailEdit);
        addFriendButton = findViewById(R.id.addFriendButton);
        friendsLayout = findViewById(R.id.friendsLayout);

        addFriendButton.setOnClickListener(v -> addFriend());

        loadFriends();
    }

    private void addFriend() {
        String email = friendEmailEdit.getText().toString();
        if (email.isEmpty()) return;

        // Find user by email
        db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String friendId = task.getResult().getDocuments().get(0).getId();
                        Map<String, Object> friend = new HashMap<>();
                        friend.put("friendId", friendId);
                        friend.put("email", email);

                        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("friends").add(friend);
                        Toast.makeText(this, "Friend added", Toast.LENGTH_SHORT).show();
                        loadFriends();
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFriends() {
        friendsLayout.removeAllViews();
        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().forEach(document -> {
                            String email = document.getString("email");
                            TextView friendView = new TextView(this);
                            friendView.setText(email);
                            friendsLayout.addView(friendView);
                        });
                    }
                });
    }
}