package com.example.mediaplayer.modelclass_java;

import java.io.Serializable;

public class MediaFiles implements Serializable {
    private String id;
    private String title;
    private String displayname;
    private String size;
    private String duration;
    private String path;
    private String dateAdded;
    public MediaFiles(String id,String title,String displayname,String size,String duration,String path,String dateAdded){
        this.id=id;
        this.title=title;
        this.displayname=displayname;
        this.size=size;
        this.duration=duration;
        this.path=path;
        this.dateAdded=dateAdded;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getDuration() {
        return duration;
    }
    //setters

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
