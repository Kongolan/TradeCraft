package us.corenetwork.tradecraft.helpers;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import us.corenetwork.tradecraft.TradeCraftPlugin;

public class CitizensHelper {
	private static final String CITIZENS_NAME = "Citizens";

	private static Plugin citizensPlugin; // A handle of Shopkeepers,
													// for compatibility.

	/**
	 * Attempts to prepare for interactions with the Shopkeepers plugin, if it
	 * is loaded.
	 */
	public static void connectWithCitizens(TradeCraftPlugin plugin) {
		citizensPlugin = plugin.getServer().getPluginManager().getPlugin(CITIZENS_NAME);
		if (citizensPlugin != null) {
			plugin.getLogger().info("Successfully connected to " + CITIZENS_NAME
					+ " - custom npcs will not be altered by this plugin.");
		}
	}

	public static boolean citizensActive() {
		return citizensPlugin != null;
	}

	public static boolean isCitizens(Entity vil) {
		if (citizensPlugin == null)
			return false;
//		NPC npc = CitizensAPI.getNPCRegistry().getNPC(vil);
//		return npc != null;
		boolean isCitizensNPC = vil.hasMetadata("NPC");
		System.out.println("This is a Citizens NPC: "+isCitizensNPC);
		return isCitizensNPC;
	}
	
}
