package com.sylvcraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.sylvcraft.MobAttributes;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawn implements Listener {
  MobAttributes plugin;
  
  public CreatureSpawn(MobAttributes instance) {
    plugin = instance;
  }

  @EventHandler
  public void onCreatureSpawn(CreatureSpawnEvent e) {
    plugin.applyAttributes(e.getEntity());
  }
}