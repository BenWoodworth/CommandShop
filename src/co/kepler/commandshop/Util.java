package co.kepler.commandshop;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Util {

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {}
		return false;
	}

	public static boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {}
		return false;
	}

	public static boolean isShort(String s) {
		try {
			Short.parseShort(s);
			return true;
		} catch (NumberFormatException e) {}
		return false;
	}
	
	private static DecimalFormat decimalFormat = new DecimalFormat("$0.00");
	public static String priceToString(double d) {
		return decimalFormat.format(d);
	}
	
	@SuppressWarnings("deprecation")
	public static Material parseMaterial(String item) {
		if (isInt(item)) {
			return Material.getMaterial(Integer.parseInt(item));
		}
		
		Material result = Material.getMaterial(item.toUpperCase());
		if (result == null) {
			result = Bukkit.getUnsafe().getMaterialFromInternalName(item.toLowerCase());
		}
		return result == Material.AIR ? null : result;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItemStack(Material item, short data, String dataTag, int amount) {
		ItemStack is = new ItemStack(item, amount, data);
		if (item == Material.AIR) return is;
		return Bukkit.getUnsafe().modifyItemStack(is, dataTag);
	}
	
	public static ItemStack getItemStack(String item, short data, String dataTag, int amount) {
		return getItemStack(parseMaterial(item), data, dataTag, amount);
	}
	
	@SuppressWarnings("deprecation")
	public static String getItemDisplayName(Material item, short data) {
		// Raw item name
		String name = item.getNewData((byte) data).toString();
		
		// Change all underscores to spaces, make all
		// first letters capital, and the rest lowercase.
		char[] result = name.toCharArray();
		boolean prevWasSpace = true;
		for (int i = 0; i < result.length; i++) {
			if (result[i] == ' ' || result[i] == '(') {
				return new String(result, 0, i);
			} else if (result[i] == '_') {
				prevWasSpace = true;
				result[i] = ' ';
			} else if (prevWasSpace) {
				result[i] = Character.toUpperCase(result[i]);
				prevWasSpace = false;
			} else {
				result[i] = Character.toLowerCase(result[i]);
			}
		}
		return new String(result);
	}
}
