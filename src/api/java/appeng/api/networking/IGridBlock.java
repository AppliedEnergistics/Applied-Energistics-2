package appeng.api.networking;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;

/**
 * An Implementation is required to create your node for IGridHost
 * 
 * Implement for use with IGridHost
 */
public interface IGridBlock
{

	/**
	 * how much power to drain per tick as part of idle network usage.
	 * 
	 * if the value of this changes, you must post a MENetworkPowerIdleChange
	 * 
	 * @return ae/t to use.
	 */
	double getIdlePowerUsage();

	/**
	 * Various flags that AE uses to modify basic behavior for various parts of the network.
	 * 
	 * @return Set of flags for this IGridBlock
	 */
	EnumSet<GridFlags> getFlags();

	/**
	 * generally speaking you will return true for this, the one exception is buses, or worm holes where the node
	 * represents something that isn't a real connection in the world, but rather one represented internally to the
	 * block.
	 * 
	 * @return if the world can connect to this node, and the node can connect to the world.
	 */
	boolean isWorldAccessible();

	/**
	 * @return current location of this node
	 */
	DimensionalCoord getLocation();

	/**
	 * @return Transparent, or a valid color, NULL IS NOT A VALID RETURN
	 */
	AEColor getGridColor();

	/**
	 * Notifies your IGridBlock that changes were made to your connections
	 */
	void onGridNotification(GridNotification notification);

	/**
	 * Update Blocks network/connection/booting status. grid,
	 *
	 * @param grid grid
	 * @param channelsInUse used channels
	 */
	public void setNetworkStatus(IGrid grid, int channelsInUse);

	/**
	 * Determine which sides of the block can be connected too, only used when isWorldAccessible returns true, not used
	 * for {@link IPart} implementations.
	 */
	EnumSet<ForgeDirection> getConnectableSides();

	/**
	 * @return the IGridHost for the node, this will be an IGridPart or a TileEntity generally speaking.
	 */
	IGridHost getMachine();

	/**
	 * called when the grid for the node has changed, the general grid state should not be trusted at this point.
	 */
	void gridChanged();

	/**
	 * Determines what item stack is used to render this node in the GUI.
	 * 
	 * @return the render item stack to use to render this node, null is valid, and will not show this node.
	 */
	public ItemStack getMachineRepresentation();
}
