package co.kepler.commandshop.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

import co.kepler.commandshop.ChatShopList;
import co.kepler.commandshop.CommandShop;
import co.kepler.commandshop.Shop;
import co.kepler.commandshop.Shop.ShopEntry;
import co.kepler.commandshop.Util;
import net.md_5.bungee.api.ChatColor;

public class CmdShopMyShops extends Cmd {
	private static List<String> TAB = makeList("list", "add", "remove", "manage");

	private CmdShopMyShopsManage manage = new CmdShopMyShopsManage();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			// shop myShops
		} else if (args[1].equalsIgnoreCase("list")) {
			// shop myShops list ...
			return list(sender, args);
		} else if (args[1].equalsIgnoreCase("add")) {
			// shop myShops add ...
			return add(sender, args);
		} else if (args[1].equalsIgnoreCase("remove")) {
			// shop myShops remove ...
			return remove(sender, args);
		} else if (args[1].equalsIgnoreCase("manage")) {
			// shop myShops manage ...
			return manage.onCommand(sender, command, label, args);
		}
		showUsage(sender, "myShops <list|add|remove|manage>");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 2) {
			// shop myShops [tab]
			return getMatches(args[1], TAB);
		} else if (args[1].equalsIgnoreCase("list")) {
			// shop myShops list [tab]
		} else if (args[1].equalsIgnoreCase("add")) {
			// shop myShops add [tab]
		} else if (args[1].equalsIgnoreCase("remove")) {
			// shop myShops remove [tab]
		} else if (args[1].equalsIgnoreCase("manage")) {
			// shop myShops manage [tab]
			manage.onTabComplete(sender, command, label, args);
		}
		return EMPTY_LIST;
	}

	public boolean list(CommandSender sender, String[] args) {
		int page = 1;
		if (args.length == 2) {
			// shop myShops list
		} else if (args.length == 3) {
			// shop myShops list [page]
			String argPage = args[2];
			if (!Util.isInt(argPage)) {
				sender.sendMessage("Invalid page number: " + argPage);
				return true;
			}
			page = Integer.parseInt(argPage);
		} else {
			showUsage(sender, "myShops list [page]");
			return true;
		}
		Player player = (Player) sender;
		List<ShopEntry> shops = CommandShop.get().shop.getPlayerShops(player);
		ChatShopList.showList(player, shops, page,
				"/shop myShops list " + (page - 1),
				"/shop myShops list " + (page + 1));
		return true;
	}

	public boolean add(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Shop shop = CommandShop.get().shop;
		ShopEntry newShop;
		if (args.length == 3 && args[2].equalsIgnoreCase("hand")) {
			newShop = shop.createShop(player, player.getItemInHand());
		} else {
			String dataTag = "{}";
			short data = 0;
			Material item;
			switch (args.length) {
			default:
				// shop myShops add <item> [data] [dataTag]
				String[] tagArr = Arrays.copyOfRange(args, 4, args.length - 1);
				dataTag = Joiner.on(' ').join(tagArr);
			case 4:
				// shop myShops add <item> [data]
				String argData = args[3];
				if (!(Util.isShort(argData))) {
					sender.sendMessage("Invalid data value: " + argData);
					return true;
				}
				data = Short.parseShort(argData);
			case 3:
				// shop myShops add <item>
				String itemArg = args[2];
				item = Util.parseMaterial(itemArg);
				if (item == null) {
					sender.sendMessage("Invalid item: " + item);
					return true;
				}
				newShop = shop.createShop((Player) sender, item, data, dataTag);
				break;
			case 2:
				// shop myShops add
				showUsage(sender, "myShops add <item> [data] [dataTag]");
				sender.sendMessage(ChatColor.RED + "Use item in hand: /shop myShops add hand");
				return true;
			}
		}
		if (newShop != null) {
			sender.sendMessage(ChatColor.GREEN + "Shop successfully created! (Shop ID: " + newShop.id + ")\n" +
					ChatColor.GREEN + "Setup your shop with: " +
					ChatColor.AQUA + "/shop myShops manage " + newShop.id);
		}
		return true;
	}

	public boolean remove(CommandSender sender, String[] args) {
		if (args.length != 3) {
			// shop myShops remove
			showUsage(sender, "myShops remove <shopID>");
			return true;
		} else {
			// shops myShops remove <shopID>
			String argID = args[2];
			if (!Util.isInt(argID)) {
				sender.sendMessage(ChatColor.RED + "Invalid Shop ID: " + argID);
				return true;
			}
			CommandShop.get().shop.removeShop((Player) sender, Integer.parseInt(argID));
		}
		return true;
	}
}
