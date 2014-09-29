package appeng.client.render.blocks;

import java.util.EnumSet;

import appeng.client.texture.FlippableIcon;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.OffsetIcon;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;

public class RenderMEChest extends BaseBlockRender
{

	public RenderMEChest() {
		super( false, 0 );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		Tessellator.instance.setBrightness( 0 );
		renderer.overrideBlockTexture = ExtraBlockTextures.getMissing();
		this.renderInvBlock( EnumSet.of( ForgeDirection.SOUTH ), block, is, Tessellator.instance, 0x000000, renderer );

		renderer.overrideBlockTexture = ExtraBlockTextures.MEChest.getIcon();
		this.renderInvBlock( EnumSet.of( ForgeDirection.UP ), block, is, Tessellator.instance, adjustBrightness( AEColor.Transparent.whiteVariant, 0.7 ),
				renderer );

		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileChest sp = imb.getTileEntity( world, x, y, z );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		ForgeDirection up = sp.getUp();
		ForgeDirection forward = sp.getForward();
		ForgeDirection west = Platform.crossProduct( forward, up );

		preRenderInWorld( imb, world, x, y, z, renderer );

		int stat = sp.getCellStatus( 0 );
		boolean result = renderer.renderStandardBlock( imb, x, y, z );

		selectFace( renderer, west, up, forward, 5, 16 - 5, 9, 12 );

		int offsetU = -4;
		int offsetV = 8;
		if ( stat == 0 )
			offsetV = 3;

		int b = world.getLightBrightnessForSkyBlocks( x + forward.offsetX, y + forward.offsetY, z + forward.offsetZ, 0 );
		Tessellator.instance.setBrightness( b );
		Tessellator.instance.setColorOpaque_I( 0xffffff );

		FlippableIcon flippableIcon = new FlippableIcon( new OffsetIcon( ExtraBlockTextures.MEStorageCellTextures.getIcon(), offsetU, offsetV ) );
		if ( forward == ForgeDirection.EAST && (up == ForgeDirection.NORTH || up == ForgeDirection.SOUTH) )
			flippableIcon.setFlip( true, false );
		else if ( forward == ForgeDirection.NORTH && up == ForgeDirection.EAST )
			flippableIcon.setFlip( false, true );
		else if ( forward == ForgeDirection.NORTH && up == ForgeDirection.WEST )
			flippableIcon.setFlip( true, false );
		else if ( forward == ForgeDirection.DOWN && up == ForgeDirection.EAST )
			flippableIcon.setFlip( false, true );
		else if ( forward == ForgeDirection.DOWN )
			flippableIcon.setFlip( true, false );

		/*
		 * 1.7.2
		 * 
		 * else if ( forward == ForgeDirection.EAST && up == ForgeDirection.UP ) flippableIcon.setFlip( true, false ); else if (
		 * forward == ForgeDirection.NORTH && up == ForgeDirection.UP ) flippableIcon.setFlip( true, false );
		 */

		renderFace( x, y, z, imb, flippableIcon, renderer, forward );

		if ( stat != 0 )
		{
			b = 0;
			if ( sp.isPowered() )
			{
				b = 15 << 20 | 15 << 4;
			}

			Tessellator.instance.setBrightness( b );
			if ( stat == 1 )
				Tessellator.instance.setColorOpaque_I( 0x00ff00 );
			if ( stat == 2 )
				Tessellator.instance.setColorOpaque_I( 0xffaa00 );
			if ( stat == 3 )
				Tessellator.instance.setColorOpaque_I( 0xff0000 );
			selectFace( renderer, west, up, forward, 9, 10, 11, 12 );
			renderFace( x, y, z, imb, ExtraBlockTextures.White.getIcon(), renderer, forward );
		}

		b = world.getLightBrightnessForSkyBlocks( x + up.offsetX, y + up.offsetY, z + up.offsetZ, 0 );
		if ( sp.isPowered() )
		{
			b = 15 << 20 | 15 << 4;
		}

		Tessellator.instance.setBrightness( b );
		Tessellator.instance.setColorOpaque_I( 0xffffff );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		ICellHandler ch = AEApi.instance().registries().cell().getHandler( sp.getStorageType() );

		Tessellator.instance.setColorOpaque_I( sp.getColor().whiteVariant );
		IIcon ico = ch == null ? null : ch.getTopTexture_Light();
		renderFace( x, y, z, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );

		if ( ico != null )
		{
			Tessellator.instance.setColorOpaque_I( sp.getColor().mediumVariant );
			ico = ch == null ? null : ch.getTopTexture_Medium();
			renderFace( x, y, z, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );

			Tessellator.instance.setColorOpaque_I( sp.getColor().blackVariant );
			ico = ch == null ? null : ch.getTopTexture_Dark();
			renderFace( x, y, z, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );
		}

		renderer.overrideBlockTexture = null;
		postRenderInWorld( renderer );

		return result;
	}
}
