package powercrystals.minefactoryreloaded.api.rednet;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection;

/**
 * Defines a Block that can connect to RedNet cables. This must be implemented on your Block class.
 * <p>
 * Note that when you implement this, the RedNet network makes several assumptions about your code -
 * It will never call the vanilla redstone output methods, querying only the methods contained in
 * this interface, and will not issue block updates.
 * <p>
 * RedNet cables have their subnets indicated to the user by colored bands on the cable.
 * The color of a given subnet is the same as the wool with metadata equal to the subnet number.
 * <p>
 * For reference:<br>
 * 0:White, 1:Orange, 2:Magenta, 3:LightBlue, 4:Yellow, 5:Lime, 6:Pink, 7:Gray,
 * 8:LightGray, 9:Cyan, 10:Purple, 11:Blue, 12:Brown, 13:Green, 14:Red, 15:Black
 */
public interface IRedNetOutputNode extends IRedNetConnection
{
	/**
	 * Returns the output values of this RedNet node. 
	 * This array must be 16 elements long. 
	 * Only called if your block is connected in "All" mode.
	 * 
	 * @param world The world this block is in.
	 * @param x This block's X coordinate.
	 * @param y This block's Y coordinate.
	 * @param z This block's Z coordinate.
	 * @param side The side the output values are required for.
	 * @return The output values.
	 */
	public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection side);

	/**
	 * Returns the output value of this RedNet node for a given subnet.
	 * Must be the same as getOutputValues(world, x, y, z, side)[subnet].
	 * 
	 * @param world The world this block is in.
	 * @param x This block's X coordinate.
	 * @param y This block's Y coordinate.
	 * @param z This block's Z coordinate.
	 * @param side The side the output value is required for.
	 * @param subnet The subnet to get the output value for (0-15).
	 * @return The output value.
	 */
	public int getOutputValue(World world, int x, int y, int z, ForgeDirection side, int subnet);
}
