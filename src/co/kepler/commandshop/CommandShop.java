package co.kepler.commandshop;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import co.kepler.commandshop.commands.CmdShop;

/**
 * @author Ben Woodworth
 * 
 * The main CommandShop class
 */
public class CommandShop extends JavaPlugin {
	private static CommandShop commandShop;
	
	public final Shop shop;
	public final Config config;
	public final CmdShop command;
	
	public CommandShop() {
		commandShop = this;
		config = new Config();
		shop = new Shop();
		command = new CmdShop();
	}
	
	@Override
	public void onEnable() {
		shop.load();

		PluginCommand c = Bukkit.getPluginCommand("shop");
		c.setExecutor(command);
		c.setTabCompleter(command);
		
		Bukkit.getPluginManager().registerEvents(new ShopView(), this);
	}
	
	@Override
	public void onDisable() {
		shop.save();
	}
	
	public void reload() {
		config.load();
	}
	
	public static CommandShop get() {
		return commandShop;
	}
	
	public static void info(String info) {
		commandShop.getLogger().info(info);
	}
}
