package co.kepler.commandshop.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.kepler.commandshop.CommandShop;
import co.kepler.commandshop.Shop.ShopEntry;
import co.kepler.commandshop.Shop.ShopPlayer;
import co.kepler.commandshop.Util;
import net.md_5.bungee.api.ChatColor;

public class CmdShopMyShopsManage extends Cmd {
	public static final List<String> TAB = makeList("price", "stock", "money");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		if (args.length == 2) {
			// shop myshops manage
		} else if (args.length >= 3) {
			// shop myshops manage <shopID> ...
			ShopEntry shop;
			String argShopID = args[2];
			if (!Util.isInt(argShopID)) {
				sender.sendMessage(ChatColor.RED + "Invalid Shop ID: " + argShopID);
				return true;
			}
			shop = CommandShop.get().shop.getShopEntry(Integer.parseInt(argShopID));
			if (shop == null) {
				sender.sendMessage(ChatColor.RED + "Shop with ID not found: " + argShopID);
				return true;
			}
			if (!shop.owner.uuid.equals(player.getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "This is not your shop!");
				return true;
			}
			if (args.length > 3) {
				if (args[3].equalsIgnoreCase("price")) {
					return price(player, args, shop);
				} else if (args[3].equalsIgnoreCase("stock")) {
					return stock(player, args, shop);
				} else if (args[3].equalsIgnoreCase("money")) {
					return money(player, args, shop);
				}
			}
			showUsage(sender, "myShops manage " + shop.id + " <price|stock|money>");
			return true;
		}
		showUsage(sender, "myShops manage <shopID> <price|stock|money>");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 4) {
			// shop myShops manage <shopID> [tab]
			return getMatches(args[2], TAB);
		}
		return EMPTY_LIST;
	}

	private boolean price(Player player, String[] args, ShopEntry shopEntry) {
		if (args.length != 6) {
			showUsage(player, "myShops manage " + shopEntry.id + " price <buyPrice> <sellPrice>");
			player.sendMessage(ChatColor.RED + "Use an asterisk (*) to disable buying or selling");
			return true;
		}
		String argBuy = args[4], argSell = args[5];
		Double buy = null, sell = null;
		if (!argBuy.equals("*") && !Util.isDouble(argBuy)) {
			player.sendMessage(ChatColor.RED + "Invalid buy price: " + argBuy);
			return true;
		} else if (!argSell.equals("*") && !Util.isInt(argSell)) {
			player.sendMessage(ChatColor.RED + "Invalid sell price: " + argSell);
			return true;
		}
		if (!argBuy.equals("*")) {
			buy = Double.parseDouble(argBuy);
			if ((int)(buy * 100) != buy * 100) {
				player.sendMessage(ChatColor.RED + "Prices must not have more than two decimal places");
				return true;
			} else if (buy <= 0) {
				player.sendMessage(ChatColor.RED + "Prices must be above $0.00");
			}
		}
		if (!argSell.equals("*")) {
			sell = Double.parseDouble(argSell);
			if ((int)(sell * 100) != sell * 100) {
				player.sendMessage(ChatColor.RED + "Prices must not have more than two decimal places");
				return true;
			} else if (buy <= 0) {
				player.sendMessage(ChatColor.RED + "Prices must be above $0.00");
			}
		}
		shopEntry.buyPrice = buy;
		shopEntry.sellPrice = sell;
		player.sendMessage(ChatColor.GREEN + "Successfully " +
				(buy == null ? "disabled buying from the shop" :
					"set buy price to " + Util.priceToString(buy)) +
				(sell == null ? ", and disabled selling to the shop" :
					", and set sell price to " + Util.priceToString(sell)));
		return true;
	}

	private boolean stock(Player player, String[] args, ShopEntry shopEntry) {
		if (args.length == 6) {
			String argAmount = args[5];
			if (!Util.isInt(argAmount)) {
				player.sendMessage(ChatColor.RED + "Invalid amount: " + argAmount);
				return true;
			}
			int amount = Integer.parseInt(argAmount);
			if (amount <= 0) {
				player.sendMessage(ChatColor.RED + "Amount must be positive");
				return true;
			}
			String argAddRemove = args[4];
			if (argAddRemove.equalsIgnoreCase("add")) {
				int added = amount;
				if (added > 0) {
					ItemStack remove = shopEntry.item.getItemStack(added);
					for (ItemStack i : player.getInventory().removeItem(remove).values()) {
						added -= i.getAmount();
					}
					shopEntry.stock += added;
				}
				player.sendMessage(ChatColor.GREEN + "Successfully added " + added + " stock to the shop");
				if (added != amount) {
					player.sendMessage(ChatColor.GREEN + "Stock not added: " + (amount - added));
				}
				return true;
			} else if (argAddRemove.equalsIgnoreCase("remove")) {
				int removed = Math.min(amount, shopEntry.stock);
				if (removed > 0) {
					ItemStack add = shopEntry.item.getItemStack(removed);
					for (ItemStack i : player.getInventory().addItem(add).values()) {
						removed -= i.getAmount();
					}
					shopEntry.stock -= removed;
				}
				player.sendMessage(ChatColor.GREEN + "Successfully removed " + removed + " stock from the shop");
				if (removed != amount) {
					player.sendMessage(ChatColor.GREEN + "Stock not removed: " + (amount - removed));
				}
				return true;
			}
		}
		showUsage(player, "myShops manage " + shopEntry.id + " stock <add|remove> <amount>");
		return true;
	}

	private boolean money(Player player, String[] args, ShopEntry shopEntry) {
		if (args.length == 6) {
			String argMoney = args[5];
			if (!Util.isDouble(argMoney)) {
				player.sendMessage(ChatColor.RED + "Invalid money amount: " + argMoney);
				return true;
			}
			double money = Double.parseDouble(argMoney);
			if (money <= 0) {
				player.sendMessage(ChatColor.RED + "Money must be positive");
				return true;
			} else if (100 * money != (int)(100 * money)) {
				player.sendMessage(ChatColor.RED + "Money must not have more than two decimal places");
			}
			String argAddRemove = args[4];
			ShopPlayer shopPlayer = CommandShop.get().shop.getPlayer(player);
			if (argAddRemove.equalsIgnoreCase("add")) {
				double added = Math.min(money, shopPlayer.bank);
				shopEntry.money += added;
				shopPlayer.bank -= added;
				player.sendMessage(ChatColor.GREEN + "Successfully transferred " +
						Util.priceToString(added) + " from your bank to the shop");
				if (added != money) {
					player.sendMessage(ChatColor.GREEN + "Money not transferred: " +
							Util.priceToString(money - added));
				}
				return true;
			} else if (argAddRemove.equalsIgnoreCase("remove")) {
				double removed = Math.min(money, shopEntry.money);
				shopEntry.money -= removed;
				shopPlayer.bank += removed;
				player.sendMessage(ChatColor.GREEN + "Successfully transferred " +
						Util.priceToString(removed) + " from the shop to your bank");
				if (removed != money) {
					player.sendMessage(ChatColor.GREEN + "Money not transferred: " +
							Util.priceToString(money - removed));
				}
				return true;
			}
		}
		showUsage(player, "myShops manage " + shopEntry.id + " money <add|remove> <amount>");
		return true;
	}
}
