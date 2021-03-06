package us.corenetwork.tradecraft.helpers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import us.corenetwork.tradecraft.NMSVillagerManager;
import us.corenetwork.tradecraft.TradeCraftPlugin;

/**
 * An attempt to wait for a newly spawned Villager to be added to
 * activeShopkeepers, so we can tell whether it was indeed a Shopkeeper. Having
 * learned about the Bukkit scheduler from nisovin, I intend to change this
 * mechanic, as multithreading leads to a potential
 * ConcurrentModificationException.
 * 
 * @author Gerrard Lukacs
 */
public class NPCWaiter implements Runnable {
	private Entity villager;

	private int checkAttempts = 5; // Number of times to check if a
									// CUSTOM-spawned villager has been
									// registered as a shopkeeper.

	private int checkDelay = 200; // The time in milliseconds before
									// checking

	// whether a Shopkeeper is registered.

	public NPCWaiter(Entity vil) {
		villager = vil;
	}

	public void run() {
		try {
			for (int i = 0; i < checkAttempts; i++) {
				Thread.sleep(checkDelay);

				// check if that's a shopkeeper - but do that on main thread
				// to be sure (using the scheduler)
				boolean isNPC = Bukkit.getScheduler()
						.callSyncMethod(TradeCraftPlugin.instance, new Callable<Boolean>() {
							public Boolean call() throws Exception {
								return new Boolean(NPCHelper.isNPC(villager));
							}
						}).get().booleanValue();

				if (isNPC) {
					// TradeCraftPlugin.instance.getLogger().info(
					// "Shopkeeper found.");
					return;
				}
			}

			// TradeCraftPlugin.instance.getLogger().info("Not Shopkeeper!");
			if (!NMSVillagerManager.isCustomVillager(villager)) {
				// TradeCraftPlugin.instance.getLogger().info(
				// "Not CostumVillager!");
				Bukkit.getScheduler().runTask(TradeCraftPlugin.instance, new Runnable() {
					@Override
					public void run() {
						NMSVillagerManager.convert(villager);
					}
				});
			}

		} catch (InterruptedException e) {
			// Seriously, assuming no reflection, that shouldn't be possible.
			TradeCraftPlugin.instance.getLogger().info("Thread interruption: No clue how you just managed that.");
			e.printStackTrace();
		} catch (ExecutionException e) {
			TradeCraftPlugin.instance.getLogger()
					.info("Bukkit Scheduler Execution interupted. Dunno if that's a problem.");
			e.printStackTrace();
		}
	}
}