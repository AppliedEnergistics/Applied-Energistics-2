package appeng.facade;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.RenderBlocksWorkaround;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.integration.abstraction.IBC;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FacadePart implements IFacadePart
{

	public final ItemStack facade;
	public final ForgeDirection side;
	public int thickness = 2;

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
	public void getBoxes(IPartCollsionHelper ch)
	{
		// the box is 15.9 for transition planes to pick up collision events.
		ch.addBox( 0.0, 0.0, 14, 16.0, 16.0, 15.9 );
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
		else if ( AppEng.instance.isIntegrationEnabled( "BC" ) )
		{
			IBC bc = (IBC) AppEng.instance.getIntegration( "BC" );
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

				if ( renderStilt && busBounds == null )
				{
					if ( renderer instanceof RenderBlocksWorkaround )
					{
						RenderBlocksWorkaround rbw = (RenderBlocksWorkaround) renderer;
						rbw.isFacade = false;
						rbw.calculations = true;
					}

					IIcon myIcon = null;
					if ( isBC() )
					{
						IBC bc = (IBC) AppEng.instance.getIntegration( "BC" );
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

						try
						{
							int color = ib.getColorFromItemStack( randomItem, 0 );
							Tessellator.instance.setColorOpaque_I( color );
						}
						catch (Throwable error)
						{
						}

						renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
						instance.setBounds( 0, 0, 16 - getFacadeThickness(), 16, 16, 16 );
						instance.prepareBounds( renderer );

						if ( renderer instanceof RenderBlocksWorkaround )
						{
							RenderBlocksWorkaround rbw = (RenderBlocksWorkaround) renderer;

							rbw.isFacade = true;

							rbw.calculations = true;
							rbw.faces = EnumSet.noneOf( ForgeDirection.class );

							rbw.renderStandardBlock( blk, x, y, z );

							rbw.calculations = false;
							rbw.faces = EnumSet.allOf( ForgeDirection.class );

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
							float r = (color >> 16 & 0xff) / 255F;
							float g = (color >> 8 & 0xff) / 255F;
							float b = (color & 0xff) / 255F;
							GL11.glColor4f( r, g, b, 1.0F );
						}
						catch (Throwable error)
						{
							GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0F );
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
		switch (side)
		{
		case DOWN:
			return AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, (getFacadeThickness()) / 16.0, 1.0 );
		case EAST:
			return AxisAlignedBB.getBoundingBox( (16.0 - getFacadeThickness()) / 16.0, 0.0, 0.0, 1.0, 1.0, 1.0 );
		case NORTH:
			return AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, (getFacadeThickness()) / 16.0 );
		case SOUTH:
			return AxisAlignedBB.getBoundingBox( 0.0, 0.0, (16.0 - getFacadeThickness()) / 16.0, 1.0, 1.0, 1.0 );
		case UP:
			return AxisAlignedBB.getBoundingBox( 0.0, (16.0 - getFacadeThickness()) / 16.0, 0.0, 1.0, 1.0, 1.0 );
		case WEST:
			return AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, (getFacadeThickness()) / 16.0, 1.0, 1.0 );
		default:
			break;

		}
		return AxisAlignedBB.getBoundingBox( 0, 0, 0, 1, 1, 1 );
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
}
