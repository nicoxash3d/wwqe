package com.wwqe.habits;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayout habitsLayout;
    private Button addHabitButton, friendsButton, leaderboardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        habitsLayout = findViewById(R.id.habitsLayout);
        addHabitButton = findViewById(R.id.addHabitButton);
        friendsButton = findViewById(R.id.friendsButton);
        leaderboardButton = findViewById(R.id.leaderboardButton);

        addHabitButton.setOnClickListener(v -> addHabit());
        friendsButton.setOnClickListener(v -> startActivity(new Intent(this, FriendsActivity.class)));
        leaderboardButton.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));

        loadHabits();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void addHabit() {
        String habitName = "New Habit";
        Map<String, Object> habit = new HashMap<>();
        habit.put("name", habitName);
        habit.put("streak", 0);
        habit.put("points", 0);
        habit.put("lastCompleted", null);

        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("habits").add(habit);
        loadHabits();
    }

    private void loadHabits() {
        habitsLayout.removeAllViews();
        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("habits")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().forEach(document -> {
                            String name = document.getString("name");
                            long streak = document.getLong("streak") != null ? document.getLong("streak") : 0;
                            long points = document.getLong("points") != null ? document.getLong("points") : 0;

                            LinearLayout habitRow = new LinearLayout(this);
                            habitRow.setOrientation(LinearLayout.HORIZONTAL);

                            TextView habitView = new TextView(this);
                            habitView.setText(name + " - Streak: " + streak + " - Points: " + points);
                            habitView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                            Button completeButton = new Button(this);
                            completeButton.setText("Complete");
                            completeButton.setOnClickListener(v -> completeHabit(document.getId(), streak, points));

                            habitRow.addView(habitView);
                            habitRow.addView(completeButton);

                            habitsLayout.addView(habitRow);
                        });
                    }
                });
    }

    private void completeHabit(String habitId, long currentStreak, long currentPoints) {
        long newStreak = currentStreak + 1;
        long newPoints = currentPoints + 10; // 10 puan per completion

        Map<String, Object> updates = new HashMap<>();
        updates.put("streak", newStreak);
        updates.put("points", newPoints);
        updates.put("lastCompleted", System.currentTimeMillis());

        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("habits").document(habitId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Habit completed! +10 points", Toast.LENGTH_SHORT).show();
                    loadHabits();
                });
    }
}