package appeng.block.qnb;


import java.util.Set;

import net.minecraft.util.EnumFacing;


public class QnbFormedState
{

	private final Set<EnumFacing> connections;

	private final boolean corner;

	private final boolean powered;

	public QnbFormedState( Set<EnumFacing> connections, boolean corner, boolean powered )
	{
		this.connections = connections;
		this.corner = corner;
		this.powered = powered;
	}

	public Set<EnumFacing> getConnections()
	{
		return connections;
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
