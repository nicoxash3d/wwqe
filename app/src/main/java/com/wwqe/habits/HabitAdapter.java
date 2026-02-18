package com.wwqe.habits;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habits;
    private OnHabitCompleteListener listener;

    public interface OnHabitCompleteListener {
        void onComplete(Habit habit);
    }

    public HabitAdapter(List<Habit> habits, OnHabitCompleteListener listener) {
        this.habits = habits;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.nameText.setText(habit.getName());
        holder.streakText.setText("Streak: " + habit.getStreak());
        holder.pointsText.setText("Points: " + habit.getPoints());
        holder.completeButton.setOnClickListener(v -> listener.onComplete(habit));
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits;
        notifyDataSetChanged();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, streakText, pointsText;
        Button completeButton;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.habitNameText);
            streakText = itemView.findViewById(R.id.streakText);
            pointsText = itemView.findViewById(R.id.pointsText);
            completeButton = itemView.findViewById(R.id.completeButton);
        }
    }
}