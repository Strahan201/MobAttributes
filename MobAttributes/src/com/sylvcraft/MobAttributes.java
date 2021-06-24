package com.sylvcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sylvcraft.commands.moba;
import com.sylvcraft.events.CreatureSpawn;

public class MobAttributes extends JavaPlugin {
  @Override
  public void onEnable() {
    saveDefaultConfig();
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(new CreatureSpawn(this), this);
    getCommand("moba").setExecutor(new moba(this));
  }

  public boolean togglePlugin() {
    boolean status = !getConfig().getBoolean("status", true);
    getConfig().set("status", status);
    saveConfig();
    return status;
  }
  
  public void showInfo(CommandSender sender) {
    showInfo(sender, "", "");
  }

  public void showInfo(CommandSender sender, String mob) {
    showInfo(sender, mob, "");
  }

  public void showInfo(CommandSender sender, String mob, String attr) {
    Map<String, String> data = new HashMap<String, String>();
    ConfigurationSection cfg = getConfig().getConfigurationSection("mobs");
    if (cfg == null) {
      msg("moba-list-empty", sender);
      return;
    }
    
    for (String keyMob : cfg.getKeys(false)) {
      if (!keyMob.equalsIgnoreCase(mob) && !mob.equals("")) continue;
      
      data.put("%mob%", keyMob);
      ConfigurationSection cfgMob = cfg.getConfigurationSection(keyMob);
      if (cfgMob == null) {
        msg("moba-list-empty-mob", sender, data);
        continue;
      }
      
      for (String keyAttr : cfgMob.getKeys(false)) {
        if (!keyAttr.equalsIgnoreCase(attr) && !attr.equals("")) continue;

        data.put("%attr%", keyAttr);
        data.put("%value%", cfgMob.getString(keyAttr));
        msg("moba-list-mob", sender, data);
      }
    }
  }

  public void applyAttributes(Entity ent) {
    if (!(ent instanceof LivingEntity)) return;
    if (!getConfig().getBoolean("status", true)) return;
    
    ConfigurationSection cfg = getConfig().getConfigurationSection("mobs." + ent.getType().name());
    if (cfg == null) return;
    
    LivingEntity le = (LivingEntity)ent;
    for (String attrString : cfg.getKeys(false)) {
      try {
        Attribute attr = Attribute.valueOf(attrString);
        le.getAttribute(attr).addModifier(new AttributeModifier(UUID.randomUUID(), "generic." + attr + ".modded", cfg.getDouble(attrString), Operation.ADD_NUMBER));
      } catch (IllegalArgumentException ex) {
        getLogger().info("Invalid attribute (" + attrString + ")");
      }
    }
  }
  
  public boolean isInRegion(Location loc, String region) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();
    ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
    if (set.size() == 0) return false;

    for (ProtectedRegion rg: set) if (rg.getId().equalsIgnoreCase(region)) return true;
    return false;
  }
  
  public void msg(String msgCode, CommandSender sender) {
  	if (getConfig().getString("messages." + msgCode) == null) return;
  	msgTransmit(getConfig().getString("messages." + msgCode), sender);
  }

  public void msg(String msgCode, CommandSender sender, Map<String, String> data) {
  	if (getConfig().getString("messages." + msgCode) == null) return;
  	String tmp = getConfig().getString("messages." + msgCode, msgCode);
  	for (Map.Entry<String, String> mapData : data.entrySet()) {
  	  tmp = tmp.replace(mapData.getKey(), mapData.getValue());
  	}
  	msgTransmit(tmp, sender);
  }
  
  public void msgTransmit(String msg, CommandSender sender) {
  	for (String m : (msg + " ").split("%br%")) {
  		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
  	}
  }
}