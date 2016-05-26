package com.example.q.mobileplayer.bean;

/**
 * Created by Q on 2016/5/23.
 */
public class HomeItem {
    private String name;
    private int resource;

    public HomeItem(String name, int resource) {
        this.name = name;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}
