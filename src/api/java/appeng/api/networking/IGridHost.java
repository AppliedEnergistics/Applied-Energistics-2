package appeng.api.networking;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.util.AECableType;

/**
 * 
 * Implement to create a networked {@link TileEntity} or {@link IPart} must
 * be implemented for a part, or tile entity to become part of a grid.
 * 
 */
public interface IGridHost
{

	/**
	 * get the grid node for a particular side of a block, you can return null,
	 * by returning a valid node later and calling updateState, you can join the
	 * Grid when your block is ready.
	 * 
	 * @param dir
	 *            feel free to ignore this, most blocks will use the same node
	 *            for every side.
	 * @return a new IGridNode, create these with
	 *         AEApi.instance().createGridNode( MyIGridBlock )
	 */
	public IGridNode getGridNode(ForgeDirection dir);

	/**
	 * Determines how cables render when they connect to this block. Priority is
	 * Smart &gt; Covered &gt; Glass
	 * 
	 * @param dir direction
	 */
	public AECableType getCableConnectionType(ForgeDirection dir);

	/**
	 * break this host, its violating security rules, just break your block, or part.
	 */
	public void securityBreak();

}
