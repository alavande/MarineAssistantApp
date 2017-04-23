package com.alavande.marineassistant;

/**
 * Created by hasee on 2017/04/14.
 */

public class FishEntity {

    private String fishName;
    private int imageResource;

    public FishEntity(String fishName, int imageResource) {
        this.fishName = fishName;
        this.imageResource = imageResource;
    }

    public String getFishName() {
        return fishName;
    }

    public void setFishName(String fishName) {
        this.fishName = fishName;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
