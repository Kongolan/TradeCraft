package us.corenetwork.tradecraft.shopkeeper;

import org.bukkit.entity.Entity;

import us.corenetwork.tradecraft.TradeCraftPlugin;

import com.nisovin.shopkeepers.ShopkeepersPlugin;

public class ShopkeepersHelper {

	private static final String SHOPKEEPERS_NAME = "Shopkeepers";

	private static ShopkeepersPlugin shopkeepersPlugin; // A handle of
														// Shopkeepers, for

	// compatibility.

	/**
	 * Attempts to prepare for interactions with the Shopkeepers plugin, if it
	 * is loaded.
	 */
	public static void connectWithShopkeepers(TradeCraftPlugin plugin) {
		shopkeepersPlugin = (ShopkeepersPlugin) plugin.getServer()
				.getPluginManager().getPlugin(SHOPKEEPERS_NAME);
		if (shopkeepersPlugin != null) {
			plugin.getLogger()
					.info("Successfully connected to "
							+ SHOPKEEPERS_NAME
							+ " - custom shopkeepers will not be altered by this plugin.");
		}
	}

	public static boolean shopkeepersActive() {
		return shopkeepersPlugin != null;
	}

	public static boolean isShopkeeper(Entity vil) {
		if (shopkeepersPlugin == null)
			return false;

		return shopkeepersPlugin.isShopkeeper(vil);
	}
}