package co.kepler.commandshop.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.kepler.commandshop.ChatShopList;
import co.kepler.commandshop.CommandShop;
import co.kepler.commandshop.Lang;
import co.kepler.commandshop.Shop;
import co.kepler.commandshop.Shop.ShopEntry;
import co.kepler.commandshop.Shop.ShopPlayer;
import co.kepler.commandshop.ShopView;
import co.kepler.commandshop.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CmdShop extends Cmd {
	private static final List<String> TAB = ImmutableList.of(
			"list", "buy", "sell", "view", "search", "myshops", "bank"); // TODO Add help command?

	private final List<String> materials;
	public CmdShopSearch cmdShopSearch = new CmdShopSearch();
	public CmdShopMyShops cmdShopMyShops = new CmdShopMyShops();
	public CmdShopBank cmdShopBank = new CmdShopBank();

	public CmdShop() {
		Material[] serverMaterials = Material.values();
		materials = new ArrayList<String>(serverMaterials.length);
		for (Material m : serverMaterials) {
			materials.add("i=" + m);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender,
			Command command, String label, String[] args) {
		if (!(sender instanceof Player)) { // TODO Remove && false
			sender.sendMessage(Lang.MUST_BE_PLAYER);
			return true;
		} else if (args.length == 0) {
			// shop
		} else if (args[0].equalsIgnoreCase("list")) {
			// shop list ...
			return list(sender, args);
		} else if (args[0].equalsIgnoreCase("buy")) {
			// shop buy ...
			return buy(sender, args);
		} else if (args[0].equalsIgnoreCase("sell")) {
			// shop sell ...
			return sell(sender, args);
		} else if (args[0].equalsIgnoreCase("view")) {
			// shop view ...
			return view(sender, args);
		} else if (args[0].equalsIgnoreCase("search")) {
			// shop search ...
			return cmdShopSearch.onCommand(sender, command, label, args);
		} else if (args[0].equalsIgnoreCase("myshops")) {
			// shop myShops ...
			return cmdShopMyShops.onCommand(sender, command, label, args);
		} else if (args[0].equalsIgnoreCase("bank")) {
			// shop bank ...
			return cmdShopBank.onCommand(sender, command, label, args);
		}
		showUsage(sender, "<list|buy|sell|view|search|myShops|bank>");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender,
			Command command, String label, String[] args) {
		if (args.length == 1) {
			return getMatches(args[0], TAB);
		} else if (args[0].equalsIgnoreCase("list")) {
			// shop list ...
		} else if (args[0].equalsIgnoreCase("buy")) {
			// shop buy ...
		} else if (args[0].equalsIgnoreCase("sell")) {
			// shop sell ...
		} else if (args[0].equalsIgnoreCase("view")) {
			// shop view ...
		} else if (args[0].equalsIgnoreCase("search")) {
			// shop search [tab]
			return cmdShopSearch.onTabComplete(sender, command, label, args);
		} else if (args[0].equalsIgnoreCase("myshops")) {
			// shop myshops [tab]
			return cmdShopMyShops.onTabComplete(sender, command, label, args);
		} else if (args[0].equalsIgnoreCase("bank")) {
			// shop bank [tab]
			return cmdShopBank.onTabComplete(sender, command, label, args);
		}
		return EMPTY_LIST;
	}

	private boolean list(CommandSender sender, String[] args) {
		if (args.length == 2) {
			String argPage = args[1];
			if (!Util.isInt(argPage)) {
				sender.sendMessage(ChatColor.RED + "Invalid page: " + argPage);
				return true;
			}
			int page = Integer.parseInt(argPage);
			List<ShopEntry> shops = CommandShop.get().shop.getShopEntries();
			Collections.sort(shops);
			ChatShopList.showList((Player) sender, shops, page,
					"/shop list " + (page - 1), "/shop list " + (page + 1));
			return true;
		}
		showUsage(sender, "list <page>");
		return true;
	}
	
	private boolean buy(CommandSender sender, String[] args) {
		boolean confirmed = false;
		int amount = 1;
		switch (args.length) {
		case 4: // shop buy <shopID> [amount] [confirm]
			String argConfirm = args[3];
			if (argConfirm.equalsIgnoreCase("true")) {
				confirmed = true;
			} else if (argConfirm.equalsIgnoreCase("false")) {
			} else {
				break;
			}
		case 3: // shop buy <shopID> [amount]
			String argAmount = args[2];
			if (!Util.isInt(argAmount)) {
				sender.sendMessage(ChatColor.RED + "Invalid amount: " + argAmount);
				return true;
			} else {
				amount = Integer.parseInt(argAmount);
			}
		case 2: // shop buy <shopID>
			String argID = args[1];
			if (!Util.isInt(argID)) {
				sender.sendMessage(ChatColor.RED + "Invalid Shop ID: " + argID);
				return true;
			}
			int id = Integer.parseInt(argID);

			Shop shop = CommandShop.get().shop;
			ShopEntry se = shop.getShopEntry(id);
			if (se == null) {
				sender.sendMessage(ChatColor.RED + "Shop doesn't exist: " + id);
				return true;
			} else if (se.buyPrice == null) {
				sender.sendMessage(ChatColor.RED + "You cannot buy from this shop!");
				return true;
			}

			double price = se.buyPrice * amount;
			String itemName = Util.getItemDisplayName(se.item.getMaterial(), se.item.getData());
			Player player = (Player) sender;
			ShopPlayer shopPlayer = shop.getPlayer(player);

			if (confirmed) {
				if (se.stock < amount) {
					sender.sendMessage(ChatColor.RED + "There aren't enough items in stock!");
					return true;
				} else if (shopPlayer.bank < price) {
					sender.sendMessage(ChatColor.RED + "You don't have enough money!");
					return true;
				}
				
				ItemStack add = se.item.getItemStack(amount);
				boolean invFull = false;
				for (ItemStack is : player.getInventory().addItem(add).values()) {
					amount -= is.getAmount();
					invFull = true;
				}
				price = amount * se.buyPrice;
				se.money += price;
				se.stock -= amount;
				shopPlayer.bank -= price;
				sender.sendMessage("\n" + ChatColor.GREEN + "Purchased " + amount +
						" x " + itemName + " for " + Util.priceToString(price));
				if (invFull) {
					sender.sendMessage(ChatColor.AQUA +
							"Inventory full. Fewer items purchased.");
				}
			} else {
				TextComponent message = new TextComponent(
						"Buy " + amount + " x " + itemName +
						" for " + Util.priceToString(price) + "? ");
				message.setColor(ChatColor.GREEN);

				TextComponent confirm = new TextComponent("[Click to Confirm]");
				confirm.setColor(ChatColor.AQUA);
				confirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
						"/shop buy " + id + " " + amount + " true"));
				confirm.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextComponent.fromLegacyText(ChatColor.AQUA + "Confirm purchase")));

				message.addExtra(confirm);
				player.spigot().sendMessage(message);
			}
			return true;
		}
		showUsage(sender, "buy <shopID> [amount]");
		return true;
	}

	private boolean sell(CommandSender sender, String[] args) {
		boolean confirmed = false;
		int amount = 1;
		switch (args.length) {
		case 4: // shop sell <shopID> [amount] [confirm]
			String argConfirm = args[3];
			if (argConfirm.equalsIgnoreCase("true")) {
				confirmed = true;
			} else if (argConfirm.equalsIgnoreCase("false")) {
			} else {
				break;
			}
		case 3: // shop sell <shopID> [amount]
			String argAmount = args[2];
			if (!Util.isInt(argAmount)) {
				sender.sendMessage(ChatColor.RED + "Invalid amount: " + argAmount);
				return true;
			} else {
				amount = Integer.parseInt(argAmount);
			}
		case 2: // shop sell <shopID>
			String argID = args[1];
			if (!Util.isInt(argID)) {
				sender.sendMessage(ChatColor.RED + "Invalid Shop ID: " + argID);
				return true;
			}
			int id = Integer.parseInt(argID);

			Shop shop = CommandShop.get().shop;
			ShopEntry se = shop.getShopEntry(id);
			if (se == null) {
				sender.sendMessage(ChatColor.RED + "Shop doesn't exist: " + id);
				return true;
			} else if (se.sellPrice == null) {
				sender.sendMessage(ChatColor.RED + "You cannot sell to this shop!");
				return true;
			}

			double price = se.sellPrice * amount;
			String itemName = Util.getItemDisplayName(se.item.getMaterial(), se.item.getData());
			Player player = (Player) sender;
			ShopPlayer shopPlayer = shop.getPlayer(player);

			if (confirmed) {
				if (se.money < price) {
					sender.sendMessage(ChatColor.RED + "The shop doesn't have enough money!");
					return true;
				}
				
				ItemStack remove = se.item.getItemStack(amount);
				boolean notEnough = false;
				for (ItemStack is : player.getInventory().removeItem(remove).values()) {
					amount -= is.getAmount();
					notEnough = true;
				}
				price = amount * se.sellPrice;
				se.money -= price;
				se.stock += amount;
				shopPlayer.bank += price;
				sender.sendMessage("\n" + ChatColor.GREEN + "Sold " + amount +
						" x " + itemName + " for " + Util.priceToString(price));
				if (notEnough) {
					sender.sendMessage(ChatColor.AQUA +
							"Not enough items. Fewer items sold.");
				}
			} else {
				TextComponent message = new TextComponent(
						"Sell " + amount + " x " + itemName +
						" for " + Util.priceToString(price) + "? ");
				message.setColor(ChatColor.GREEN);

				TextComponent confirm = new TextComponent("[Click to Confirm]");
				confirm.setColor(ChatColor.AQUA);
				confirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
						"/shop sell " + id + " " + amount + " true"));
				confirm.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextComponent.fromLegacyText(ChatColor.AQUA + "Confirm sale")));

				message.addExtra(confirm);
				player.spigot().sendMessage(message);
			}
			return true;
		}
		showUsage(sender, "sell <shopID> [amount]");
		return true;
	}

	private boolean view(CommandSender sender, String[] args) {
		int id, amount = 1;
		switch (args.length) {
		case 3:
			// shop view <shopID> [amount]
			String amountArg = args[2];
			if (Util.isInt(amountArg)) {
				amount = Integer.parseInt(amountArg);
			} else {
				showUsage(sender, "Invalid amount: " + amountArg);
				return true;
			}
		case 2:
			// shop view <shopID>
			String idArg = args[1];
			if (Util.isInt(idArg)) {
				id = Integer.parseInt(idArg);
			} else {
				showUsage(sender, "Invalid Shop ID: " + idArg);
				return true;
			}
			ShopEntry entry = CommandShop.get().shop.getShopEntry(id);
			if (entry == null) {
				sender.sendMessage(ChatColor.RED + "Shop ID does not exist: " + id);
				return true;
			}
			ShopView.show((Player) sender, id, amount);
			return true;
		}
		showUsage(sender, "preview <shopID> [amount]");
		return true;
	}
}
