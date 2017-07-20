
package appeng.block.qnb;


import java.util.Set;

import net.minecraft.util.EnumFacing;


public class QnbFormedState
{

	private final Set<EnumFacing> adjacentQuantumBridges;

	private final boolean corner;

	private final boolean powered;

	public QnbFormedState( Set<EnumFacing> adjacentQuantumBridges, boolean corner, boolean powered )
	{
		this.adjacentQuantumBridges = adjacentQuantumBridges;
		this.corner = corner;
		this.powered = powered;
	}

	public Set<EnumFacing> getAdjacentQuantumBridges()
	{
		return adjacentQuantumBridges;
	}

	public boolean isCorner()
	{
		return corner;
	}

	public boolean isPowered()
	{
		return powered;
	}

}
