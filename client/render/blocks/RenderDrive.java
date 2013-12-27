package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraTextures;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;

public class RenderDrive extends BaseBlockRender
{

	public RenderDrive() {
		super( false, 0 );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer)
	{
		renderer.overrideBlockTexture = ExtraTextures.getMissing();
		this.renderInvBlock( EnumSet.of( ForgeDirection.SOUTH ), block, Tessellator.instance, 0x000000, renderer );

		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileDrive sp = imb.getTileEntity( world, x, y, z );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		ForgeDirection up = sp.getUp();
		ForgeDirection forward = sp.getForward();
		ForgeDirection west = Platform.crossProduct( forward, up );

		boolean result = super.renderInWorld( imb, world, x, y, z, renderer );
		Tessellator tess = Tessellator.instance;

		Icon ico = ExtraTextures.MEStorageCellTextures.getIcon();

		int b = world.getLightBrightnessForSkyBlocks( x + forward.offsetX, y + forward.offsetY, z + forward.offsetZ, 0 );

		for (int yy = 0; yy < 5; yy++)
		{
			for (int xx = 0; xx < 2; xx++)
			{
				int stat = sp.getCellStatus( yy * 2 + (1 - xx) );
				selectFace( renderer, west, up, forward, 2 + xx * 7, 7 + xx * 7, 1 + yy * 3, 3 + yy * 3 );

				int spin = 0;

				switch (forward.offsetX + forward.offsetY * 2 + forward.offsetZ * 3)
				{
				case 1:
					switch (up)
					{
					case UP:
						spin = 3;
						break;
					case DOWN:
						spin = 1;
						break;
					case NORTH:
						spin = 0;
						break;
					case SOUTH:
						spin = 2;
						break;
					default:
					}
					break;
				case -1:
					switch (up)
					{
					case UP:
						spin = 1;
						break;
					case DOWN:
						spin = 3;
						break;
					case NORTH:
						spin = 0;
						break;
					case SOUTH:
						spin = 2;
						break;
					default:
					}
					break;
				case -2:
					switch (up)
					{
					case EAST:
						spin = 1;
						break;
					case WEST:
						spin = 3;
						break;
					case NORTH:
						spin = 2;
						break;
					case SOUTH:
						spin = 0;
						break;
					default:
					}
					break;
				case 2:
					switch (up)
					{
					case EAST:
						spin = 1;
						break;
					case WEST:
						spin = 3;
						break;
					case NORTH:
						spin = 0;
						break;
					case SOUTH:
						spin = 0;
						break;
					default:
					}
					break;
				case 3:
					switch (up)
					{
					case UP:
						spin = 2;
						break;
					case DOWN:
						spin = 0;
						break;
					case EAST:
						spin = 3;
						break;
					case WEST:
						spin = 1;
						break;
					default:
					}
					break;
				case -3:
					switch (up)
					{
					case UP:
						spin = 2;
						break;
					case DOWN:
						spin = 0;
						break;
					case EAST:
						spin = 1;
						break;
					case WEST:
						spin = 3;
						break;
					default:
					}
					break;
				}

				double u1 = ico.getInterpolatedU( (spin % 4 < 2) ? 1 : 6 );
				double u2 = ico.getInterpolatedU( ((spin + 1) % 4 < 2) ? 1 : 6 );
				double u3 = ico.getInterpolatedU( ((spin + 2) % 4 < 2) ? 1 : 6 );
				double u4 = ico.getInterpolatedU( ((spin + 3) % 4 < 2) ? 1 : 6 );

				int m = 1;
				int mx = 3;
				if ( stat == 0 )
				{
					m = 4;
					mx = 5;
				}

				double v1 = ico.getInterpolatedV( ((spin + 1) % 4 < 2) ? m : mx );
				double v2 = ico.getInterpolatedV( ((spin + 2) % 4 < 2) ? m : mx );
				double v3 = ico.getInterpolatedV( ((spin + 3) % 4 < 2) ? m : mx );
				double v4 = ico.getInterpolatedV( ((spin + 0) % 4 < 2) ? m : mx );

				tess.setBrightness( b );
				tess.setColorOpaque_I( 0xffffff );
				switch (forward.offsetX + forward.offsetY * 2 + forward.offsetZ * 3)
				{
				case 1:
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u1, v1 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u3, v3 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMinZ, u4, v4 );
					break;
				case -1:
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMinZ, u1, v1 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u2, v2 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u4, v4 );
					break;
				case -2:
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u1, v1 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMinZ, u4, v4 );
					break;
				case 2:
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMinZ, u1, v1 );
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u4, v4 );
					break;
				case 3:
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u1, v1 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMinY, z + renderer.renderMaxZ, u4, v4 );
					break;
				case -3:
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMinY, z + renderer.renderMaxZ, u1, v1 );
					tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
					tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u4, v4 );
					break;
				}

				if ( (forward == ForgeDirection.UP && up == ForgeDirection.SOUTH) || forward == ForgeDirection.DOWN )
					selectFace( renderer, west, up, forward, 3 + xx * 7, 4 + xx * 7, 1 + yy * 3, 2 + yy * 3 );
				else
					selectFace( renderer, west, up, forward, 5 + xx * 7, 6 + xx * 7, 2 + yy * 3, 3 + yy * 3 );

				if ( stat != 0 )
				{
					Icon wico = ExtraTextures.White.getIcon();
					u1 = wico.getInterpolatedU( (spin % 4 < 2) ? 1 : 6 );
					u2 = wico.getInterpolatedU( ((spin + 1) % 4 < 2) ? 1 : 6 );
					u3 = wico.getInterpolatedU( ((spin + 2) % 4 < 2) ? 1 : 6 );
					u4 = wico.getInterpolatedU( ((spin + 3) % 4 < 2) ? 1 : 6 );

					v1 = wico.getInterpolatedV( ((spin + 1) % 4 < 2) ? 1 : 3 );
					v2 = wico.getInterpolatedV( ((spin + 2) % 4 < 2) ? 1 : 3 );
					v3 = wico.getInterpolatedV( ((spin + 3) % 4 < 2) ? 1 : 3 );
					v4 = wico.getInterpolatedV( ((spin + 0) % 4 < 2) ? 1 : 3 );

					if ( sp.isPowered() )
						tess.setBrightness( 15 << 20 | 15 << 4 );
					else
						tess.setBrightness( 0 );

					if ( stat == 1 )
						Tessellator.instance.setColorOpaque_I( 0x00ff00 );
					if ( stat == 2 )
						Tessellator.instance.setColorOpaque_I( 0xffaa00 );
					if ( stat == 3 )
						Tessellator.instance.setColorOpaque_I( 0xff0000 );

					switch (forward.offsetX + forward.offsetY * 2 + forward.offsetZ * 3)
					{
					case 1:
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u1, v1 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u3, v3 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMinZ, u4, v4 );
						break;
					case -1:
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMinZ, u1, v1 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u2, v2 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u4, v4 );
						break;
					case -2:
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u1, v1 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMinZ, u4, v4 );
						break;
					case 2:
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMinZ, u1, v1 );
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMinZ, u4, v4 );
						break;
					case 3:
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u1, v1 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMinY, z + renderer.renderMaxZ, u4, v4 );
						break;
					case -3:
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMinY, z + renderer.renderMaxZ, u1, v1 );
						tess.addVertexWithUV( x + renderer.renderMinX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u2, v2 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMaxY, z + renderer.renderMaxZ, u3, v3 );
						tess.addVertexWithUV( x + renderer.renderMaxX, y + renderer.renderMinY, z + renderer.renderMaxZ, u4, v4 );
						break;
					}
				}
			}
		}

		renderer.overrideBlockTexture = null;
		return result;
	}
}
