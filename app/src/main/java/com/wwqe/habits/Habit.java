package com.wwqe.habits;

public class Habit {
    private String id;
    private String name;
    private long streak;
    private long points;

    public Habit() {}

    public Habit(String id, String name, long streak, long points) {
        this.id = id;
        this.name = name;
        this.streak = streak;
        this.points = points;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getStreak() { return streak; }
    public void setStreak(long streak) { this.streak = streak; }

    public long getPoints() { return points; }
    public void setPoints(long points) { this.points = points; }
}