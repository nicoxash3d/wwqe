package com.wwqe.habits;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements HabitAdapter.OnHabitCompleteListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView habitsRecyclerView;
    private HabitAdapter adapter;
    private List<Habit> habits;
    private FloatingActionButton addHabitFab;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase initialize
        try {
            com.google.firebase.FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Toast.makeText(this, "Firebase init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        habitsRecyclerView = findViewById(R.id.habitsRecyclerView);
        addHabitFab = findViewById(R.id.addHabitFab);

        habits = new ArrayList<>();
        adapter = new HabitAdapter(habits, this);
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitsRecyclerView.setAdapter(adapter);

        addHabitFab.setOnClickListener(v -> addHabit());

        // Menu for friends and leaderboard
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_friends) {
                startActivity(new Intent(this, FriendsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.menu_leaderboard) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                return true;
            }
            return false;
        });
        toolbar.inflateMenu(R.menu.main_menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            loadHabits();
        }
    }

    private void addHabit() {
        String habitName = "New Habit";
        Map<String, Object> habit = new HashMap<>();
        habit.put("name", habitName);
        habit.put("streak", 0);
        habit.put("points", 0);
        habit.put("lastCompleted", null);

        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("habits").add(habit)
                .addOnSuccessListener(documentReference -> loadHabits());
    }

    private void loadHabits() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("habits")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        habits.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                            String id = doc.getId();
                            String name = doc.getString("name") != null ? doc.getString("name") : "Unknown";
                            long streak = doc.getLong("streak") != null ? doc.getLong("streak") : 0;
                            long points = doc.getLong("points") != null ? doc.getLong("points") : 0;
                            habits.add(new Habit(id, name, streak, points));
                        }
                        adapter.updateHabits(habits);
                    } else {
                        Toast.makeText(this, "Load failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onComplete(Habit habit) {
        long newStreak = habit.getStreak() + 1;
        long newPoints = habit.getPoints() + 10;

        Map<String, Object> updates = new HashMap<>();
        updates.put("streak", newStreak);
        updates.put("points", newPoints);
        updates.put("lastCompleted", System.currentTimeMillis());

        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("habits").document(habit.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Habit completed! +10 points", Toast.LENGTH_SHORT).show();
                    loadHabits();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}