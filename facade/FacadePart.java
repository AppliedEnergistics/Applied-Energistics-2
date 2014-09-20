package appeng.facade;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.ISimplifiedBundle;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.RenderBlocksWorkaround;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBC;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FacadePart implements IFacadePart, IBoxProvider
{

	public final ItemStack facade;
	public final ForgeDirection side;
	public int thickness = 2;

	@SideOnly(Side.CLIENT)
	ISimplifiedBundle prevLight;

	public FacadePart(ItemStack facade, ForgeDirection side) {
		if ( facade == null )
			throw new RuntimeException( "Facade Part constructed on null item." );
		this.facade = facade.copy();
		this.facade.stackSize = 1;
		this.side = side;
	}

	@Override
	public ItemStack getItemStack()
	{
		return facade;
	}

	@Override
	public void getBoxes(IPartCollisionHelper ch, Entity e)
	{
		if ( e instanceof EntityLivingBase )
		{
			// prevent weird snagg behavior
			ch.addBox( 0.0, 0.0, 14, 16.0, 16.0, 16.0 );
		}
		else
		{
			// the box is 15.9 for transition planes to pick up collision events.
			ch.addBox( 0.0, 0.0, 14, 16.0, 16.0, 15.9 );
		}
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		getBoxes( bch, null );

	}

	public static boolean isFacade(ItemStack is)
	{
		if ( is.getItem() instanceof IFacadeItem )
			return true;
		return false;
	}

	ItemStack getTexture()
	{
		if ( facade.getItem() instanceof IFacadeItem )
		{
			IFacadeItem fi = (IFacadeItem) facade.getItem();
			return fi.getTextureItem( facade );
		}
		else if ( AppEng.instance.isIntegrationEnabled( IntegrationType.BC ) )
		{
			IBC bc = (IBC) AppEng.instance.getIntegration( IntegrationType.BC );
			return bc.getTextureForFacade( facade );
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper instance2, RenderBlocks renderer, IFacadeContainer fc, AxisAlignedBB busBounds,
			boolean renderStilt)
	{
		if ( facade != null )
		{
			BusRenderHelper instance = (BusRenderHelper) instance2;

			try
			{
				ItemStack randomItem = getTexture();

				RenderBlocksWorkaround rbw = null;
				if ( renderer instanceof RenderBlocksWorkaround )
				{
					rbw = (RenderBlocksWorkaround) renderer;
				}

				if ( renderStilt && busBounds == null )
				{
					if ( rbw != null )
					{
						rbw.isFacade = false;
						rbw.calculations = true;
					}

					IIcon myIcon = null;
					if ( isBC() )
					{
						IBC bc = (IBC) AppEng.instance.getIntegration( IntegrationType.BC );
						myIcon = bc.getFacadeTexture();
					}

					if ( myIcon == null )
						myIcon = facade.getIconIndex();

					instance.setTexture( myIcon );

					if ( isBC() )
						instance.setBounds( 6, 6, 10, 10, 10, 15 );
					else
						instance.setBounds( 7, 7, 10, 9, 9, 15 );

					instance.renderBlock( x, y, z, renderer );
					instance.setTexture( null );

				}

				if ( randomItem != null )
				{
					if ( randomItem.getItem() instanceof ItemBlock )
					{
						ItemBlock ib = (ItemBlock) randomItem.getItem();
						Block blk = Block.getBlockFromItem( ib );

						if ( AEApi.instance().partHelper().getCableRenderMode().transparentFacades )
						{
							if ( rbw != null )
								rbw.opacity = 0.3f;
							instance.renderForPass( 1 );
						}
						else
						{
							if ( blk.canRenderInPass( 1 ) )
							{
								instance.renderForPass( 1 );
							}
						}

						int color = 0xffffff;

						try
						{
							color = ib.getColorFromItemStack( randomItem, 0 );
						}
						catch (Throwable error)
						{
						}

						renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
						instance.setBounds( 0, 0, 16 - getFacadeThickness(), 16, 16, 16 );
						instance.prepareBounds( renderer );

						if ( rbw != null )
						{
							rbw.isFacade = true;

							rbw.calculations = true;
							rbw.faces = EnumSet.noneOf( ForgeDirection.class );

							if ( prevLight != null && rbw.similarLighting( blk, rbw.blockAccess, x, y, z, prevLight ) )
								rbw.populate( prevLight );
							else
							{
								instance.setRenderColor( color );
								rbw.renderStandardBlock( instance.getBlock(), x, y, z );
								instance.setRenderColor( 0xffffff );
								prevLight = rbw.getLightingCache();
							}

							rbw.calculations = false;
							rbw.faces = calculateFaceOpenFaces( rbw.blockAccess, fc, x, y, z, side );

							((RenderBlocksWorkaround) renderer).setTexture(
									blk.getIcon( ForgeDirection.DOWN.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.UP.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.NORTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.SOUTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.WEST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.EAST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ) );
						}
						else
						{
							instance.setTexture( blk.getIcon( ForgeDirection.DOWN.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.UP.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.NORTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.SOUTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.WEST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ),
									blk.getIcon( ForgeDirection.EAST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ) );
						}

						if ( busBounds == null )
						{
							if ( side == ForgeDirection.UP || side == ForgeDirection.DOWN )
							{
								instance.renderBlockCurrentBounds( x, y, z, renderer );
							}
							else if ( side == ForgeDirection.NORTH || side == ForgeDirection.SOUTH )
							{
								if ( fc.getFacade( ForgeDirection.UP ) != null )
									renderer.renderMaxY -= getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.DOWN ) != null )
									renderer.renderMinY += getFacadeThickness() / 16.0;

								instance.renderBlockCurrentBounds( x, y, z, renderer );
							}
							else
							{
								if ( fc.getFacade( ForgeDirection.UP ) != null )
									renderer.renderMaxY -= getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.DOWN ) != null )
									renderer.renderMinY += getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.SOUTH ) != null )
									renderer.renderMaxZ -= getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.NORTH ) != null )
									renderer.renderMinZ += getFacadeThickness() / 16.0;

								instance.renderBlockCurrentBounds( x, y, z, renderer );
							}
						}
						else
						{
							if ( side == ForgeDirection.UP || side == ForgeDirection.DOWN )
							{
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.maxZ, 1.0, 1.0, 1.0 );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, 0.0, 1.0, 1.0, busBounds.minZ );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.minZ, busBounds.minX, 1.0, busBounds.maxZ );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.maxX, 0.0, busBounds.minZ, 1.0, 1.0, busBounds.maxZ );
							}
							else if ( side == ForgeDirection.NORTH || side == ForgeDirection.SOUTH )
							{
								if ( fc.getFacade( ForgeDirection.UP ) != null )
									renderer.renderMaxY -= getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.DOWN ) != null )
									renderer.renderMinY += getFacadeThickness() / 16.0;

								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.maxX, 0.0, 0.0, 1.0, 1.0, 1.0 );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, 0.0, busBounds.minX, 1.0, 1.0 );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.minX, 0.0, 0.0, busBounds.maxX, busBounds.minY, 1.0 );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.minX, busBounds.maxY, 0.0, busBounds.maxX, 1.0, 1.0 );
							}
							else
							{
								if ( fc.getFacade( ForgeDirection.UP ) != null )
									renderer.renderMaxY -= getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.DOWN ) != null )
									renderer.renderMinY += getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.SOUTH ) != null )
									renderer.renderMaxZ -= getFacadeThickness() / 16.0;

								if ( fc.getFacade( ForgeDirection.NORTH ) != null )
									renderer.renderMinZ += getFacadeThickness() / 16.0;

								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.maxZ, 1.0, 1.0, 1.0 );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, 0.0, 1.0, 1.0, busBounds.minZ );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.minZ, 1.0, busBounds.minY, busBounds.maxZ );
								renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, busBounds.maxY, busBounds.minZ, 1.0, 1.0, busBounds.maxZ );
							}
						}

						if ( rbw != null )
						{
							rbw.opacity = 1.0f;
							rbw.faces = EnumSet.allOf( ForgeDirection.class );
						}

						instance.renderForPass( 0 );
						instance.setTexture( null );
						Tessellator.instance.setColorOpaque_F( 1, 1, 1 );

						return;
					}
				}
			}
			catch (Throwable t)
			{
				AELog.error( t );

			}

			return;
		}
	}

	private EnumSet<ForgeDirection> calculateFaceOpenFaces(IBlockAccess blockAccess, IFacadeContainer fc, int x, int y, int z, ForgeDirection side)
	{
		EnumSet<ForgeDirection> out = EnumSet.of( side, side.getOpposite() );
		IFacadePart facade = fc.getFacade( side );

		for (ForgeDirection it : ForgeDirection.VALID_DIRECTIONS)
		{
			if ( !out.contains( it ) && alphaDiff( blockAccess.getTileEntity( x + it.offsetX, y + it.offsetY, z + it.offsetZ ), side, facade ) )
			{
				out.add( it );
			}
		}

		if ( out.contains( ForgeDirection.UP ) && (side.offsetX != 0 || side.offsetZ != 0) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.UP );
			if ( fp != null && (fp.isTransparent() == facade.isTransparent()) )
				out.remove( ForgeDirection.UP );
		}

		if ( out.contains( ForgeDirection.DOWN ) && (side.offsetX != 0 || side.offsetZ != 0) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.DOWN );
			if ( fp != null && (fp.isTransparent() == facade.isTransparent()) )
				out.remove( ForgeDirection.DOWN );
		}

		if ( out.contains( ForgeDirection.SOUTH ) && (side.offsetX != 0) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.SOUTH );
			if ( fp != null && (fp.isTransparent() == facade.isTransparent()) )
				out.remove( ForgeDirection.SOUTH );
		}

		if ( out.contains( ForgeDirection.NORTH ) && (side.offsetX != 0) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.NORTH );
			if ( fp != null && (fp.isTransparent() == facade.isTransparent()) )
				out.remove( ForgeDirection.NORTH );
		}

		/*
		 * if ( out.contains( ForgeDirection.EAST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.EAST ); }
		 * 
		 * if ( out.contains( ForgeDirection.WEST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.WEST ); }
		 * 
		 * if ( out.contains( ForgeDirection.NORTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.NORTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.NORTH ); }
		 * 
		 * if ( out.contains( ForgeDirection.SOUTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.SOUTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.SOUTH ); }
		 * 
		 * if ( out.contains( ForgeDirection.EAST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.EAST ); }
		 * 
		 * if ( out.contains( ForgeDirection.WEST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.WEST ); }
		 */
		return out;
	}

	private boolean alphaDiff(TileEntity tileEntity, ForgeDirection side, IFacadePart facade)
	{
		if ( tileEntity instanceof IPartHost )
		{
			IPartHost ph = (IPartHost) tileEntity;
			IFacadePart fp = ph.getFacadeContainer().getFacade( side );

			return fp == null || (fp.isTransparent() != facade.isTransparent());
		}

		return true;
	}

	@SideOnly(Side.CLIENT)
	private void renderSegmentBlockCurrentBounds(BusRenderHelper instance, int x, int y, int z, RenderBlocks renderer, double minX, double minY, double minZ,
			double maxX, double maxY, double maxZ)
	{
		double oldMinX = renderer.renderMinX;
		double oldMinY = renderer.renderMinY;
		double oldMinZ = renderer.renderMinZ;
		double oldMaxX = renderer.renderMaxX;
		double oldMaxY = renderer.renderMaxY;
		double oldMaxZ = renderer.renderMaxZ;

		renderer.renderMinX = Math.max( renderer.renderMinX, minX );
		renderer.renderMinY = Math.max( renderer.renderMinY, minY );
		renderer.renderMinZ = Math.max( renderer.renderMinZ, minZ );
		renderer.renderMaxX = Math.min( renderer.renderMaxX, maxX );
		renderer.renderMaxY = Math.min( renderer.renderMaxY, maxY );
		renderer.renderMaxZ = Math.min( renderer.renderMaxZ, maxZ );

		// don't draw it if its not at least a pixel wide...
		if ( renderer.renderMaxX - renderer.renderMinX >= 1.0 / 16.0 && renderer.renderMaxY - renderer.renderMinY >= 1.0 / 16.0
				&& renderer.renderMaxZ - renderer.renderMinZ >= 1.0 / 16.0 )
		{
			instance.renderBlockCurrentBounds( x, y, z, renderer );
		}

		renderer.renderMinX = oldMinX;
		renderer.renderMinY = oldMinY;
		renderer.renderMinZ = oldMinZ;
		renderer.renderMaxX = oldMaxX;
		renderer.renderMaxY = oldMaxY;
		renderer.renderMaxZ = oldMaxZ;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper instance, RenderBlocks renderer)
	{
		if ( facade != null )
		{
			IFacadeItem fi = (IFacadeItem) facade.getItem();

			try
			{
				ItemStack randomItem = fi.getTextureItem( facade );

				instance.setTexture( facade.getIconIndex() );
				instance.setBounds( 7, 7, 4, 9, 9, 14 );
				instance.renderInventoryBox( renderer );
				instance.setTexture( null );

				if ( randomItem != null )
				{
					if ( randomItem.getItem() instanceof ItemBlock )
					{
						ItemBlock ib = (ItemBlock) randomItem.getItem();
						Block blk = Block.getBlockFromItem( ib );

						try
						{
							int color = ib.getColorFromItemStack( randomItem, 0 );
							GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0F );
							instance.setInvColor( color );
						}
						catch (Throwable error)
						{
							GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0F );
							instance.setInvColor( 0xffffff );
						}

						Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
						Tessellator.instance.setColorOpaque_F( 1, 1, 1 );
						instance.setTexture( blk.getIcon( side.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ) );

						instance.setBounds( 0, 0, 14, 16, 16, 16 );
						instance.renderInventoryBox( renderer );

						instance.setTexture( null );

						return;
					}
				}
			}
			catch (Throwable t)
			{

			}

			return;
		}
	}

	@Override
	public ForgeDirection getSide()
	{
		return side;
	}

	public int getFacadeThickness()
	{
		return thickness;
	}

	@Override
	public AxisAlignedBB getPrimaryBox()
	{
		return Platform.getPrimaryBox( side, getFacadeThickness() );
	}

	@Override
	public Item getItem()
	{
		ItemStack is = getTexture();
		if ( is == null )
			return null;
		return is.getItem();
	}

	@Override
	public int getItemDamage()
	{
		ItemStack is = getTexture();
		if ( is == null )
			return 0;
		return is.getItemDamage();
	}

	@Override
	public boolean isBC()
	{
		return !(facade.getItem() instanceof IFacadeItem);
	}

	@Override
	public void setThinFacades(boolean useThinFacades)
	{
		thickness = useThinFacades ? 1 : 2;
	}

	@Override
	public boolean isTransparent()
	{
		if ( AEApi.instance().partHelper().getCableRenderMode().transparentFacades )
			return true;

		ItemStack is = getTexture();
		Block blk = Block.getBlockFromItem( is.getItem() );
		if ( !blk.isOpaqueCube() )
			return true;

		return false;
	}

}
