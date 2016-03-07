package us.corenetwork.tradecraft;

import net.minecraft.server.v1_9_R1.Item;
import net.minecraft.server.v1_9_R1.Enchantment;

/**
 * Created by Matej on 2.12.2014.
 */
public class MinecraftNames {
	
	public static Integer getEnchantmentId(String name) {
		// getByName("name);
		Enchantment nmsEnchantment = Enchantment.b(name);
		if (nmsEnchantment == null)
			return null;

		// nmsEnchantment.id
		return Enchantment.getId(nmsEnchantment);
	}

	public static Integer getMaterialId(String name) {
		Item item = Item.d(name);
		if (item == null)
			return null;

		return Item.getId(item);
	}
	
}
