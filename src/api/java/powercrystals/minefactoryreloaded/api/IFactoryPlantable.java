package powercrystals.minefactoryreloaded.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author PowerCrystals
 *
 * Defines a plantable object for use in the Planter.
 */
public interface IFactoryPlantable
{
	/**
	 * @return The item this plantable is managing.
	 */
	public Item getSeed();

	/**
	 * @param stack The stack being planted.
	 * @return True if this plantable can be planted (useful for metadata items).
	 */
	public boolean canBePlanted(ItemStack stack);
	
	/**
	 * @param world The world instance this block or item will be placed into.
	 * @param x The destination X coordinate.
	 * @param y The destination Y coordinate.
	 * @param z The destination Z coordinate.
	 * @param stack The stack being planted.
	 * @return The block that will be placed into the world.
	 */
	public ReplacementBlock getPlantedBlock(World world, int x, int y, int z, ItemStack stack);
	
	/**
	 * @param world The world instance this block or item will be placed into.
	 * @param x The destination X coordinate.
	 * @param y The destination Y coordinate.
	 * @param z The destination Z coordinate.
	 * @param stack The stack being planted.
	 * @return True if this plantable can be placed at the provided coordinates.
	 */
	public boolean canBePlantedHere(World world, int x, int y, int z, ItemStack stack);
	
	/**
	 * Called before planting is performed. Used to till soil, for example.
	 * @param world The world instance this block or item will be placed into.
	 * @param x The destination X coordinate.
	 * @param y The destination Y coordinate.
	 * @param z The destination Z coordinate.
	 * @param stack The stack being planted.
	 */
	public void prePlant(World world, int x, int y, int z, ItemStack stack);
	
	/**
	 * Called after planting is performed. Usually empty.
	 * @param world The world instance this block or item will be placed into.
	 * @param x The destination X coordinate.
	 * @param y The destination Y coordinate.
	 * @param z The destination Z coordinate.
	 * @param stack The stack being planted.
	 */
	public void postPlant(World world, int x, int y, int z, ItemStack stack);
}
