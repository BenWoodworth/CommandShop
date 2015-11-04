package co.kepler.commandshop;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import co.kepler.commandshop.Shop.ShopEntry;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Ben Woodworth
 *
 * An inventory GUI that pops up showing details about a single shop
 */
public class ShopView implements Listener {
	private static final String TITLE = ChatColor.DARK_GREEN + "View Shop Item";

	private static Set<Integer> previewInventories = new HashSet<Integer>();

	public static void show(Player p, int id, int amount) {
		ShopEntry e = CommandShop.get().shop.getShopEntry(id);
		if (e == null) {
			p.sendMessage(ChatColor.RED + "Shop with ID " + id + " does not exist!");
		}
		
		
		Inventory inv = Bukkit.createInventory(p, 9, TITLE + " (Shop ID: " + e.id + ")");
		previewInventories.add(inv.hashCode());

		// Item
		inv.setItem(0, e.item.getItemStack(1));

		// Player head with owner info
		ItemStack curItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta skullMeta = (SkullMeta) curItem.getItemMeta();
		skullMeta.setOwner(e.owner.name);
		skullMeta.setDisplayName(ChatColor.GREEN + "Shop Owner");
		skullMeta.setLore(Arrays.asList(ChatColor.AQUA + e.owner.name));
		curItem.setItemMeta(skullMeta);
		inv.setItem(6, curItem);

		// Chest showing items in stock and money
		curItem = new ItemStack(Material.CHEST);
		ItemMeta curItemMeta = curItem.getItemMeta();
		curItemMeta.setDisplayName(ChatColor.GREEN + "Stock/Money");
		curItemMeta.setLore(Arrays.asList(
				ChatColor.GREEN + "Stock: " + ChatColor.AQUA + Integer.toString(e.stock) + (e.stock == 1 ? " Item" : " Items"),
				ChatColor.GREEN + "Money: " + ChatColor.AQUA + Util.priceToString(e.money)));
		curItem.setItemMeta(curItemMeta);
		inv.setItem(7, curItem);

		// Show buy and sell prices
		curItem = CommandShop.get().config.getCurrencyItem();
		curItemMeta = curItem.getItemMeta();
		if (amount == 1) {
			curItemMeta.setDisplayName(ChatColor.GREEN + "Prices");
		} else {
			curItemMeta.setDisplayName(ChatColor.GREEN + "Prices (for " + amount + " items)");
		}
		String buyPrice, sellPrice;
		buyPrice = sellPrice = ChatColor.GRAY + "N/A";
		if (e.buyPrice != null) buyPrice = ChatColor.AQUA + Util.priceToString(e.buyPrice * amount);
		if (e.sellPrice != null) sellPrice = ChatColor.AQUA + Util.priceToString(e.sellPrice * amount);
		curItemMeta.setLore(Arrays.asList(
				ChatColor.GREEN + "Buy from shop: " + buyPrice,
				ChatColor.GREEN + "Sell to shop: " + sellPrice));
		curItem.setItemMeta(curItemMeta);
		inv.setItem(8, curItem);

		p.openInventory(inv);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) { // TODO Allow some dragging
		if (event.getInventory().getTitle().startsWith(TITLE)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		if (e.getInventory().getTitle().startsWith(TITLE)) { // TODO Allow some clicking
			e.setCancelled(true);
		}
	}
}
