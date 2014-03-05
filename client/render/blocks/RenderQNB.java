package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraTextures;
import appeng.tile.qnb.TileQuantumBridge;

public class RenderQNB extends BaseBlockRender
{

	public void renderCableAt(double Thickness, IBlockAccess world, int x, int y, int z, AEBaseBlock block, RenderBlocks renderer, IIcon texture, double pull,
			EnumSet<ForgeDirection> connections)
	{
		block.getRendererInstance().setTemporaryRenderIcon( texture );

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

		block.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack item, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		float px = 2.0f / 16.0f;
		float maxpx = 14.0f / 16.0f;
		renderer.setRenderBounds( px, px, px, maxpx, maxpx, maxpx );

		super.renderInventory( block, item, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileQuantumBridge tqb = block.getTileEntity( world, x, y, z );
		if ( tqb == null )
			return false;

		renderer.renderAllFaces = true;

		if ( tqb.getBlockType() == AEApi.instance().blocks().blockQuantumLink.block() )
		{
			if ( tqb.isFormed() )
			{
				AEColoredItemDefinition cabldef = AEApi.instance().parts().partCableGlass;
				Item cable = cabldef.item( AEColor.Transparent );

				AEColoredItemDefinition ccabldef = AEApi.instance().parts().partCableCovered;
				Item ccable = ccabldef.item( AEColor.Transparent );

				EnumSet<ForgeDirection> sides = tqb.getConnections();
				renderCableAt( 0.11D, world, x, y, z, block, renderer, cable.getIconIndex( cabldef.stack( AEColor.Transparent, 1 ) ), 0.141D, sides );
				renderCableAt( 0.188D, world, x, y, z, block, renderer, ccable.getIconIndex( ccabldef.stack( AEColor.Transparent, 1 ) ), 0.1875D,
						tqb.getConnections() );
			}

			float px = 2.0f / 16.0f;
			float maxpx = 14.0f / 16.0f;
			renderer.setRenderBounds( px, px, px, maxpx, maxpx, maxpx );
			renderer.renderStandardBlock( block, x, y, z );
			// super.renderWorldBlock(world, x, y, z, block, modelId, renderer);
		}
		else
		{
			if ( !tqb.isFormed() )
			{
				float px = 2.0f / 16.0f;
				float maxpx = 14.0f / 16.0f;
				renderer.setRenderBounds( px, px, px, maxpx, maxpx, maxpx );
				renderer.renderStandardBlock( block, x, y, z );
			}
			else if ( tqb.isCorner() )
			{
				// renderCableAt(0.11D, world, x, y, z, block, modelId,
				// renderer,
				// AppEngTextureRegistry.Blocks.MECable.get(), true, 0.0D);
				AEColoredItemDefinition ccabldef = AEApi.instance().parts().partCableCovered;
				Item ccable = ccabldef.item( AEColor.Transparent );

				renderCableAt( 0.188D, world, x, y, z, block, renderer, ccable.getIconIndex( ccabldef.stack( AEColor.Transparent, 1 ) ), 0.05D,
						tqb.getConnections() );

				float px = 4.0f / 16.0f;
				float maxpx = 12.0f / 16.0f;

				renderer.setRenderBounds( px, px, px, maxpx, maxpx, maxpx );
				renderer.renderStandardBlock( block, x, y, z );

				if ( tqb.isPowered() )
				{

					px = 3.9f / 16.0f;
					maxpx = 12.1f / 16.0f;
					renderer.setRenderBounds( px, px, px, maxpx, maxpx, maxpx );

					int bn = 15;
					Tessellator.instance.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
					Tessellator.instance.setBrightness( bn << 20 | bn << 4 );
					for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
						renderFace( x, y, z, block, ExtraTextures.BlockQRingCornerLight.getIcon(), renderer, side );

				}
			}
			else
			{
				float px = 2.0f / 16.0f;
				float maxpx = 14.0f / 16.0f;
				renderer.setRenderBounds( 0, px, px, 1, maxpx, maxpx );
				renderer.renderStandardBlock( block, x, y, z );

				renderer.setRenderBounds( px, 0, px, maxpx, 1, maxpx );
				renderer.renderStandardBlock( block, x, y, z );

				renderer.setRenderBounds( px, px, 0, maxpx, maxpx, 1 );
				renderer.renderStandardBlock( block, x, y, z );

				if ( tqb.isPowered() )
				{
					px = -0.01f / 16.0f;
					maxpx = 16.01f / 16.0f;
					renderer.setRenderBounds( px, px, px, maxpx, maxpx, maxpx );

					int bn = 15;
					Tessellator.instance.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
					Tessellator.instance.setBrightness( bn << 20 | bn << 4 );
					for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
						renderFace( x, y, z, block, ExtraTextures.BlockQRingEdgeLight.getIcon(), renderer, side );
				}
			}
		}

		renderer.renderAllFaces = false;
		return true;
	}
}
