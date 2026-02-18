package com.wwqe.habits;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayout leaderboardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        leaderboardLayout = findViewById(R.id.leaderboardLayout);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        leaderboardLayout.removeAllViews();

        // Get friends
        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("friends")
                .get()
                .addOnCompleteListener(friendsTask -> {
                    if (friendsTask.isSuccessful()) {
                        List<String> friendIds = new ArrayList<>();
                        friendIds.add(mAuth.getCurrentUser().getUid()); // Include self

                        friendsTask.getResult().forEach(document -> {
                            friendIds.add(document.getString("friendId"));
                        });

                        // Get points for each
                        Map<String, Long> pointsMap = new HashMap<>();
                        for (String userId : friendIds) {
                            db.collection("users").document(userId).collection("habits")
                                    .get()
                                    .addOnCompleteListener(habitsTask -> {
                                        if (habitsTask.isSuccessful()) {
                                            long totalPoints = 0;
                                            for (com.google.firebase.firestore.DocumentSnapshot doc : habitsTask.getResult()) {
                                                totalPoints += doc.getLong("points") != null ? doc.getLong("points") : 0;
                                            }
                                            pointsMap.put(userId, totalPoints);

                                            if (pointsMap.size() == friendIds.size()) {
                                                displayLeaderboard(pointsMap);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void displayLeaderboard(Map<String, Long> pointsMap) {
        // Sort by points descending
        pointsMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> {
                    TextView scoreView = new TextView(this);
                    scoreView.setText("User: " + entry.getKey() + " - Points: " + entry.getValue());
                    leaderboardLayout.addView(scoreView);
                });
    }
}