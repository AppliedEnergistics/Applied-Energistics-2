package powercrystals.minefactoryreloaded.api;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Defines a fruit entry for the Fruit Picker.
 * 
 * @author powercrystals
 *
 */
public interface IFactoryFruit
{
	/**
	 * @return The block this fruit has in the world.
	 */
	public Block getPlant();
	
	/**
	 * Used to determine if this fruit can be picked (is it ripe yet, etc) 
	 * @param world The world where the fruit is being picked
	 * @param x The x-coordinate of the fruit
	 * @param y The y-coordinate of the fruit
	 * @param z The z-coordinate of the fruit
	 * @return True if the fruit can be picked
	 */
	public boolean canBePicked(World world, int x, int y, int z);
	
	/**
	 * @return Whether or not the Fruit Picker should break the block when harvesting. If false, no changes will be performed by the Fruit Picker itself.
	 */
	public boolean breakBlock();
	
	/**
	 * Called by the Fruit Picker to determine what block to replace the picked block with. Only ID and meta/damage will be used.
	 * At the time this method is called, the fruit still exists. Do not pass an item ID as the return value.
	 * @param world The world where the fruit is being picked
	 * @param x The x-coordinate of the fruit
	 * @param y The y-coordinate of the fruit
	 * @param z The z-coordinate of the fruit
	 * @return The block to replace the fruit block with, or null for air.
	 */
	public ReplacementBlock getReplacementBlock(World world, int x, int y, int z);
	
	/**
	 * Called by the Fruit Picker before the fruit is picked.
	 * @param world The world where the fruit is being picked
	 * @param x The x-coordinate of the fruit
	 * @param y The y-coordinate of the fruit
	 * @param z The z-coordinate of the fruit
	 */
	public void prePick(World world, int x, int y, int z);
	
	/**
	 * Called by the Fruit Picker to determine what drops to generate. At the time this method is called, the fruit still exists.
	 * @param world The world where the fruit is being picked
	 * @param x The x-coordinate of the fruit
	 * @param y The y-coordinate of the fruit
	 * @param z The z-coordinate of the fruit
	 */
	public List<ItemStack> getDrops(World world, Random rand, int x, int y, int z);
	
	/**
	 * Called by the Fruit Picker after the fruit is picked.
	 * @param world The world where the fruit is being picked
	 * @param x The x-coordinate of the fruit
	 * @param y The y-coordinate of the fruit
	 * @param z The z-coordinate of the fruit
	 */
	public void postPick(World world, int x, int y, int z);
}
