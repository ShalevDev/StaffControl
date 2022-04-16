package com.shalev.staffcontrol;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VanishManager implements Listener, CommandExecutor {

    public List<Player> lst = new ArrayList<>();
    private Main plugin;

    public VanishManager(Main plugin) {
        this.plugin = plugin;
    }

    private void vanish(Player p){
        for(Player online : Bukkit.getOnlinePlayers()){
            if(!online.equals(p) && !online.hasPermission("sc.vanish"))
                try{
                    online.hidePlayer(plugin,p);
                }
                catch (NoSuchMethodError e) {
                    online.hidePlayer(p);
                }
        }

        lst.add(p);
        p.sendMessage(ChatColor.GREEN+"You are now vanished!");
    }

    private void unVanish(Player p){
        for(Player online : Bukkit.getOnlinePlayers()) {
            if(!online.equals(p)) {
                try{
                    online.showPlayer(plugin,p);
                }
                catch (NoSuchMethodError e) {
                    online.showPlayer(p);
                }
            }
        }

        lst.remove(p);
        p.sendMessage(ChatColor.GREEN+"You are now visible!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {



        if(label.equalsIgnoreCase("vanish")){
            if(plugin.playerCheck(sender)) return true;

            Player p = (Player) sender;
            switchItem(p);
            if(lst.contains(p)){
                unVanish(p);
            }
            else{
                vanish(p);
            }
        }

        return true;
    }

    private boolean checkMode(Player p){
        if(plugin.getConfig().isSet(p.getUniqueId()+".mode"))
            return plugin.getConfig().getBoolean(p.getUniqueId()+".mode");
        return false;
    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        boolean statment;

        try{
            statment =  e.getHand() == EquipmentSlot.HAND && ( e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && checkMode(p);
        }
        catch(NoSuchMethodError event){
            statment =  ( e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && checkMode(p);
        }

        if(statment){


            ItemStack item = p.getInventory().getItemInHand();
            Material m = item.getType();

            Material lever = XMaterial.LEVER.parseMaterial();
            Material torch = XMaterial.REDSTONE_TORCH.parseMaterial();
            Material compass = XMaterial.COMPASS.parseMaterial();
            Material head = XMaterial.PLAYER_HEAD.parseMaterial();
            Material barrier = XMaterial.BARRIER.parseMaterial();

            if(m == lever && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD+"Unvanish")){
                e.setCancelled(true);
                unVanish(p);
                p.getInventory().setItemInHand(setItemName(new ItemStack(torch),ChatColor.GOLD+"Vanish"));
                p.updateInventory();

            }
            else if(m == torch && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD+"Vanish")){
                e.setCancelled(true);
                vanish(p);
                p.getInventory().setItemInHand(setItemName(new ItemStack(lever),ChatColor.GOLD+"Unvanish"));
                p.updateInventory();


            }

            else if(m == compass){
                String sLoc = plugin.getConfig().getString("compass");
                String[] cords = sLoc.split(",");
                World w = p.getWorld();
                Location loc = new Location(w,Double.parseDouble(cords[0]), Double.parseDouble(cords[1]) , Double.parseDouble(cords[2]));
                p.teleport(loc);
                p.sendMessage(ChatColor.GREEN+"Teleported to "+ChatColor.WHITE+sLoc+ChatColor.GREEN+"!");
            }
            else if(m==head){
                e.setCancelled(true);
                List<Player> staff = new ArrayList<>();
                for(Player s : Bukkit.getOnlinePlayers())
                    if(s.hasPermission("sc.staff"))
                        staff.add(s);
                openStaffOnline(p,staff);
            }
            else if(m==barrier) {
                e.setCancelled(true);
                plugin.staffMenu(p);
            }
        }
    }

    private void openStaffOnline(Player p,List<Player> online){
        CustomInventory holder = new CustomInventory("online",new ArrayList<Inventory>());


        int itemsInPage = 38;

        int count=0;
        while(online.size()>0) {
            Inventory inv = Bukkit.createInventory(holder, 54, "Online Staff");
            setFrame(inv, XMaterial.RED_STAINED_GLASS_PANE.parseItem());

            int length = online.size();
            int i;
            int countPlace = 10;
            for ( i = 0; i < length && countPlace < itemsInPage; i++) {
                if(i == inv.getContents().length-11 || i== inv.getContents().length-17)
                    continue;
                inv.setItem(countPlace,getPlayerHead(online.get(0)));
                countPlace+=1;
                online.remove(online.get(0));

            }
            if(count == 0){
                p.openInventory(inv);
            }
            holder.lst.add(inv);

            if(i!=length) {

                inv.setItem(inv.getContents().length - 11, setItemName(XMaterial.ARROW.parseItem(), ChatColor.GREEN+"Next"));
            }
            if(count>0)
                inv.setItem(inv.getContents().length-17,setItemName(XMaterial.ARROW.parseItem(), ChatColor.GREEN+"Previous"));
            count+=1;
        }



    }

    private ItemStack setItemName(ItemStack item,String name){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getPlayerHead(Player player){
        ItemStack item =XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW+ player.getName());
        try{
            meta.setOwningPlayer(player);
        }
        catch(NoSuchMethodError e){
            meta.setOwner(player.getName());
        }

        item.setItemMeta(meta);
        return item;
    }

    public void setFrame(Inventory inv,ItemStack item){
        for(int i=0;i<9;i++) {
            inv.setItem(i, item);
            inv.setItem(inv.getSize()-i-1, item);
        }
        for(int i=9;i<inv.getSize()-9;i+=9){
            inv.setItem(i,item);
            inv.setItem(i+8,item);
        }

    }


    private void switchItem(Player p) {
        Inventory inv = p.getInventory();
        for (int i=0;i<inv.getContents().length;i++){
            ItemStack item = inv.getContents()[i];
            if(item == null)
                return;
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Vanish") && item.getType()==XMaterial.REDSTONE_TORCH.parseMaterial()) {
                ItemStack newItem = new ItemStack(XMaterial.LEVER.parseMaterial());
                ItemMeta meta = newItem.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Unvanish");
                newItem.setItemMeta(meta);
                inv.setItem(i,newItem);

            }
            else if(item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Unvanish") && item.getType()==XMaterial.LEVER.parseMaterial()){
                ItemStack newItem = new ItemStack(XMaterial.REDSTONE_TORCH.parseMaterial());
                ItemMeta meta = newItem.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Vanish");
                newItem.setItemMeta(meta);
                inv.setItem(i,newItem);

            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(!p.isBanned()) {
            if (!p.hasPermission("sc.vanish")) {
                Set<Player> hiddenPlayers = p.spigot().getHiddenPlayers();
                for (Player hidden : hiddenPlayers) {
                    if (!lst.contains(hidden))
                        try{
                            p.showPlayer(plugin,hidden);
                        }
                        catch (NoSuchMethodError error) {
                            p.showPlayer(plugin,hidden);
                        }
                }

                for (Player hidden : lst) {
                    if (!p.spigot().getHiddenPlayers().contains(hidden)) {
                        try{
                            p.hidePlayer(plugin,hidden);
                        }
                        catch (NoSuchMethodError error) {
                            p.hidePlayer(plugin,hidden);
                        }
                    }
                }
            }
        }
    }


}
