package powercrystals.minefactoryreloaded.api;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author PowerCrystals
 * 
 * Class used to register plants and other farming-related things with MFR. Will do nothing if MFR does not exist.
 * 
 */
public class FactoryRegistry
{
	/*
	 * This may be called at any time during pre-init, init or post-init, assuming all blocks and items
	 * that are being accessed from the registry have been appropriately registered.
	 * Possible messages:
	 * 
	 * // Registration:
	 * addLaserPreferredOre				| NBTTag with an ItemStack saved on it, with the color on the "value" attribute,
	 * 									| A ValuedItem with item and value set.
	 * registerAutoSpawnerBlacklist		| The String identifier of an entity,
	 * 									| A subclass of EntityLivingBase.
	 * registerFertilizable				| An instance of IFactoryFertilizable.
	 * registerFertilizer				| An instance of IFactoryFertilizer.
	 * registerFruitLog					| The String identifier of a block.
	 * registerGrindable				| An instance of IFactoryGrindable.
	 * registerGrinderBlacklist			| A subclass of EntityLivingBase.
	 * registerHarvestable				| An instance of IFactoryHarvestable.
	 * registerLaserOre					| NBTTag with an ItemStack saved on it, with the weight on the "value" attribute,
	 * 									| A ValuedItem with item and value set.
	 * registerLiquidDrinkHandler		| A ValuedItem with key and object set; ILiquidDrinkHandler expected.
	 * registerMobEggHandler			| An instance of IMobEggHandler.
	 * registerPickableFruit			| An instance of IFactoryFruit.
	 * registerPlantable				| An instance of IFactoryPlantable.
	 * registerRanchable				| An instance of IFactoryRanchable.
	 * registerRedNetLogicCircuit		| An instance of IRedNetLogicCircuit.
	 * registerRubberTreeBiome			| The biomeName field of a biome to white list for rubber trees to spawn in.
	 * registerSafariNetBlacklist		| A subclass of EntityLivingBase.
	 * registerSafariNetHandler			| An instance of ISafariNetHandler.
	 * registerSludgeDrop				| NBTTag with an ItemStack saved on it, with the weight on the "value" attribute,
	 * 									| A ValuedItem with item and value set.
	 * registerSpawnHandler				| An instance of IMobSpawnHandler.
	 * registerVillagerTradeMob			| An instance of IRandomMobProvider.
	 * 
	 * // Simple implementations:
	 * { Harvestables
	 * registerHarvestable_Standard		| The String identifier of a block.
	 * registerHarvestable_Log			| The String identifier of a block.
	 * registerHarvestable_Leaves		| The String identifier of a block.
	 * registerHarvestable_Vine			| The String identifier of a block.
	 * registerHarvestable_Shrub		| The String identifier of a block.
	 * registerHarvestable_Mushroom		| The String identifier of a block.
	 * registerHarvestable_Crop			| An ItemStack of a block, with a damage value indicating the meta value to harvest at.
	 * 									| A ValuedItem with value and object set; Block expected.
	 * registerHarvestable_Gourd		| An NBTTag with the stem and fruit attributes, both String identifiers of blocks.
	 * }
	 * { Plantables
	 * registerPlantable_Standard		| An NBTTag with the seed (Item, String identifier), and
	 * 									  crop (Block, String identifier) attributes set, optionally
	 * 									  also having the meta (Integer, placed metadata value) attribute set.
	 * 									  No special checks for location, just sustainability.
	 * registerPlantable_Crop			| An NBTTag with the seed (Item, String identifier), and
	 * 									  crop (Block, String identifier) attributes set, optionally
	 * 									  also having the meta (Integer, placed metadata value) attribute set.
	 * 									  Will automatically hoe dirt and grass into farmland when planting.
	 * registerPlantable_Sapling		| An NBTTag with the sapling (Block, String identifier), and optionally
	 * 									  the seed (Item, String identifier) attributes set.
	 * }
	 * { Fertilizer
	 * registerFertilizer				| An NBTTag with the fert (Item, String identifier), meta (Integer), and 
	 * 									  type (Integer, index into FertilizerType.values()) attributes set.
	 * }
	 * { Fertilizables
	 * registerFertilizable_Grass		| The String identifier of a block. Will bonemeal the block and expect 
	 * 									  tall grass be planted above and around it, must be IGrowable. Works with
	 * 									  the GrowPlant and Grass type fertilizers, not recommended for crop plants.
	 * registerFertilizable_Gourd		| The String identifier of a block. Must be IGrowable, and expects identical
	 * 									  behavior to vanilla stems. Works with the GrowPlant fertilizers.
	 * registerFertilizable_Crop		| An NBTTag with the plant (Block, String identifier, IGrowable), and
	 * 									  meta (Integer, max growth phase) attributes set, optionally also having
	 * 									  the type (Integer, index into FertilizerType) attribute set.
	 * registerFertilizable_Cocoa		| An NBTTag with the plant (Block, String identifier), and optionally also
	 * 									  the type (Integer, index into FertilizerType) attributes set.
	 * 									  Expects metadata of the block to exactly match cocoa pods.
	 * registerFertilizable_Standard	| An NBTTag with the plant (Block, String identifier, IGrowable), and
	 * 									  optionally also the type (Integer, index into FertilizerType) attributes set.
	 * 									  Expects the block to change when successfully grown (e.g., saplings).
	 * }
	 */
	public static void sendMessage(String message, Object value)
	{
		if (!Loader.isModLoaded("MineFactoryReloaded") ||
				Loader.instance().activeModContainer() == null)
			return;
		try
		{
			Method m = FMLInterModComms.class.getDeclaredMethod("enqueueMessage", Object.class, String.class, IMCMessage.class);
			m.setAccessible(true);
			Constructor<IMCMessage> c = IMCMessage.class.getConstructor(String.class, Object.class);
			c.setAccessible(true);
			m.invoke(null, Loader.instance().activeModContainer(), "MineFactoryReloaded", c.newInstance(message, value));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
