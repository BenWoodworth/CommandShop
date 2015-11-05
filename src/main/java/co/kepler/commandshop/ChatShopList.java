package co.kepler.commandshop;

import java.util.List;

import org.bukkit.entity.Player;

import co.kepler.commandshop.Shop.ShopEntry;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Ben Woodworth
 *
 * Show a list of shops in a player's chat.
 */
public class ChatShopList {
	
	public static void showList(Player player, List<ShopEntry> shops, int page,
			String prevPageCommand, String nextPageCommand) {
		if (shops.size() == 0) {
			player.sendMessage(ChatColor.RED + "No shops to display!");
			return;
		}
		
		int resultsPerPage = CommandShop.get().config.getShopsPerPageInChat();
		int totalPages = (int)Math.max(1, Math.ceil(shops.size() / (double) resultsPerPage));
		page = Math.min(totalPages, Math.max(1, page));
		
		// Construct prev page button
		TextComponent prevPageText = new TextComponent("\n[<---]");
		if (page == 1) {
			prevPageText.setColor(ChatColor.GRAY);
		} else {
			prevPageText.setColor(ChatColor.AQUA);
			prevPageText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, prevPageCommand));
			prevPageText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					TextComponent.fromLegacyText(ChatColor.GREEN + "Go to previous page")));
		}

		// Add page number
		TextComponent curPageText = new TextComponent(" Page " + page + "/" + totalPages + " ");
		curPageText.setColor(ChatColor.GREEN);

		// Add next page
		TextComponent nextPageText = new TextComponent("[--->]");
		if (page == totalPages) {
			nextPageText.setColor(ChatColor.GRAY);
		} else {
			TextComponent lore = new TextComponent(ChatColor.GREEN + "Go to next page");
			nextPageText.setColor(ChatColor.AQUA);
			nextPageText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextPageCommand));
			nextPageText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new BaseComponent[] {lore}));
		}

		// Add hover/click message
		TextComponent totalShopsText = new TextComponent(" | Total Shops: " + shops.size());
		totalShopsText.setColor(ChatColor.GREEN);

		// Add results
		TextComponent shopsText = null;
		int start = (page - 1) * resultsPerPage;
		int stop = Math.min(start + resultsPerPage, shops.size());
		for (int i = start; i < stop; i++) {
			ShopEntry e = shops.get(i);
			String itemName = Util.getItemDisplayName(e.item.getMaterial(), e.item.getData());
			TextComponent curComponent = new TextComponent(
					"\nShop ID: " + e.id +
					" | " + e.owner.name +
					" | " + itemName +
					" | Stock: " + e.stock +
					" | " + (e.buyPrice == null ? "*" : Util.priceToString(e.buyPrice)) +
					"/" + (e.sellPrice == null ? "*" : Util.priceToString(e.sellPrice)));
			curComponent.setColor(ChatColor.GREEN);

			// Hover text
			String hoverText = ChatColor.GREEN + "Click to view this shop\n" +
					ChatColor.AQUA + "Owner: " + ChatColor.GREEN + e.owner.name + "\n" +
					ChatColor.AQUA + "Item: " + ChatColor.GREEN + itemName + "\n" +
					ChatColor.AQUA + "Stock: " + ChatColor.GREEN + e.stock + "\n" +
					ChatColor.AQUA + "Money: " + ChatColor.GREEN + Util.priceToString(e.money);
			if (e.buyPrice != null) {
				hoverText += "\n" + ChatColor.AQUA + "Buy from shop: " +
						ChatColor.GREEN + Util.priceToString(e.buyPrice);
			}
			if (e.sellPrice != null) {
				hoverText += "\n" + ChatColor.AQUA + "Sell to shop: " +
						ChatColor.GREEN + Util.priceToString(e.sellPrice);
			}
			curComponent.setHoverEvent(new HoverEvent(
					HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));

			// Run command on click
			curComponent.setClickEvent(new ClickEvent(
					ClickEvent.Action.RUN_COMMAND, "/shop view " + e.id));

			// Add to message
			if (shopsText == null) {
				shopsText = curComponent;
			} else {
				shopsText.addExtra(curComponent);
			}
		}
		
		// Tell players to hover over text
		TextComponent extraInfo = new TextComponent("\nMove cursor over shops for more info");
		extraInfo.setColor(ChatColor.AQUA);

		// Tell player if not all items are visible without opening the chat
		if (stop - start > 8) {
			extraInfo.addExtra("\nOpen the chat to view shops");
		}

		// Send message to player
		player.spigot().sendMessage(
				prevPageText,
				curPageText,
				nextPageText,
				totalShopsText,
				shopsText,
				extraInfo);
	}
}
