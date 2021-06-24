package com.sylvcraft.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import com.sylvcraft.MobAttributes;

public class moba implements TabExecutor {
  MobAttributes plugin;
  
  public moba(MobAttributes instance) {
    plugin = instance;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    List<String> ret = new ArrayList<>();
    
    switch (args.length) {
    case 1:
      if (hasPerm(sender, "toggle")) ret.add("toggle");
      if (hasPerm(sender, "reload")) ret.add("reload");
      if (hasPerm(sender, "set")) ret.add("set");
      return StringUtil.copyPartialMatches(args[0].toLowerCase(), ret, new ArrayList<String>());
      
    case 2:
      if (!hasPerm(sender, "set")) return Collections.emptyList();

      switch (args[0].toLowerCase()) {
      case "set":
        ret.add("[DISABLE]");
        for (EntityType ent : EntityType.values()) {
          ret.add(ent.name());
        }
        return StringUtil.copyPartialMatches(args[1].toLowerCase(), ret, new ArrayList<String>());
      }
      break;
    
    case 3:
      if (!hasPerm(sender, "set")) return Collections.emptyList();

      switch (args[0].toLowerCase()) {
      case "set":
        ret.add("[DISABLE]");
        for (Attribute attr : Attribute.values()) {
          if (attr.name().toLowerCase().indexOf(args[2].toLowerCase()) == -1) continue;
          ret.add(attr.name());
        }
        return ret;
      }
      break;
      
    case 4:
      if (!hasPerm(sender, "set")) return Collections.emptyList();

      switch (args[0].toLowerCase()) {
      case "mob":
        return Arrays.asList("[DISABLE]");
      }
    }
    return Collections.emptyList();
  }
  
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    try {
      if (!(sender instanceof Player)) {
        plugin.msg("player-only", null);
        return true;
      }

      if (args.length == 0) {
        showHelp(sender);
        return true;
      }

      Map<String, String> data = new HashMap<String, String>();
      switch (args[0].toLowerCase()) {
      case "toggle":
        if (!hasPerm(sender, "toggle")) {
          plugin.msg("access-denied", sender);
          return true;
        }
        
        plugin.msg("plugin-" + (plugin.togglePlugin()?"enabled":"disabled"), sender);
        break;
        
      case "reload":
        if (!hasPerm(sender, "reload")) {
          plugin.msg("access-denied", sender);
          return true;
        }
        
        plugin.reloadConfig();
        plugin.msg("reloaded", sender);
        break;
        
      case "set":
        if (!hasPerm(sender, "set")) {
          plugin.msg("access-denied", sender);
          return true;
        }
        
        String mob = args.length > 1?args[1].toUpperCase():"";
        String attr = args.length > 2?args[2].toUpperCase():"";
        String val = args.length > 3?args[3].toUpperCase():"";
        data.put("%mob%", mob);
        data.put("%attr%", attr);
        data.put("%value%", val);

        if (mob.equals("")) {
          plugin.showInfo(sender);
          return true;
        }
        
        switch (attr.toLowerCase()) {
        case "[disable]":
          plugin.getConfig().set("mobs." + mob, null);
          plugin.saveConfig();
          plugin.msg("moba-disabledmob", sender, data);
          return true;
          
        case "":
          plugin.showInfo(sender, mob);
          return true;
        }
        
        switch (val.toLowerCase()) {
        case "[disable]":
          plugin.getConfig().set("mobs." + mob + "." + attr, null);
          plugin.saveConfig();
          plugin.msg("moba-disabledattr", sender, data);
          return true;

        case "":
          plugin.showInfo(sender, mob, attr);
          return true;
        }

        if (val.equals("")) {
          showHelp(sender);
          return true;
        }
        
        double value = Double.valueOf(val);
        plugin.getConfig().set("mobs." + mob + "." + attr, value);
        plugin.saveConfig();
        plugin.msg("moba-set", sender, data);
        break;
      }

      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private void showHelp(CommandSender sender) {
    int displayed = 0;
	if (sender.hasPermission("MobAttributes.xxxx")) { plugin.msg("xxxx", sender); displayed++; }
	if (displayed == 0) plugin.msg("access-denied", sender);
  }
  
  private boolean hasPerm(CommandSender sender, String perm) {
    return sender.hasPermission("mobattributes.admin") || sender.hasPermission("mobattributes." + perm);
  }
}
