package com.alavande.marineassistant;

/**
 * Created by hasee on 2017/03/29.
 */

public class NoteEntity {

    private String time, title, content;

    public NoteEntity(String time, String title, String content) {
        this.time = time;
        this.title = title;
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
