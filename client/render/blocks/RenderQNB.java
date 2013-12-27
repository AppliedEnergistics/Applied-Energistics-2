package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.client.texture.ExtraTextures;
import appeng.tile.qnb.TileQuantumBridge;

public class RenderQNB extends RenderCable
{

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack item, RenderBlocks renderer)
	{
		float px = 2.0f / 16.0f;
		float maxpx = 14.0f / 16.0f;
		renderer.setRenderBounds( px, px, px, maxpx, maxpx, maxpx );

		super.renderInventory( block, item, renderer );
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
				EnumSet<ForgeDirection> sides = EnumSet.allOf( ForgeDirection.class );
				renderCableAt( 0.11D, world, x, y, z, block, renderer, block.getIcon( 0, 0 ), 0.141D, sides );
				renderCableAt( 0.188D, world, x, y, z, block, renderer, block.getIcon( 0, 0 ), 0.1875D,
						EnumSet.complementOf( EnumSet.of( ForgeDirection.UNKNOWN ) ) );
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
				// renderCableAt(0.11D, world, x, y, z, block, modelId, renderer,
				// AppEngTextureRegistry.Blocks.MECable.get(), true, 0.0D);
				renderCableAt( 0.188D, world, x, y, z, block, renderer, ExtraTextures.BlockInterfaceAlternate.getIcon(), 0.05D,
						EnumSet.complementOf( EnumSet.of( ForgeDirection.UNKNOWN ) ) );

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
						renderFace( x, y, z, block, block.getIcon( 0, 0 ), renderer, side );

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
						renderFace( x, y, z, block, block.getIcon( 0, 0 ), renderer, side );
				}
			}
		}

		renderer.renderAllFaces = false;
		return true;
	}

}
