package co.kepler.commandshop;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class Config {
	private FileConfiguration config;

	private ItemStack currencyItem = null;
	private Set<Material> disabledItems = null;
	
	public Config() {
		CommandShop plugin = CommandShop.get();
		config = plugin.getConfig();
		plugin.saveDefaultConfig();
	}

	public void load() {
		currencyItem = null;
		disabledItems = null;
		config = CommandShop.get().getConfig();
	}

	public double getStartingBalance() {
		return config.getDouble("startingBalance");
	}

	public ItemStack getCurrencyItem(int amount) {
		// Define currencyItem if null
		if (currencyItem == null) {
			String material = config.getString("currencyItem.item");
			short data = (short) config.getInt("currencyItem.data");
			String dataTag = config.getString("currencyItem.dataTag");

			// Create ItemStack
			if (currencyItem != null) return currencyItem;
			UnsafeValues unsafe = Bukkit.getUnsafe();
			Material m = unsafe.getMaterialFromInternalName(material);
			currencyItem = new ItemStack(m, 1, data);
			unsafe.modifyItemStack(currencyItem, dataTag);
		}
		
		// Set the size of the ItemStack
		ItemStack result = currencyItem.clone();
		result.setAmount(amount);
		return result;
	}
	
	public ItemStack getCurrencyItem() {
		return getCurrencyItem(1);
	}
	
	public boolean isItemDisabled(Material m) {
		if (disabledItems == null) {
			disabledItems = new HashSet<Material>();
			UnsafeValues unsafe = Bukkit.getUnsafe();
			for (String item : config.getStringList("disabledShopItems")) {
				disabledItems.add(unsafe.getMaterialFromInternalName(item));
			}
		}
		return disabledItems.contains(m);
	}
	
	public int getShopsPerPageInChat() {
		return config.getInt("shopsPerPageInChat");
	}
}
