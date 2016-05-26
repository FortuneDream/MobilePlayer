package com.example.q.mobileplayer.bean;

import java.io.Serializable;

/**
 * Created by Q on 2016/5/24.
 */
public class VideoItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String duration;
    private long size;
    private String path;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
