package co.kepler.commandshop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import co.kepler.commandshop.ChatShopList;
import co.kepler.commandshop.CommandShop;
import co.kepler.commandshop.Shop.ShopEntry;
import co.kepler.commandshop.Util;
import net.md_5.bungee.api.ChatColor;

public class CmdShopSearch extends Cmd {
	private static final String USAGE =
			"search <page> [player] [item] [data] [dataTag]\n" +
					"Use an asterisk (*) as a wildcard";
	private static final List<String>
	PAGE = makeList("<page>", "1", "2", "3", "4", "5"),
	DATA = makeList("[data]", "*", "0"),
	DATA_TAG = makeList("[dataTag]", "*", "{");

	@Override
	public boolean onCommand(CommandSender sender,
			Command command, String label, String[] args) {
		int page;
		String player = null;
		Material itemMaterial = null;
		Short itemData = null;
		String itemDataTag = null;

		switch (args.length) {
		default: // dataTag
			if (!args[5].equals("*")) {
				String[] tagArr = Arrays.copyOfRange(args, 8, args.length - 1);
				itemDataTag = Joiner.on(' ').join(tagArr);
			}
		case 5: // data
			String argData = args[4];
			if (argData.equals("*")) {
			} else if (Util.isShort(argData)) {
				itemData = Short.parseShort(argData);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid item data: " + argData);
				return true;
			}
		case 4: // item
			String argItem = args[3];
			if (!argItem.equals("*")) {
				itemMaterial = Util.parseMaterial(argItem);
				if (itemMaterial == null) {
					sender.sendMessage(ChatColor.RED + "Invalid item: " + argItem);
					return true;
				}
			}
		case 3: // player
			player = args[2];
			if (player.equals("*")) {
				player = null;
			}
		case 2: // page
			String argPage = args[1];
			if (Util.isInt(argPage)) {
				page = Integer.parseInt(argPage);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid page number: " + argPage);
				return true;
			}
			break;
		case 1:
			showUsage(sender, USAGE);
			return true;
		}

		SearchQuery query = new SearchQuery(player, itemMaterial, itemData, itemDataTag);
		List<ShopEntry> results = search(query);

		if (results.size() == 0) {
			sender.sendMessage(ChatColor.RED + "Your search did not match any shop items!");
		} else {
			ChatShopList.showList((Player) sender, results, page,
					query.getCommand(page - 1), query.getCommand(page + 1));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender,
			Command command, String label, String[] args) {
		String arg = args[args.length - 1];
		switch (args.length) {
		case 2: return getMatches(arg, PAGE);
		case 3: return getMatches(arg, CommandShop.get().shop.getOwners("<player>", "*"));
		case 4: return getItemTabComplete(arg);
		case 5: return getMatches(arg, DATA);
		case 6: return getMatches(arg, DATA_TAG);
		default: return EMPTY_LIST;
		}
	}

	@SuppressWarnings("deprecation")
	private List<String> getItemTabComplete(String s) {
		List<String> result = new ArrayList<String>();
		if (s.length() == 0) {
			result.add("[item]");
			result.add("*");
		}
		s = s.toLowerCase();
		Bukkit.getUnsafe().tabCompleteInternalMaterialName(s, result);
		Bukkit.getUnsafe().tabCompleteInternalMaterialName("minecraft:" + s, result);
		return result;
	}

	public List<ShopEntry> search(SearchQuery query) {
		List<ShopEntry> results = new ArrayList<ShopEntry>();
		for (ShopEntry entry : CommandShop.get().shop.getShopEntries()) {
			if (query.entryMatches(entry)) {
				// Add matching shop entries to list
				results.add(entry);
			}
		}
		return ImmutableList.copyOf(results);
	}

	public class SearchQuery {
		public final String player;
		public final Material itemMaterial;
		public final Short itemData;
		public final String itemDataTag;

		public SearchQuery(String player, Material itemMaterial, Short itemData, String itemDataTag) {
			this.player = player;
			this.itemMaterial = itemMaterial;
			this.itemData = itemData;
			this.itemDataTag = itemDataTag;
		}

		public String getCommand(int page) {
			return Joiner.on(' ').join(
					"/shop search",
					Integer.toString(page),
					str(player),
					str(itemMaterial),
					str(itemData),
					str(itemDataTag));
		}

		private String str(Object o) {
			return o == null ? "*" : o.toString();
		}

		public boolean entryMatches(ShopEntry entry) {
			if (	itemMaterial != null && itemMaterial != entry.item.getMaterial() || // Material doesn't match or
					itemData != null && itemData != entry.item.getData() || // Item data doesn't match or
					player != null && !StringUtils.containsIgnoreCase(entry.owner.name, player)) { // Player doesn't match
				return false;
			} else if (itemDataTag != null) {
				ItemStack is = Util.getItemStack(Material.AIR, (short) 0, itemDataTag, 0);
				Map<String, Object> a = is.getItemMeta().serialize();
				Map<String, Object> b = entry.item.itemMeta.serialize();

				// Check that all things in a are also in b.
				for (String key : a.keySet()) {
					if (!a.get(key).equals(b.get(key))) {
						return false;
					}
				}
			}
			return true;
		}
	}
}
