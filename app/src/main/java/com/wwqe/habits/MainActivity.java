package com.wwqe.habits;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private Button addHabitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        habitsLayout = findViewById(R.id.habitsLayout);
        addHabitButton = findViewById(R.id.addHabitButton);

        addHabitButton.setOnClickListener(v -> addHabit());

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
        // Simple add habit, for demo
        String habitName = "New Habit";
        Map<String, Object> habit = new HashMap<>();
        habit.put("name", habitName);
        habit.put("streak", 0);
        habit.put("points", 0);

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
                            long streak = document.getLong("streak");
                            long points = document.getLong("points");

                            TextView habitView = new TextView(this);
                            habitView.setText(name + " - Streak: " + streak + " - Points: " + points);
                            habitsLayout.addView(habitView);
                        });
                    }
                });
    }
}