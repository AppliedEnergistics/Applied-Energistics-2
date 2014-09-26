package powercrystals.minefactoryreloaded.api.rednet;

import powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetConnection;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Defines a Block that can connect to RedNet cables. This must be implemented on your Block class.
 * <p>
 * Note that when you implement this, the RedNet network makes several assumptions about your code -
 * It will not clamp values to 0 <= x <= 15. This means you must be able to accept any possible integer
 * without crashing, even negatives. It will also assume that calling the onInput(s)Changed() methods
 * are sufficient, and will not issue block updates. In Single mode, it will call onInputChanged.
 * <p>
 * RedNet cables have their subnets indicated to the user by colored bands on the cable.
 * The color of a given subnet is the same as the wool with metadata equal to the subnet number.
 * <p>
 * For reference:<br>
 * 0:White, 1:Orange, 2:Magenta, 3:LightBlue, 4:Yellow, 5:Lime, 6:Pink, 7:Gray,
 * 8:LightGray, 9:Cyan, 10:Purple, 11:Blue, 12:Brown, 13:Green, 14:Red, 15:Black
 */
public interface IRedNetInputNode extends IRedNetConnection
{
	/**
	 * Called when the input values to this block change. Only called if your block is connected in "All" mode.
	 * Do not issue a network value update from inside this method call; it will be ignored. Issue your updates
	 * on the next tick.
	 * 
	 * @param world The world this block is in.
	 * @param x This block's X coordinate.
	 * @param y This block's Y coordinate.
	 * @param z This block's Z coordinate.
	 * @param side The side the input values are being changed on.
	 * @param inputValues The new set of input values. This array will be 16 elements long.
	 */
	public void onInputsChanged(World world, int x, int y, int z, ForgeDirection side, int[] inputValues);

	/**
	 * Called when the input value to this block changes. Only called if your block is connected in "Single" mode.
	 * Do not issue a network value update from inside this method call; it will be ignored. Issue your updates
	 * on the next tick.
	 * 
	 * @param world The world this block is in.
	 * @param x This block's X coordinate.
	 * @param y This block's Y coordinate.
	 * @param z This block's Z coordinate.
	 * @param side The side the input values are being changed on.
	 * @param inputValue The new input value
	 */
	public void onInputChanged(World world, int x, int y, int z, ForgeDirection side, int inputValue);
}
