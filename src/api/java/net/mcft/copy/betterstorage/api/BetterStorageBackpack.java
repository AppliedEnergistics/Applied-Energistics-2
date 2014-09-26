package net.mcft.copy.betterstorage.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;

public final class BetterStorageBackpack {
	
	private static Method getBackpack = null;
	
	public static List<BackpackSpawnEntry> spawnWithBackpack = new ArrayList<BackpackSpawnEntry>();
	
	private BetterStorageBackpack() {  }
	
	/** Enables a type of entity to spawn with a backpack with a certain probability. <br>
	 *  The class has to match exactly, subclasses need to be registered separately. <br>
	 *  Don't use too high probability, mobs spawning with backpacks should be a rarity. */
	public static void spawnWithBackpack(Class<? extends EntityLivingBase> entityClass, double probability) {
		spawnWithBackpack.add(new BackpackSpawnEntry(entityClass, probability));
	}
	
	/** Returns the backpack stack if an entity is carrying one, or null of it doesn't. <br>
	 *  This can and should be used to check if the entity has a backpack equipped. */
	// Used Thaumcraft's API to see how it did things - I'm unsure what's the best way.
	public static ItemStack getBackpack(EntityLivingBase entity) {
		if (Loader.isModLoaded("betterstorage")) {
			try {
				if (getBackpack == null) {
					Class itemBackpack = Class.forName("net.mcft.copy.betterstorage.item.ItemBackpack");
					getBackpack = itemBackpack.getMethod("getBackpack", EntityLivingBase.class);
				}
				return (ItemStack)getBackpack.invoke(null, entity);
			} catch (Exception e) {
				FMLLog.warning("[betterstorage] Could not invoke net.mcft.copy.betterstorage.item.ItemBackpack.getBackpack.");
			}
		}
		return null;
	}
	
	public static class BackpackSpawnEntry {
		public final Class<? extends EntityLivingBase> entityClass;
		public final double probability;
		public BackpackSpawnEntry(Class<? extends EntityLivingBase> entityClass, double probability) {
			this.entityClass = entityClass;
			this.probability = probability;
		}
	}
	
}
