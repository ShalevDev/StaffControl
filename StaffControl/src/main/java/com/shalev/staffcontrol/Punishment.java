package com.shalev.staffcontrol;

import org.bukkit.Material;

public class Punishment {

    private String name;
    private String time;
    private String type;
    private Material item;


    public Punishment(String name, String time, Material item,String type){
        this.name = name;
        this.time = time;
        this.item = item;
        this.type = type;
    }

    public String getType(){return type;}

    public String getName(){
        return name;
    }

    public String getTime(){
        return time;
    }

    public Material getMaterial(){
        return item;
    }
}
