package co.kepler.commandshop.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.kepler.commandshop.CommandShop;
import co.kepler.commandshop.Shop.ShopPlayer;
import co.kepler.commandshop.Util;
import net.md_5.bungee.api.ChatColor;

public class CmdShopBank extends Cmd {
	private static final List<String> TAB = makeList("balance", "deposit", "withdraw");
	
	@Override
	public boolean onCommand(CommandSender sender,
			Command command, String label, String[] args) {
		if (args.length == 1) {
			// shop bank
		} else if (args[1].equalsIgnoreCase("balance")) {
			// shop bank balance ...
			return balance((Player) sender, args);
		} else if (args[1].equalsIgnoreCase("deposit")) {
			// shop bank deposit ...
			return deposit((Player) sender, args);
		} else if (args[1].equalsIgnoreCase("withdraw")) {
			// shop bank withdraw ...
			return withdraw((Player) sender, args);
		}
		showUsage(sender, "bank <balance|deposit|withdraw>");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 2) {
			// shop bank [tab]
			return getMatches(args[1], TAB);
		}
		return EMPTY_LIST;
	}

	private boolean balance(Player player, String[] args) {
		if (args.length > 2) {
			showUsage(player, "bank balance");
		} else {
			player.sendMessage(ChatColor.GREEN + "Your current bank balance is " +
					Util.priceToString(CommandShop.get().shop.getPlayer(player).bank));
		}
		return true;
	}
	
	private boolean deposit(Player player, String[] args) {
		if (args.length != 3) {
			showUsage(player, "bank deposit <amount>");
			return true;
		}
		
		String argAmount = args[2];
		if (!Util.isInt(argAmount)){
			player.sendMessage(ChatColor.RED + "Invalid amount: " + argAmount);
			return true;
		}
		
		int amount = Integer.parseInt(argAmount);
		int deposited = amount;
		ItemStack remove = CommandShop.get().config.getCurrencyItem(amount);
		for (ItemStack is : player.getInventory().removeItem(remove).values()) {
			deposited -= is.getAmount();
		}
		CommandShop.get().shop.getPlayer(player).bank += deposited;
		
		player.sendMessage(ChatColor.GREEN + "Successfully deposited " + Util.priceToString(deposited));
		if (amount != deposited) {
			player.sendMessage(ChatColor.GREEN + "Not deposited: " + Util.priceToString(amount - deposited));
		}
		return true;
	}
	
	private boolean withdraw(Player player, String[] args) {
		if (args.length != 3) {
			showUsage(player, "bank withdraw <amount>");
			return true;
		}
		
		String argAmount = args[2];
		if (!Util.isInt(argAmount)){
			player.sendMessage(ChatColor.RED + "Invalid amount: " + argAmount);
			return true;
		}
		ShopPlayer shopPlayer = CommandShop.get().shop.getPlayer(player);
		int amount = Integer.parseInt(argAmount);
		int withdrawn = (int) Math.min(amount, shopPlayer.bank);
		ItemStack add = CommandShop.get().config.getCurrencyItem(withdrawn);
		for (ItemStack is : player.getInventory().addItem(add).values()) {
			withdrawn -= is.getAmount();
		}
		shopPlayer.bank -= withdrawn;
		
		player.sendMessage(ChatColor.GREEN + "Successfully withdrew " + Util.priceToString(withdrawn));
		if (amount != withdrawn) {
			player.sendMessage(ChatColor.GREEN + "Not withdrawn: " + Util.priceToString(amount - withdrawn));
		}
		return true;
	}
}
