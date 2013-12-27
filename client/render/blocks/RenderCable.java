package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;

public class RenderCable extends BaseBlockRender
{

	public void renderCableAt(double Thickness, IBlockAccess world, int x, int y, int z, AEBaseBlock block, RenderBlocks renderer, Icon texture, double pull,
			EnumSet<ForgeDirection> connections)
	{

		if ( connections.contains( ForgeDirection.UNKNOWN ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.5D - Thickness, 0.5D + Thickness, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.WEST ) )
		{
			renderer.setRenderBounds( 0.0D, 0.5D - Thickness, 0.5D - Thickness, 0.5D - Thickness - pull, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.EAST ) )
		{
			renderer.setRenderBounds( 0.5D + Thickness + pull, 0.5D - Thickness, 0.5D - Thickness, 1.0D, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.NORTH ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.0D, 0.5D + Thickness, 0.5D + Thickness, 0.5D - Thickness - pull );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.SOUTH ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.5D + Thickness + pull, 0.5D + Thickness, 0.5D + Thickness, 1.0D );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.DOWN ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.0D, 0.5D - Thickness, 0.5D + Thickness, 0.5D - Thickness - pull, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.UP ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D + Thickness + pull, 0.5D - Thickness, 0.5D + Thickness, 1.0D, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

	}
}
