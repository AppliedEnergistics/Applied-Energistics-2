package appeng.client.render.crafting;


import java.util.EnumSet;

import net.minecraft.util.EnumFacing;


/**
 * Transports the rendering state for a block of a crafting cube.
 */
public final class CraftingCubeState
{

	// Contains information on which sides of the block are connected to other parts of a formed crafting cube
	private final EnumSet<EnumFacing> connections;

	public CraftingCubeState( EnumSet<EnumFacing> connections )
	{
		this.connections = connections;
	}

	public EnumSet<EnumFacing> getConnections()
	{
		return connections;
	}
}
