package co.kepler.commandshop.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

/**
 * A class to allow for easier subcommand organization and management.
 * 
 * @author Kepler_
 */
public abstract class Cmd implements CommandExecutor, TabCompleter {
	protected static final List<String> EMPTY_LIST = ImmutableList.of();

	public abstract boolean onCommand(CommandSender sender,
			Command command, String label, String[] args);

	public abstract List<String> onTabComplete(CommandSender sender,
			Command command, String label, String[] args);
	
	/**
	 * Get an immutable list of the specified items.
	 * @param items List elements.
	 * @return Returns an immutable list with the given strings.
	 */
	protected static List<String> makeList(String... items) {
		return ImmutableList.copyOf(items);
	}
	
	/**
	 * Get a list of items that match an argument.
	 * @param arg Argument to find matches of.
	 * @param toMatch List of possible matches.
	 * @return Returns a list of all matching items.
	 */
	protected static List<String> getMatches(String arg, List<String> toMatch) {
		ArrayList<String> result = new ArrayList<String>(toMatch.size());
		arg = arg.toLowerCase();
		for (String s : toMatch) {
			if (s.toLowerCase().startsWith(arg)) {
				result.add(s);
			}
		}
		return result;
	}
	
	/**
	 * Get a list of players who match the argument.
	 * @param arg Argument to find players.
	 * @return Returns all matching players.
	 */
	protected static List<String> listPlayers(String arg) {
		ArrayList<String> names = new ArrayList<String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			names.add(p.getName());
		}
		return getMatches(arg, names);
	}
	
	/**
	 * Send command usage to the command sender.
	 * @param sender The command sender.
	 * @param usage The command usage.
	 */
	protected static void showUsage(CommandSender sender, String usage) {
		sender.sendMessage(ChatColor.RED + "Usage: /shop " + usage);
	}
}
