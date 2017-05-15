package com.alavande.marineassistant;

/**
 * Created by hasee on 2017/04/28.
 */

public class Recorder {

    private String activityName;

    public Recorder(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
}
