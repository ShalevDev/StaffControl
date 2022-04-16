package com.shalev.staffcontrol;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class MyListener implements Listener {

    private Main plugin;

    public MyListener(Main plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        FileConfiguration config = IPconfig.getConfig();
        config.set(event.getPlayer().getUniqueId().toString(), event.getPlayer().getAddress().toString());
        IPconfig.save();
    }


    @EventHandler
    public void onPlayerTalk(AsyncPlayerChatEvent e){
        if(plugin.getConfig().getBoolean(e.getPlayer().getUniqueId()+".staffchat"))
        {
            e.setCancelled(true);
            for(Player p: Bukkit.getOnlinePlayers())
                if(p.hasPermission("sc.staffchat"))
                    p.sendMessage(ChatColor.GOLD+"[StaffChat] "+ChatColor.AQUA+e.getPlayer().getName()+": "+ChatColor.WHITE+e.getMessage());
        }
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        ItemStack item = event.getCurrentItem();

        if(event.getInventory().getHolder() instanceof CustomInventory && item!=null)
        {
            CustomInventory holder = (CustomInventory) event.getInventory().getHolder();
            event.setCancelled(true);
            if(holder.getName().equals("ban-1")) {
                if (item.getItemMeta().getLore() != null) {
                    //get the inventory class
                    String reason = item.getItemMeta().getDisplayName();
                    String duration = item.getItemMeta().getLore().get(0);

                    reason = ChatColor.stripColor(reason);
                    duration = ChatColor.stripColor(duration);
                    String[] split = duration.split(" ");
                    duration = split[1]+" "+split[2];

                    Player banned = Bukkit.getPlayer(holder.getBanned());
                    Player banning = Bukkit.getPlayer(holder.getPlayerBanning());
                    OfflinePlayer offlineBanned = null;
                    if(banned ==null)
                        offlineBanned =Bukkit.getOfflinePlayer(holder.getBanned());

                    FileConfiguration config = CustomConfig.getConfig();
                    FileConfiguration ipConfig = IPconfig.getConfig();
                    String type = config.getString(reason + ".type");
                    String target = "";


                    if (type.equalsIgnoreCase("NORMAL")) {
                        target = holder.getBanned().toString();

                    } else {

                        if (banned == null) {
                            target = translateIP(ipConfig.getString(holder.getBanned().toString()));

                        } else {
                            target = translateIP(banned.getAddress().toString());
                        }
                        target = target.replace('.','@');
                    }


                    String banMessage = ChatColor.DARK_RED + "You have been banned from the server!\n" + ChatColor.GRAY + "Reason: " + ChatColor.RED + reason + "\n" + ChatColor.GRAY + "Duration: " + ChatColor.RED + duration;

                    Date date = new Date(System.currentTimeMillis()+time2Date(duration));

                    FileConfiguration banConfig = BannedConfig.getConfig();

                    banConfig.set(target+".time",date.getTime());
                    banConfig.set(target+".reason",reason);

                    BannedConfig.save();

                    //plugin.getServer().getBanList(banType).addBan(target, reason, date, banning.getName());


                    if (banned != null && type.equalsIgnoreCase("NORMAL")) {
                        banned.kickPlayer(banMessage);
                        banned.setPlayerListName(null);
                    }
                    else if (type.equalsIgnoreCase("IP")) {
                        target = target.replace('@','.');
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (translateIP(p.getAddress().toString()).equals(target)) {
                                p.kickPlayer(banMessage);
                                p.setPlayerListName(null);
                            }

                        }
                    }


                    banning.closeInventory();
                    if(banned==null)
                        banning.sendMessage(ChatColor.GRAY + offlineBanned.getName() + ChatColor.GREEN + " has been successfully banned!");
                    else
                        banning.sendMessage(ChatColor.GRAY + banned.getName() + ChatColor.GREEN + " has been successfully banned!");



                }
            }
            else if(holder.getName().equals("ban-2")){
                Inventory inv = event.getClickedInventory();
                for(int i=0;i<inv.getContents().length;i++){
                    if(inv.getContents()[i]!=null && inv.getContents()[i].equals(item)){
                        if(i>=10 && i<=inv.getContents().length-11)
                        {
                            ItemStack selItem = inv.getContents()[i];
                            FileConfiguration config = CustomConfig.getConfig();

                            config.set(holder.getItemName()+".time",holder.getTime());
                            config.set(holder.getItemName()+".material",selItem.getType().toString());
                            config.set(holder.getItemName()+".type",holder.getType());
                            CustomConfig.save();
                            event.getWhoClicked().closeInventory();
                            event.getWhoClicked().sendMessage(ChatColor.GREEN+"Succesfully added "+ChatColor.GRAY+holder.getItemName()+ChatColor.GREEN+" punishment");

                        }
                        return;
                    }
                }
            }
            else if(holder.getName().equals("pDetails")) {
                Inventory inv = event.getClickedInventory();
                for (int i = 0; i < inv.getContents().length; i++) {
                    if (inv.getContents()[i]!=null && inv.getContents()[i].equals(item)) {
                        if(i>=10 && i<=inv.getContents().length-11){
                            XMaterial mat = XMaterial.matchXMaterial(item);

                            if(mat.equals(XMaterial.CHEST)){
                                Inventory pInv = holder.getUser().getInventory();
                                event.getWhoClicked().openInventory(holder.getUser().getInventory());
                            }
                            else if(mat.equals(XMaterial.ENDER_CHEST)){
                                event.getWhoClicked().openInventory(holder.getUser().getEnderChest());
                            }

                        }

                    }
                }
            }
            else if(holder.getName().equals("online") && item.getType() == XMaterial.ARROW.parseMaterial()){
                Player p = (Player) event.getWhoClicked();
                for(int i=0;i<holder.lst.size();i++){
                    if(holder.lst.get(i) == event.getInventory())
                        if(ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Next"))
                            p.openInventory(holder.lst.get(i+1));
                        else
                            p.openInventory(holder.lst.get(i-1));

                }

            }
        }

    }

    @EventHandler
    public void playerLoginEvent(PlayerLoginEvent e){
        FileConfiguration config = BannedConfig.getConfig();
        String uuid = e.getPlayer().getUniqueId().toString();

        String ip = translateIP(e.getAddress().toString().replace('.','@'));
        if(config.isSet(uuid) || config.isSet(ip)) {
            if (config.getLong(uuid + ".time") > System.currentTimeMillis()){



                String reason = config.getString(uuid+".reason");
                String duration = fromMilToTime(config.getLong(uuid+".time")-System.currentTimeMillis());
                String banMessage = ChatColor.DARK_RED + "You have been banned from the server!\n" + ChatColor.GRAY + "Reason: " + ChatColor.RED + reason + "\n" + ChatColor.GRAY + "Duration: " + ChatColor.RED + duration;
                e.setKickMessage(banMessage);
                e.setResult(PlayerLoginEvent.Result.KICK_BANNED);


            }
            else if(config.getLong(ip+".time")  > System.currentTimeMillis()){



                String reason = config.getString(ip+".reason");
                String duration = fromMilToTime(config.getLong(ip+".time")-System.currentTimeMillis());
                String banMessage = ChatColor.DARK_RED + "You have been banned from the server!\n" + ChatColor.GRAY + "Reason: " + ChatColor.RED + reason + "\n" + ChatColor.GRAY + "Duration: " + ChatColor.RED + duration;


                e.setKickMessage(banMessage);

                e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            }
            else {

                config.set(uuid, null);
                config.set(ip,null);
                BannedConfig.save();
            }
        }
    }



    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent e){
        FileConfiguration config = BannedConfig.getConfig();
        String uuid = e.getPlayer().getUniqueId().toString();
        String ip = translateIP(e.getPlayer().getAddress().toString()).replace('.','@');
        if(config.isSet(uuid) || config.isSet(ip)) {
            if (config.getLong(uuid + ".time") > System.currentTimeMillis()){
                String reason = config.getString(uuid+".reason");
                String duration = fromMilToTime(config.getLong(uuid+".time")-System.currentTimeMillis());
                String banMessage = ChatColor.DARK_RED + "You have been banned from the server!\n" + ChatColor.GRAY + "Reason: " + ChatColor.RED + reason + "\n" + ChatColor.GRAY + "Duration: " + ChatColor.RED + duration;
                e.setQuitMessage("");
                e.getPlayer().kickPlayer(banMessage);
            }
            else if(config.getLong(ip+".time")  > System.currentTimeMillis()){
                String reason = config.getString(ip+".reason");
                String duration = fromMilToTime(config.getLong(ip+".time")-System.currentTimeMillis());
                String banMessage = ChatColor.DARK_RED + "You have been banned from the server!\n" + ChatColor.GRAY + "Reason: " + ChatColor.RED + reason + "\n" + ChatColor.GRAY + "Duration: " + ChatColor.RED + duration;
                e.setQuitMessage("");
                e.getPlayer().kickPlayer(banMessage);
            }
            else {
                config.set(uuid, null);
                config.set(uuid,null);
                BannedConfig.save();
            }
        }
    }

    private String fromMilToTime(long mil){
        String message="";
        if(mil>86400000)
        {
            long days = mil/86400000;
            mil = mil-(days*86400000);
            message="Days: "+days+" ,";
        }
        if(mil>3600000)
        {
            long hours = mil/3600000;
            mil = mil-(hours*3600000);
            message+="Hours: "+hours+" ,";
        }
        if(mil>60000){
            long minutes = mil/60000;
            mil = mil-(minutes*60000);
            message+="Minutes: "+minutes+" ,";
        }
        if(mil>1000)
        {
            long seconds = mil/1000;

            message+="Seconds: "+seconds;
        }
        return message;
    }



    private long time2Date(String duration){
        String[] split = duration.split(" ");
        long time = Long.parseLong(split[0]);
        duration = split[1];
        if(duration.equals("second(s)")){
            return time*1000;
        }
        else if(duration.equals("minute(s)")){
            return time*60000;
        }
        else if(duration.equals("hour(s)")){
            return time*3600000;
        }
        else{
            return time*86400000;
        }

    }

    private String translateIP(String ip){
        ip = ip.split(":")[0];
        ip = ip.substring(1);
        return ip;
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){
            Player p = (Player) event.getDamager();
            if(checkMode(p)){
                event.setCancelled(true);
                if(p.getItemInHand().getType() == XMaterial.DIAMOND_AXE.parseMaterial()) {
                    openBanMenu(p, event.getEntity().getUniqueId());
                }
                else if(p.getItemInHand().getType() == XMaterial.CARROT_ON_A_STICK.parseMaterial()){
                    openPlayerDetails(p,(Player) event.getEntity());
                }
                else event.setCancelled(false);

            }
        }
    }



    public void openPlayerDetails(Player executor,Player damaged){
        CustomInventory holder = new CustomInventory("pDetails",damaged);
        Inventory inv = Bukkit.createInventory(holder,36,damaged.getName()+"'s Details");
        setFrame(inv,XMaterial.RED_STAINED_GLASS_PANE.parseItem());


        inv.addItem(setItemName(new ItemStack(XMaterial.COOKED_BEEF.parseItem()),ChatColor.GREEN + "Food: " +ChatColor.WHITE+ damaged.getFoodLevel()));

        Double health = damaged.getHealth();
        int exactHealth;
        if(health%10>=0.5)
            exactHealth= (int) damaged.getHealth()+1;
        else
            exactHealth = (int) damaged.getHealth();
        inv.addItem(setItemName(new ItemStack(XMaterial.GOLDEN_APPLE.parseItem()),ChatColor.GREEN + "Health: " + ChatColor.WHITE+exactHealth));

        inv.addItem(setItemName(new ItemStack(XMaterial.CHEST.parseItem()),ChatColor.GREEN+"Inventory"));

        inv.addItem(setItemName(new ItemStack(XMaterial.ENDER_CHEST.parseItem()),ChatColor.GREEN+"Ender Chest"));

        String time ="";
        try {
            time = ChatColor.GREEN + "Time Played (in hours): " + ChatColor.WHITE + damaged.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000;
        }
        catch (NoSuchFieldError error){
            time = ChatColor.GOLD+"Feature only availble at version 1.13+";
        }
        inv.addItem(setItemName(new ItemStack(XMaterial.IRON_SWORD.parseItem()),time));

        String ip = ChatColor.GREEN+"IP: "+ChatColor.WHITE+translateIP(damaged.getAddress().toString());
        inv.addItem(setItemName(new ItemStack(XMaterial.IRON_ORE.parseItem()),ip));

        String uuid = ChatColor.WHITE+""+damaged.getUniqueId();

        ItemStack item = XMaterial.WHEAT_SEEDS.parseItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+"UUID (unique identifier)");
        meta.setLore(Collections.singletonList(uuid));
        item.setItemMeta(meta);
        inv.addItem(item);

        int level = damaged.getLevel();
        inv.addItem(setItemName(new ItemStack(XMaterial.ENCHANTING_TABLE.parseItem()),ChatColor.GREEN+"Level: "+ChatColor.WHITE+level));


        Location loc = damaged.getLocation();
        String location = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();

        inv.addItem(setItemName(new ItemStack(XMaterial.COMPASS.parseItem()),ChatColor.GREEN+"Location: "+ChatColor.WHITE+location));

        Location spawn = damaged.getBedSpawnLocation();
        if(spawn == null)
            inv.addItem(setItemName(new ItemStack(XMaterial.RED_BED.parseItem()),ChatColor.GREEN+"Spawn Location: "+ChatColor.WHITE+"Default"));
        else
        {
            String spawnLocation = spawn.getBlockX()+","+spawn.getBlockY()+","+spawn.getBlockZ();
            inv.addItem(setItemName(new ItemStack(XMaterial.RED_BED.parseItem()),ChatColor.GREEN+"Spawn Location: "+ChatColor.WHITE+spawnLocation));
        }

        if(damaged.getGameMode() == GameMode.SURVIVAL)
            inv.addItem(setItemName(new ItemStack(XMaterial.COBBLESTONE.parseItem()),ChatColor.GREEN+"Gamemode: "+ChatColor.WHITE+"Survival"));
        else if(damaged.getGameMode() == GameMode.ADVENTURE)
            inv.addItem(setItemName(new ItemStack(XMaterial.SAND.parseItem()),ChatColor.GREEN+"Gamemode: "+ChatColor.WHITE+"Adventure"));
        else if(damaged.getGameMode() == GameMode.SPECTATOR)
            inv.addItem(setItemName(new ItemStack(XMaterial.GLASS.parseItem()),ChatColor.GREEN+"Gamemode: "+ChatColor.WHITE+"Spectator"));
        else if(damaged.getGameMode() == GameMode.CREATIVE)
            inv.addItem(setItemName(new ItemStack(XMaterial.GRASS_BLOCK.parseItem()),ChatColor.GREEN+"Gamemode: "+ChatColor.WHITE+"Creative"));


        executor.openInventory(inv);
    }

    private ItemStack setItemName(ItemStack item,String name){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }


    public void openBanMenu(Player damager, UUID banned){
        Inventory inv = Bukkit.createInventory(new CustomInventory("ban-1",banned,damager.getUniqueId()),54, ChatColor.RED+"Ban GUI");
        setFrame(inv, XMaterial.RED_STAINED_GLASS_PANE.parseItem());
        setPunishments(inv,damager);
        damager.openInventory(inv);
    }

    private void setPunishments(Inventory inv,Player banner){
        FileConfiguration config = CustomConfig.getConfig();

        String[] keys = config.getKeys(false).toArray(new String[0]);


        for(int i=0;i< keys.length;i++){
            String time = translateTime(config.getString(keys[i]+".time"));
            String mName = config.getString(keys[i]+".material");
            String type = config.getString(keys[i]+".type");
            if(time.equals("") || (!type.equalsIgnoreCase("ip") && !type.equalsIgnoreCase("normal"))) {
                banner.sendMessage(ChatColor.DARK_RED+keys[i]+" isn't configured correctly");
                continue;
            }

            Material m = Material.valueOf(mName);

            ItemStack item = new ItemStack(m);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(ChatColor.GREEN+keys[i]);
            meta.setLore(Arrays.asList(ChatColor.GRAY+"Duration: "+ time,ChatColor.GRAY+"Type: "+type.toLowerCase()));
            item.setItemMeta(meta);
            inv.addItem(item);

        }
    }

    private String translateTime(String time){
        if(time.length()<2)
            return "";
        StringBuffer sb = new StringBuffer(time);
        String message="";
        if(sb.charAt(sb.length()-1) == 's')
            message = "second(s)";
        else if(sb.charAt(sb.length()-1) == 'm')
            message ="minute(s)";
        else if(sb.charAt(sb.length()-1) == 'h')
            message = "hour(s)";
        else if(sb.charAt(sb.length()-1) == 'd')
            message = "day(s)";

        sb.deleteCharAt(sb.length()-1);

        if(!isNumeric(sb.toString()) || message.equals("")) {
            return "";
        }

        return sb + " "+message;

    }

    private static boolean isNumeric(String strNum) {
        try {
            Integer.parseInt(strNum);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

    private boolean checkMode(Player p){
        if(plugin.getConfig().isSet(p.getUniqueId()+".mode"))
            return plugin.getConfig().getBoolean(p.getUniqueId()+".mode");
        return false;
    }


}
