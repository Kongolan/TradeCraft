package us.corenetwork.tradecraft;

import net.minecraft.server.v1_10_R1.Block;
import net.minecraft.server.v1_10_R1.EntityAgeable;
import net.minecraft.server.v1_10_R1.EntityExperienceOrb;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.EntityVillager;
import net.minecraft.server.v1_10_R1.EnumHand;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.MerchantRecipe;
import net.minecraft.server.v1_10_R1.MerchantRecipeList;
import net.minecraft.server.v1_10_R1.MinecraftKey;
import net.minecraft.server.v1_10_R1.MobEffect;
import net.minecraft.server.v1_10_R1.MobEffects;
import net.minecraft.server.v1_10_R1.SoundEffects;
import net.minecraft.server.v1_10_R1.Village;
import net.minecraft.server.v1_10_R1.World;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Created by Matej on 23.2.2014.
 */
public class CustomVillager extends EntityVillager {

	private final static String NO_CAREER = "NO_CAREER";

	private TradeCraftVillager tradeCraftVillager;
	private String lastTradingPlayer = null;

	private boolean overrideName = false;

	// Schedules after window closes
	private boolean updateTrades = false;

	public CustomVillager(World world) {
		super(world);
		Logs.debug("Spawned! " + this.getUniqueID().toString() + "  "
				+ this.world.worldData.getName() + "  " + this.locX + "  "
				+ this.locY + "  " + this.locZ);

		AILimiter.apply(this);
	}

	public CustomVillager(World world, int i) {
		super(world, i);

		AILimiter.apply(this);
	}

	private void init() {
		loadVillagerData();
		if (tradeCraftVillager != null
				&& tradeCraftVillager.getTrades().size() == 0) {
			tradeCraftVillager.addTier(0);
		}
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityAgeable) {
		return b(entityAgeable);
	}

	/**
	 * Returns list of offers
	 */
	@Override
	public MerchantRecipeList getOffers(EntityHuman entityHuman) {

		if (tradeCraftVillager == null)
			return super.getOffers(entityHuman);

		MerchantRecipeList trades = tradeCraftVillager.getTrades();
		if (trades == null || trades.size() == 0) {
			Logs.severe("Villager " + uniqueID.toString() + " has no trades!");

			CustomRecipe recipe = new CustomRecipe(
					new ItemStack((Block) Block.REGISTRY.get(new MinecraftKey(
							"bedrock")), 65), new ItemStack(
							(Block) Block.REGISTRY.get(new MinecraftKey(
									"bedrock")), 1));
			// recipe.lockManually();

			MerchantRecipeList list = new MerchantRecipeList();
			list.add(recipe);

			return list;
		}

		return trades;
	}

	/**
	 * Activated when new player starts trading (or null when nobody is trading
	 * (trading window closes))
	 */
	@Override
	public void setTradingPlayer(EntityHuman entityHuman) {
		if (tradeCraftVillager != null) {
			if (entityHuman == null) // Nobody is trading now
			{

				if (updateTrades) {
					if (!tradeCraftVillager.areAllTiersUnlocked()) {
						tradeCraftVillager.addTier(tradeCraftVillager
								.getLastTier() + 1);
					}

					tradeCraftVillager.refreshAllTrades();

					// Particle effects and increasing village popularity
					Village village = (Village) ReflectionUtils.get(
							EntityVillager.class, this, "village");

					if (village != null && lastTradingPlayer != null) {
						Logs.debugIngame("Reputation UP!");
						this.world.broadcastEntityEffect(this, (byte) 14);
						village.a(lastTradingPlayer, 1);
					}

					// Particle effect when new tier is created
				    this.addEffect(new MobEffect(MobEffects.REGENERATION, 200, 0));

					// set willing
					super.o(true);
				}

				updateTrades = false;
			} else
				Logs.debugIngame("Trading with: "
						+ tradeCraftVillager.getCareer() + " "
						+ this.getUniqueID().toString());
		}

		super.setTradingPlayer(entityHuman);
	}

	/**
	 * Called when somebody right clicks on villager
	 * 
	 * @return has trade window been opened
	 */
	@Override
	public boolean a(EntityHuman entityHuman, EnumHand enumhand, ItemStack itemstack) {
		if (tradeCraftVillager == null)
			init();

		overrideName = true;
		boolean returningBool = super.a(entityHuman, enumhand, itemstack);
		overrideName = false;

		return returningBool;
	}

	@Override
	public String getCustomName() {
		if (overrideName && tradeCraftVillager != null) {
			String color = VillagerConfig.getColor(tradeCraftVillager
					.getCareer());
			color = color.equals("") ? Settings
					.getString(Setting.DEFAULT_PROFESSION_COLOR) : color;
			return ChatColor.translateAlternateColorCodes('&', color
					+ tradeCraftVillager.getCareer());
		} else
			return super.getCustomName();
	}

	/**
	 * Activated when player makes a trade
	 */
	@Override
	public void a(MerchantRecipe vanillaRecipe) {
		if (tradeCraftVillager == null) {
			super.a(vanillaRecipe);
			return;
		}
		// Yes/No sound
		super.a(SoundEffects.gu, cd(), ce());

		// Refresh inventory
		EntityHuman human = t_();
		if (human != null && human instanceof EntityPlayer) {
			final org.bukkit.entity.Player player = ((EntityPlayer) human)
					.getBukkitEntity();
			Bukkit.getScheduler().runTask(TradeCraftPlugin.instance,
					new Runnable() {
						@Override
						public void run() {
							player.updateInventory();
						}
					});
			((EntityPlayer) human).updateInventory(human.activeContainer);
			lastTradingPlayer = human.getName();
		}

		CustomRecipe recipe = (CustomRecipe) vanillaRecipe;
		if (tradeCraftVillager.getTrades() == null)
			return;

		int tradeID = tradeCraftVillager.getTrades().indexOf(recipe);
		if (tradeID < 0) {
			Logs.severe("Player completed unknown trade on villager "
					+ uniqueID.toString() + "! ");
			return;
		}
		tradeCraftVillager.useTrade(recipe);

		Logs.debugIngame("Trade completed! Left:" + recipe.getTradesLeft());

		int xp = 3 + this.random.nextInt(4);

		if (recipe.getTradesPerformed() == 1
				|| random.nextDouble() < Settings
						.getDouble(Setting.ALL_UNLOCKED_REFRESH_CHANCE)) {
			updateTrades = true;
			xp += 5;
		}

		// give xp
		this.world.addEntity(new EntityExperienceOrb(this.world, this.locX,
				this.locY + 0.5D, this.locZ, xp));
	}

	@Override
	public void die() {
		super.die();
		if (dead) {
			if (tradeCraftVillager != null) {
				if (!tradeCraftVillager.isPortaling()) {
					tradeCraftVillager.setDead(true);
				}
			} else {
				Logs.debug("Dead without object " + this.uniqueID.toString());
			}
		}
	}

	public void loadVillagerData() {
		if (!Villagers.exists(uniqueID.toString())) {
			String newCareer = VillagerConfig.getRandomCareer(getProfession());
			if (newCareer == null) {
				newCareer = NO_CAREER;
			}
			if (!newCareer.equals(NO_CAREER)
					|| !Settings
							.getBoolean(Setting.KEEP_VANILLA_IF_NO_TRADES_SPECIFIED)) {
				Villagers.create(uniqueID.toString(), newCareer);
				Logs.debug(this.world.getWorldData().getName() + " "
						+ this.locX + " " + this.locY + " " + this.locZ);
			}
		}
		tradeCraftVillager = Villagers.getVillager(uniqueID.toString());
	}
}
