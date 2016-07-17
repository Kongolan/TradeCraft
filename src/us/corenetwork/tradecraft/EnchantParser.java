package us.corenetwork.tradecraft;

import java.util.Map;

import net.minecraft.server.v1_10_R1.EnchantmentManager;
import net.minecraft.server.v1_10_R1.MathHelper;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Matej on 29.3.2014.
 */
public class EnchantParser extends NodeParser {
	private ItemStack itemA;
	private ItemStack itemB;
	private ItemStack enchantedItem;

	public EnchantParser(ItemStack enchantedItem, ItemStack itemA,
			ItemStack itemB) {
		this.itemA = itemA;
		this.itemB = itemB;
		this.enchantedItem = enchantedItem;
	}

	public void parse(List<?> nodes) {
		parseNodeList(nodes);

	}

	@Override
	protected void parseNode(String type, LinkedHashMap<?, ?> node) {
		if (type.equals("enchant")) {
			Integer enchantID = null;
			int level = -1;

			for (Map.Entry<?, ?> entry : node.entrySet()) {
				String key = (String) entry.getKey();
				if (key.equalsIgnoreCase("enchantingLvL")) {
					if (entry.getValue().equals("1,30")) {
						Enchantment[] enchantments = Enchantment.values();
						int index = TradeCraftPlugin.random
								.nextInt(enchantments.length);
						enchantID = enchantments[index].getId();
						break;
					} else if (entry.getValue().equals("5,19")) {
						// EnchantmentManager.a(rnd, itemstack1, 5 +
						// rnd.nextInt(15));
						break;
					}
				} else {
					enchantID = MinecraftNames.getEnchantmentId(key);
					if (enchantID != null) {
						level = VillagerConfig
								.getRandomNumber(entry.getValue());
						break;
					}
				}
			}

			if (enchantID == null) {
				Logs.warning("Invalid trades config: Missing enchant ID!");
				return;
			}
			Enchantment enchantment = Enchantment.getById(enchantID.intValue());
			if (enchantment == null) {
				Logs.warning("Invalid enchantment ID: " + enchantID);
				return;
			}

			if (level < 0) {
				int enchantLevel = 0;
				int maxLevel = enchantment.getMaxLevel();

				if (maxLevel == 1)
					enchantLevel = 1;
				else
					enchantLevel = TradeCraftPlugin.random
							.nextInt(maxLevel - 1) + 1;
				// int enchantLevel = VillagerConfig.getRandomNumber(node
				// .get("level"));
				//
				// int maxLvL = enchantment.getMaxLevel();
				// if (enchantLevel > maxLvL)
				// enchantLevel = maxLvL;
				//
				level = enchantLevel;
				if (enchantLevel == 0) {
					Logs.warning("Invalid trades config: Missing or invalid enchant level!");
					return;
				}
			}

			Object bonusA = node.get("bonusAmountA" + level);
			if (bonusA != null && itemA != null) {
				int bonusAmount = VillagerConfig.getRandomNumber(bonusA);
				itemA.setAmount(Math.min(itemA.getAmount() + bonusAmount, itemA
						.getType().getMaxStackSize()));
			}

			Object bonusB = node.get("bonusAmountB" + level);
			if (bonusB != null && itemB != null) {
				int bonusAmount = VillagerConfig.getRandomNumber(bonusB);
				itemB.setAmount(Math.min(itemB.getAmount() + bonusAmount, itemB
						.getType().getMaxStackSize()));
			}

			if (enchantedItem.getType() == Material.ENCHANTED_BOOK) {
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedItem
						.getItemMeta();
				meta.addStoredEnchant(enchantment, level, true);
				enchantedItem.setItemMeta(meta);
			} else
				enchantedItem.addUnsafeEnchantment(enchantment, level);
		}
	}
}
