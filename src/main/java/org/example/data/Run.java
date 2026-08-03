package org.example.data;

public class Run {
    private String name;
    private int time;
    private String worldId;
    private long runStartTime;



    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setRunStartTime(long runStartTime) {
        this.runStartTime = runStartTime;
    }

    public String getWorldId(){
        return this.worldId;
    }
    public long getRunStartTime(){
        return this.runStartTime;
    }
}
