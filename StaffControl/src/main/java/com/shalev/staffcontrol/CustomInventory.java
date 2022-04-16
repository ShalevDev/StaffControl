package com.shalev.staffcontrol;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.UUID;

public class CustomInventory implements InventoryHolder {

    private String name;
    private UUID playerBanned;
    private UUID playerBanning;
    private String itemName;
    private String time;
    private String type;
    private Player user;
    public List<Inventory> lst;

    public CustomInventory(String name,List<Inventory> lst){
        this.name = name;
        this.lst = lst;
    }

    public CustomInventory (String name){
        this.name = name;
    }

    public CustomInventory (String name, Player user){
        this.name = name;
        this.user=user;
    }

    public CustomInventory (String name,UUID playerBanned,UUID playerBanning){
        this.name = name;
        this.playerBanned = playerBanned;
        this.playerBanning = playerBanning;
    }

    public CustomInventory(String name,String itemName,String time,String type)
    {
        this.name=name;
        this.itemName=itemName;
        this.time=time;
        this.type = type;
    }



    public Player getUser(){return user;}

    public String getItemName(){return itemName;}

    public String getTime(){return time;}

    public String getType(){return type;}

    public UUID getPlayerBanning(){return playerBanning;}

    public UUID getBanned(){return playerBanned;}

    public String getName(){
        return name;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
