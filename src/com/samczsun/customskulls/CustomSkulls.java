package com.samczsun.customskulls;

import java.util.UUID;

import net.minecraft.server.v1_7_R4.TileEntitySkull;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minecraft.util.org.apache.commons.codec.binary.Base64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlock;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomSkulls extends JavaPlugin implements Listener {
    private static final String PREFIX = "[" + ChatColor.GOLD + "CustomSkulls" + ChatColor.RESET + "] ";

    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender cs, Command c, String l, String[] args) {
        if (cs.hasPermission("customskulls.use")) {
            if (args.length == 0) {
                cs.sendMessage(PREFIX + " Use /customskull <name> to spawn in a skull replacer (case insensitive)!");
            } else {
                String customName = args[0].toLowerCase();
                if (cs.hasPermission("customskulls.spawn.all") || cs.hasPermission("customskulls.spawn." + customName)) {
                    if (getConfig().isString(customName)) {
                        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                        SkullMeta meta = (SkullMeta) skull.getItemMeta();
                        try {
                            ReflectionUtil.set(meta, "profile", getGameProfile(getConfig().getString(customName), customName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        meta.setDisplayName("Replace to " + args[0]);
                        skull.setItemMeta(meta);
                        if (cs instanceof Player) {
                            Player p = (Player) cs;
                            p.getInventory().addItem(skull);
                            p.sendMessage(PREFIX + "You got a replace skull item!");
                        } else if (args.length > 1) {
                            Player p = Bukkit.getPlayer(args[1]);
                            if (p != null) {
                                p.getInventory().addItem(skull);
                                p.sendMessage(PREFIX + "You got a replace skull item!");
                            } else {
                                cs.sendMessage(PREFIX + "That player is not online");
                            }
                        } else {
                            cs.sendMessage(PREFIX + "You must specify a player");
                        }
                    } else {
                        cs.sendMessage(PREFIX + "That is not a custom name");
                    }
                } else {
                    cs.sendMessage(PREFIX + "You need the customskulls.spawn.all or the customskulls.spawn." + customName + " permission!");
                }
            }
        } else {
            cs.sendMessage(PREFIX + "You need the customskulls.use permission!");
        }
        return true;
    }

    @EventHandler
    public void select(PlayerInteractEvent e) throws IllegalArgumentException, IllegalAccessException {
        if (e.getClickedBlock() != null) {
            if (e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) {
                if (e.getClickedBlock().getType() == Material.SKULL) {
                    SkullMeta meta = (SkullMeta) e.getPlayer().getItemInHand().getItemMeta();
                    GameProfile original = ReflectionUtil.get(meta, "profile", GameProfile.class);
                    CraftBlock block = ((CraftBlock) e.getClickedBlock());
                    CraftWorld world = ((CraftWorld) block.getWorld());
                    TileEntitySkull state = (TileEntitySkull) world.getTileEntityAt(block.getX(), block.getY(), block.getZ());
                    if (original != null) {
                        state.setGameProfile(getGameProfile(getConfig().getString(original.getName().toLowerCase()), original.getName()));
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(state.getUpdatePacket());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.SKULL) {
            final Location finalLocation = e.getBlock().getLocation();
            final ItemStack finalItem = e.getItemInHand();
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        SkullMeta meta = (SkullMeta) finalItem.getItemMeta();
                        GameProfile original = ReflectionUtil.get(meta, "profile", GameProfile.class);
                        CraftBlock block = ((CraftBlock) finalLocation.getBlock());
                        CraftWorld world = ((CraftWorld) block.getWorld());
                        TileEntitySkull state = (TileEntitySkull) world.getTileEntityAt(block.getX(), block.getY(), block.getZ());
                        if (original != null) {
                            state.setGameProfile(getGameProfile(getConfig().getString(original.getName().toLowerCase()), original.getName()));
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(state.getUpdatePacket());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskLater(this, 2L);
        }
    }

    public static GameProfile getGameProfile(String skinURL, String name) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", new Property("textures", Base64.encodeBase64String((("{\"timestamp\":" + System.currentTimeMillis() + ",\"profileId\":\"" + profile.getId().toString() + "\",\"profileName\":\"" + name + "\", \"textures\":{SKIN:{url:\"" + skinURL + "\"}}}").getBytes())), "signature"));
        return profile;
    }
}
