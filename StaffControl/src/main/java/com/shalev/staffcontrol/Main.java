package com.shalev.staffcontrol;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class Main extends JavaPlugin {

    public MyListener listener = new MyListener(this);
    public VanishManager vanishManager = new VanishManager(this);
    public List<Material> lstMat = Arrays.asList(XMaterial.DIAMOND_SWORD.parseMaterial(),XMaterial.IRON_SWORD.parseMaterial(),XMaterial.STONE_SWORD.parseMaterial(),XMaterial.WOODEN_SWORD.parseMaterial(),XMaterial.DIAMOND_PICKAXE.parseMaterial(),XMaterial.ANVIL.parseMaterial(),XMaterial.APPLE.parseMaterial(),XMaterial.MUSHROOM_STEW.parseMaterial(),XMaterial.GOLDEN_APPLE.parseMaterial(),XMaterial.FEATHER.parseMaterial(),XMaterial.DIAMOND_HELMET.parseMaterial(),XMaterial.DIAMOND_CHESTPLATE.parseMaterial(),XMaterial.DIAMOND_LEGGINGS.parseMaterial(),XMaterial.DIAMOND_BOOTS.parseMaterial(),XMaterial.OAK_SAPLING.parseMaterial(),XMaterial.WATER_BUCKET.parseMaterial(),XMaterial.POTION.parseMaterial(),XMaterial.DAYLIGHT_DETECTOR.parseMaterial(),XMaterial.BREAD.parseMaterial(),XMaterial.COOKED_BEEF.parseMaterial(),XMaterial.CHEST.parseMaterial(),XMaterial.CRAFTING_TABLE.parseMaterial(),XMaterial.TORCH.parseMaterial(),XMaterial.STONE.parseMaterial(),XMaterial.ENCHANTING_TABLE.parseMaterial(),XMaterial.COBBLESTONE.parseMaterial(),XMaterial.BOOKSHELF.parseMaterial(),XMaterial.ARROW.parseMaterial(),XMaterial.REDSTONE_TORCH.parseMaterial());


    @Override
    public void onEnable() {
        // Plugin startup logic

        //Opens default config
        saveConfig();
        saveDefaultConfig();

        setDefaultConfig();

        BannedConfig.setup();

        //Opens Config responsible for saving the punishments details
        CustomConfig.setup();

        //Opens config responsible for saving ip's
        IPconfig.setup();

        //Sets up the default punishments if the config is empty
        setPunishments();
        getServer().getPluginManager().registerEvents(listener,this);
        getServer().getPluginManager().registerEvents(vanishManager,this);
        getCommand("vanish").setExecutor(vanishManager);




    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    //Checks if the sender is an instance of a player
    public boolean playerCheck(CommandSender sender){
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "Only a player can perform this command!");
            return true;
        }
        return false;
    }


    //Checks if the player is in staff mode and sets the opposite
    public boolean checkMode(Player p){
        if(!getConfig().isSet(p.getUniqueId()+".mode")) {
            getConfig().set(p.getUniqueId() + ".mode", true);
            saveConfig();
            return false;
        }
        else{
            boolean mode = getConfig().getBoolean(p.getUniqueId()+".mode");
            getConfig().set(p.getUniqueId()+".mode",!mode);
            saveConfig();
            return mode;
        }
    }

    private void setDefaultConfig(){
        if(!getConfig().isSet("compass"))
            getConfig().set("compass","0,0,0");

        saveConfig();
    }

    //Sets the default punishments if the config is empty
    private void setPunishments(){
        List<Punishment> lst = Arrays.asList(new Punishment("Cheating","30d",Material.DIAMOND_SWORD, "IP"),new Punishment("Spamming","5d", XMaterial.FEATHER.parseMaterial(), "NORMAL"),new Punishment("Cursing","3d",XMaterial.APPLE.parseMaterial(),"IP"));
        FileConfiguration config = CustomConfig.getConfig();
        if(config.getKeys(false).size() == 0){
            for (Punishment p : lst){
                config.set(p.getName()+".time",p.getTime());
                config.set(p.getName()+".material",p.getMaterial().toString());
                config.set(p.getName()+".type",p.getType());
            }
        }
        CustomConfig.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if(label.equalsIgnoreCase("punish")){
            //Autocompletes add,remove and the list of online players in the first argument
            if(args.length == 1){
                List<String> lst = new ArrayList<>();
                lst.add("add");
                lst.add("remove");
                for(Player p : Bukkit.getOnlinePlayers())
                    lst.add(p.getName());

                return lst;
            }

            //Cancels the autocomplition of the list of players
            if(args.length == 2 || args.length == 3)
                return new ArrayList<>();

            //Sets the complition to normal and ip
            if(args.length == 4){
                return Arrays.asList("normal","ip");
            }


        }

        return super.onTabComplete(sender, command, label, args);
    }

    private ItemStack setItemName(ItemStack item,String name){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void staffMenu(Player p){
        //If the staff mode is on
        if(checkMode(p))
        {
            //Clear the hotbar
            Inventory playerInv = p.getInventory();
            for(int i=0;i<9;i++)
                playerInv.setItem(i,null);

            p.sendMessage("Staff mode is now "+ChatColor.RED+"OFF"+ChatColor.WHITE+".");
        }
        else {

            Inventory playerInv = p.getInventory();
            //Create the staff hotbar


            playerInv.setItem(0,setItemName(XMaterial.DIAMOND_AXE.parseItem(),ChatColor.GOLD+"Ban GUI"));
            playerInv.setItem(1,setItemName(XMaterial.CARROT_ON_A_STICK.parseItem(),ChatColor.GOLD+"Player Details"));
            if(vanishManager.lst.contains(p))
                playerInv.setItem(2,setItemName(XMaterial.LEVER.parseItem(),ChatColor.GOLD+"Unvanish"));
            else
                playerInv.setItem(2,setItemName(XMaterial.REDSTONE_TORCH.parseItem(),ChatColor.GOLD+"Vanish"));
            playerInv.setItem(3,null);
            playerInv.setItem(4,null);
            playerInv.setItem(5,setItemName(XMaterial.COMPASS.parseItem(),ChatColor.GOLD+"Warp"));
            playerInv.setItem(6,XMaterial.WOODEN_AXE.parseItem());
            playerInv.setItem(7,setItemName(XMaterial.PLAYER_HEAD.parseItem(),ChatColor.GOLD+"Online Staff"));
            playerInv.setItem(8,setItemName(XMaterial.BARRIER.parseItem(),ChatColor.GOLD+"Close Menu"));

            p.sendMessage("Staff mode is now "+ChatColor.GREEN+"ON"+ChatColor.WHITE+".");
        }
    }





    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(label.equalsIgnoreCase("unban")){
            FileConfiguration config = BannedConfig.getConfig();
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if(!player.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED+"This player isn't banned!");
                return true;
            }
            String uuid = player.getUniqueId().toString();
            FileConfiguration ipConfig = IPconfig.getConfig();
            String ip = ipConfig.getString(uuid);
            ip = ip.replace('.','@');
            if( !config.isSet(uuid) && !config.isSet(ip))
            {
                sender.sendMessage(ChatColor.RED+"This player isn't banned!");
                return true;
            }

            config.set(uuid,null);
            config.set(ip,null);
            BannedConfig.save();
            sender.sendMessage(args[0]+ChatColor.GREEN+" has been successfully unbanned");
            return true;
        }

        if(command.getName().equalsIgnoreCase("staffChat")){
            if(playerCheck(sender)) return true;

            Player self = (Player) sender;
            if(args.length>0) {
                for (Player p : Bukkit.getOnlinePlayers()) {

                    if (p.hasPermission("sc.staffchat")) {
                        String message = ChatColor.GOLD + "[StaffChat] " + ChatColor.AQUA + self.getName() + ":" + ChatColor.WHITE + " ";
                        for (String word : args)
                            message += word + " ";
                        p.sendMessage(message);
                    }
                }
            }
            else{

                boolean staffchat = getConfig().getBoolean(self.getUniqueId()+".staffchat");
                if(staffchat)
                {
                    getConfig().set(self.getUniqueId()+".staffchat",false);
                    saveConfig();
                    self.sendMessage(ChatColor.GOLD+"Staff chat is now "+ChatColor.RED+"OFF"+ChatColor.GOLD+".");

                }
                else{
                    getConfig().set(self.getUniqueId()+".staffchat",true);
                    saveConfig();
                    self.sendMessage(ChatColor.GOLD+"Staff chat is now "+ChatColor.GREEN+"ON"+ChatColor.GOLD+".");
                }



            }
        }

        if(label.equalsIgnoreCase("setCompass")){
            if(args.length<3 || !isNumeric(args[0]) || !isNumeric(args[1]) || !isNumeric(args[2])) return false;
            String location = args[0]+","+args[1]+","+args[2];
            getConfig().set("compass",location);
            saveConfig();
            sender.sendMessage(ChatColor.GREEN+"Compass location set to "+ChatColor.WHITE+location);
            return true;
        }

        if(label.equalsIgnoreCase("playerDetails")){
            if(playerCheck(sender)) return true;

            if(args.length<1 || Bukkit.getPlayer(args[0]) == null) return false;

            Player p = Bukkit.getPlayer(args[0]);

            listener.openPlayerDetails((Player) sender,p);
            return true;
        }

        if(label.equalsIgnoreCase("staff"))
        {
            //Checks if the sender is an instance of a player and returns in order to stop if its not
            if(playerCheck(sender)) return true;

            Player p = (Player) sender;

            staffMenu(p);
            return true;
        }
        if(label.equalsIgnoreCase("punish")){
            //Checks if the sender is an instance of a player and returns in order to stop if its not
            if(playerCheck(sender)) return true;

            //if the length of the command is less then 1
            if(args.length<1) return false;

            //gets an instance of a player, both if he is online or not
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[0]);
            Player p = Bukkit.getPlayer(args[0]);

            if(oPlayer.hasPlayedBefore() || p!=null){
                if(p!=null)
                    listener.openBanMenu((Player) sender,p.getUniqueId());
                else
                    listener.openBanMenu((Player) sender,oPlayer.getUniqueId());

            }
            else if(args.length == 2 && args[0].equalsIgnoreCase("remove")){
                FileConfiguration config = CustomConfig.getConfig();

                if(!config.isSet(args[1])){
                    sender.sendMessage(ChatColor.DARK_RED+args[1]+" isn't a punishment!");
                    return true;
                }

                config.set(args[1],null);
                CustomConfig.save();
                sender.sendMessage(ChatColor.GREEN+"Successfully removed "+ChatColor.GRAY+args[1]+ChatColor.GREEN+" punishment");
            }
            else if(args.length>=4 && args[0].equalsIgnoreCase("add")){
                String name = args[1];
                String time = args[2];
                String type = args[3];


                if (!validTime(time) || !validType(type)) return false;

                CustomInventory holder = new CustomInventory("ban-2",name,time,type);
                Inventory inv = Bukkit.createInventory(holder,54,"Choose A Logo!");

                chooseMaterial((Player) sender,inv);


            }
            else return false;

        }

        return true;
    }

    private void chooseMaterial(Player p,Inventory inv){

        listener.setFrame(inv,XMaterial.RED_STAINED_GLASS_PANE.parseItem());

        for(Material m :lstMat){
            inv.addItem(new ItemStack(m));
        }

        p.openInventory(inv);

    }


    private boolean validType(String type){
        if(!type.equalsIgnoreCase("normal") && !type.equalsIgnoreCase("IP")) return false;
        return true;
    }

    private boolean validTime(String time){
        if(time.length()<2) return false;

        String last = time.substring(time.length()-1);
        if(!last.equals("s") && !last.equals("m") && !last.equals("h") && !last.equals("d")) return false;

        time = time.substring(0,time.length()-1);
        if(!isNumeric(time)) return false;

        return true;
    }

    private static boolean isNumeric(String strNum) {
        try {
            Integer.parseInt(strNum);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

