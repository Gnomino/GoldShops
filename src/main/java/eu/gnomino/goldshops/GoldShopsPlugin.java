package eu.gnomino.goldshops;

import org.bukkit.plugin.java.JavaPlugin;

public class GoldShopsPlugin extends JavaPlugin {
	public void onEnable() {
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
	}
	public void onDisable() {
		
	}
}
