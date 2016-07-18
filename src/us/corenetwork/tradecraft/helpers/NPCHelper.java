package us.corenetwork.tradecraft.helpers;

import org.bukkit.entity.Entity;

public class NPCHelper {

	public static boolean isNPC(Entity vil) {
		if (ShopkeepersHelper.isShopkeeper(vil))
			return true;

		if (CitizensHelper.isCitizens(vil))
			return true;

		return false;
	}

}
