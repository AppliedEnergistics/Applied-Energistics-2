package appeng.parts;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.CableRenderHelper;
import appeng.facade.FacadeContainer;
import appeng.helpers.AEMultiTile;
import appeng.me.GridConnection;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CableBusContainer implements AEMultiTile, ICableBusContainer
{

	private IPartCable center;
	private IPart sides[] = new IPart[6];
	private FacadeContainer fc = new FacadeContainer();

	public boolean hasRedstone = false;
	public IPartHost tcb;

	boolean inWorld = false;
	public boolean requiresDynamicRender = false;

	public void setHost(IPartHost host)
	{
		tcb.clearContainer();
		tcb = host;
	}

	public CableBusContainer(IPartHost host) {
		tcb = host;
	}

	@Override
	public IPart getPart(ForgeDirection side)
	{
		if ( side == ForgeDirection.UNKNOWN )
			return center;
		return sides[side.ordinal()];
	}

	public void updateDynamicRender()
	{
		requiresDynamicRender = false;
		for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS)
		{
			IPart p = getPart( s );
			if ( p != null )
				requiresDynamicRender = requiresDynamicRender || p.requireDynamicRender();
		}
	}

	@Override
	public void removePart(ForgeDirection side, boolean supressUpdate)
	{
		if ( side == ForgeDirection.UNKNOWN )
		{
			if ( center != null )
				center.removeFromWorld();
			center = null;
		}
		else
		{
			if ( sides[side.ordinal()] != null )
				sides[side.ordinal()].removeFromWorld();
			sides[side.ordinal()] = null;
		}

		if ( !supressUpdate )
		{
			updateDynamicRender();
			updateConnections();
			markForUpdate();
			PartChanged();
		}
	}

	/**
	 * use for FMP
	 */
	public void updateConnections()
	{
		if ( center != null )
		{
			EnumSet<ForgeDirection> sides = EnumSet.allOf( ForgeDirection.class );

			for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS)
			{
				if ( getPart( s ) != null || isBlocked( s ) )
					sides.remove( s );
			}

			center.setValidSides( sides );
			IGridNode n = center.getGridNode();
			if ( n != null )
				n.updateState();
		}
	}

	@Override
	public boolean canAddPart(ItemStack is, ForgeDirection side)
	{
		if ( is.getItem() instanceof IPartItem )
		{
			IPartItem bi = (IPartItem) is.getItem();

			is = is.copy();
			is.stackSize = 1;

			IPart bp = bi.createPartFromItemStack( is );
			if ( bp instanceof IPartCable )
			{
				boolean canPlace = true;
				for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
					if ( getPart( d ) != null && !getPart( d ).canBePlacedOn( ((IPartCable) bp).supportsBuses() ) )
						canPlace = false;

				if ( !canPlace )
					return false;

				return getPart( ForgeDirection.UNKNOWN ) == null;
			}
			else if ( !(bp instanceof IPartCable) && side != ForgeDirection.UNKNOWN )
			{
				IPart cable = getPart( ForgeDirection.UNKNOWN );
				if ( cable != null && !bp.canBePlacedOn( ((IPartCable) cable).supportsBuses() ) )
					return false;

				return getPart( side ) == null;
			}
		}
		return false;
	}

	@Override
	public ForgeDirection addPart(ItemStack is, ForgeDirection side, EntityPlayer player)
	{
		if ( canAddPart( is, side ) )
		{
			if ( is.getItem() instanceof IPartItem )
			{
				IPartItem bi = (IPartItem) is.getItem();

				is = is.copy();
				is.stackSize = 1;

				IPart bp = bi.createPartFromItemStack( is );
				if ( bp instanceof IPartCable )
				{
					boolean canPlace = true;
					for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
						if ( getPart( d ) != null && !getPart( d ).canBePlacedOn( ((IPartCable) bp).supportsBuses() ) )
							canPlace = false;

					if ( !canPlace )
						return null;

					if ( getPart( ForgeDirection.UNKNOWN ) != null )
						return null;

					center = (IPartCable) bp;
					bp.setPartHostInfo( ForgeDirection.UNKNOWN, this, tcb.getTile() );

					if ( player != null )
						bp.onPlacement( player, is, side );

					if ( inWorld )
						bp.addToWorld();

					IGridNode cn = center.getGridNode();
					if ( cn != null )
					{
						for (ForgeDirection ins : ForgeDirection.VALID_DIRECTIONS)
						{
							IPart sbp = getPart( ins );
							if ( sbp != null )
							{
								IGridNode sn = sbp.getGridNode();
								if ( sn != null && cn != null )
								{
									try
									{
										new GridConnection( (IGridNode) cn, (IGridNode) sn, ForgeDirection.UNKNOWN );
									}
									catch (FailedConnection e)
									{
										// ekk!

										bp.removeFromWorld();
										center = null;
										return null;
									}
								}
							}
						}
					}

					updateConnections();
					markForUpdate();
					PartChanged();
					return ForgeDirection.UNKNOWN;
				}
				else if ( !(bp instanceof IPartCable) && side != ForgeDirection.UNKNOWN )
				{
					IPart cable = getPart( ForgeDirection.UNKNOWN );
					if ( cable != null && !bp.canBePlacedOn( ((IPartCable) cable).supportsBuses() ) )
						return null;

					sides[side.ordinal()] = bp;
					bp.setPartHostInfo( side, this, this.getTile() );

					if ( player != null )
						bp.onPlacement( player, is, side );

					if ( inWorld )
						bp.addToWorld();

					if ( center != null )
					{
						IGridNode cn = center.getGridNode();
						IGridNode sn = bp.getGridNode();

						if ( cn != null && sn != null )
						{
							try
							{
								new GridConnection( (IGridNode) cn, (IGridNode) sn, ForgeDirection.UNKNOWN );
							}
							catch (FailedConnection e)
							{
								// ekk!

								bp.removeFromWorld();
								sides[side.ordinal()] = null;
								return null;
							}
						}
					}

					updateDynamicRender();
					updateConnections();
					markForUpdate();
					PartChanged();
					return side;
				}
			}
		}
		return null;
	}

	public void addToWorld()
	{
		if ( inWorld )
			return;
		inWorld = true;

		TileEntity te = getTile();
		hasRedstone = te.getWorldObj().isBlockIndirectlyGettingPowered( te.xCoord, te.yCoord, te.zCoord );

		// start with the center, then install the side parts into the grid.
		for (int x = 6; x >= 0; x--)
		{
			ForgeDirection s = ForgeDirection.getOrientation( x );
			IPart part = getPart( s );

			if ( part != null )
			{
				part.setPartHostInfo( s, this, te );
				part.addToWorld();

				if ( s != ForgeDirection.UNKNOWN )
				{
					IGridNode sn = part.getGridNode();
					if ( sn != null )
					{
						// this is a really stupid if statement, why was this
						// here?
						// if ( !sn.getConnections().iterator().hasNext() )

						IPart center = getPart( ForgeDirection.UNKNOWN );
						if ( center != null )
						{
							IGridNode cn = center.getGridNode();
							if ( cn != null )
							{
								try
								{
									AEApi.instance().createGridConnection( cn, sn );
								}
								catch (FailedConnection e)
								{
									// ekk
								}
							}
						}

					}
				}
			}
		}
	}

	public void removeFromWorld()
	{
		if ( !inWorld )
			return;
		inWorld = false;

		for (ForgeDirection s : ForgeDirection.values())
		{
			IPart part = getPart( s );
			if ( part != null )
				part.removeFromWorld();
		}
	}

	public boolean canConnectRedstone(EnumSet<ForgeDirection> enumSet)
	{
		for (ForgeDirection dir : enumSet)
		{
			IPart part = getPart( dir );
			if ( part != null && part.canConnectRedstone() )
				return true;
		}
		return false;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection side)
	{
		IPart part = getPart( side );
		if ( part != null )
		{
			IGridNode n = part.getExternalFacingNode();
			if ( n != null )
				return n;
		}

		if ( center != null )
			return center.getGridNode();

		return null;
	}

	public Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(boolean ignoreCableConnections, Entity e, boolean visual)
	{
		List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

		for (ForgeDirection s : ForgeDirection.values())
		{
			IPartCollsionHelper bch = new BusCollisionHelper( boxes, s, e, visual );

			IPart part = getPart( s );
			if ( part != null )
			{
				if ( ignoreCableConnections && part instanceof IPartCable )
					bch.addBox( 6.0, 6.0, 6.0, 10.0, 10.0, 10.0 );
				else
					part.getBoxes( bch );

			}

			if ( !ignoreCableConnections && s != null && s != ForgeDirection.UNKNOWN )
			{
				IFacadePart fp = fc.getFacade( s );
				if ( fp != null )
					fp.getBoxes( bch );
			}
		}

		return boxes;
	}

	public void onEntityCollision(Entity entity)
	{
		for (ForgeDirection s : ForgeDirection.values())
		{
			IPart part = getPart( s );
			if ( part != null )
				part.onEntityCollision( entity );
		}
	}

	public boolean isEmpty()
	{
		for (ForgeDirection s : ForgeDirection.values())
		{
			IPart part = getPart( s );
			if ( part != null )
				return false;

			if ( s != ForgeDirection.UNKNOWN )
			{
				IFacadePart fp = fc.getFacade( s );
				if ( fp != null )
					return false;
			}
		}
		return true;
	}

	public void onNeighborChanged()
	{
		TileEntity te = getTile();
		hasRedstone = te.getWorldObj().isBlockIndirectlyGettingPowered( te.xCoord, te.yCoord, te.zCoord );

		for (ForgeDirection s : ForgeDirection.values())
		{
			IPart part = getPart( s );
			if ( part != null )
				part.onNeighborChanged();
		}
	}

	public boolean isSolidOnSide(ForgeDirection side)
	{
		if ( side == null || side == ForgeDirection.UNKNOWN )
			return false;

		// facades are solid..
		IFacadePart fp = fc.getFacade( side );
		if ( fp != null )
			return true;

		// buses can be too.
		IPart part = getPart( side );
		return part != null && part.isSolid();
	}

	public int isProvidingWeakPower(ForgeDirection side)
	{
		IPart part = getPart( side );
		return part != null ? part.isProvidingWeakPower() : 0;
	}

	public int isProvidingStrongPower(ForgeDirection side)
	{
		IPart part = getPart( side );
		return part != null ? part.isProvidingStrongPower() : 0;
	}

	public void setSide(ForgeDirection s)
	{
		switch (s)
		{
		case DOWN:
			BusRenderHelper.instance.ax = ForgeDirection.EAST;
			BusRenderHelper.instance.ay = ForgeDirection.NORTH;
			BusRenderHelper.instance.az = ForgeDirection.DOWN;
			break;
		case UP:
			BusRenderHelper.instance.ax = ForgeDirection.EAST;
			BusRenderHelper.instance.ay = ForgeDirection.SOUTH;
			BusRenderHelper.instance.az = ForgeDirection.UP;
			break;
		case EAST:
			BusRenderHelper.instance.ax = ForgeDirection.SOUTH;
			BusRenderHelper.instance.ay = ForgeDirection.UP;
			BusRenderHelper.instance.az = ForgeDirection.EAST;
			break;
		case WEST:
			BusRenderHelper.instance.ax = ForgeDirection.NORTH;
			BusRenderHelper.instance.ay = ForgeDirection.UP;
			BusRenderHelper.instance.az = ForgeDirection.WEST;
			break;
		case NORTH:
			BusRenderHelper.instance.ax = ForgeDirection.WEST;
			BusRenderHelper.instance.ay = ForgeDirection.UP;
			BusRenderHelper.instance.az = ForgeDirection.NORTH;
			break;
		case SOUTH:
			BusRenderHelper.instance.ax = ForgeDirection.EAST;
			BusRenderHelper.instance.ay = ForgeDirection.UP;
			BusRenderHelper.instance.az = ForgeDirection.SOUTH;
			break;
		case UNKNOWN:
		default:
			BusRenderHelper.instance.ax = ForgeDirection.EAST;
			BusRenderHelper.instance.ay = ForgeDirection.UP;
			BusRenderHelper.instance.az = ForgeDirection.SOUTH;
			break;

		}
	}

	@SideOnly(Side.CLIENT)
	public void renderStatic(double x, double y, double z)
	{
		CableRenderHelper.getInstance().renderStatic( this, fc );
	}

	@SideOnly(Side.CLIENT)
	public void renderDynamic(double x, double y, double z)
	{
		CableRenderHelper.getInstance().renderDynamic( this, x, y, z );
	}

	public void writeToStream(ByteBuf data) throws IOException
	{
		int sides = 0;
		for (int x = 0; x < 7; x++)
		{
			IPart p = getPart( ForgeDirection.getOrientation( x ) );
			if ( p != null )
			{
				sides = sides | (1 << x);
			}
		}

		data.writeByte( (byte) sides );

		for (int x = 0; x < 7; x++)
		{
			ItemStack is = null;
			IPart p = getPart( ForgeDirection.getOrientation( x ) );
			if ( p != null )
			{
				is = p.getItemStack( PartItemStack.Network );
				is.setTagCompound( null );

				data.writeShort( Item.getIdFromItem( is.getItem() ) );
				data.writeShort( is.getItemDamage() );

				if ( p != null )
					p.writeToStream( data );
			}
		}

		fc.writeToStream( data );
	}

	public boolean readFromStream(ByteBuf data) throws IOException
	{
		byte sides = data.readByte();

		for (int x = 0; x < 7; x++)
		{
			ForgeDirection side = ForgeDirection.getOrientation( x );
			if ( ((sides & (1 << x)) == (1 << x)) )
			{
				IPart p = getPart( side );

				short itemID = data.readShort();
				short dmgValue = data.readShort();

				Item myItem = Item.getItemById( itemID );

				ItemStack current = p != null ? p.getItemStack( PartItemStack.Network ) : null;
				if ( current != null && current.getItem() == myItem && current.getItemDamage() == dmgValue )
					p.readFromStream( data );
				else
				{
					removePart( side, false );
					side = addPart( new ItemStack( myItem, 1, dmgValue ), side, null );
					if ( side != null )
					{
						p = getPart( side );
						p.readFromStream( data );
					}
					else
						throw new RuntimeException( "Invalid Stream For CableBus Container." );
				}
			}
			else
				removePart( side, false );
		}

		return fc.readFromStream( data );
	}

	ForgeDirection getSide(IPart part)
	{
		if ( center == part )
			return ForgeDirection.UNKNOWN;
		else
		{
			for (int x = 0; x < 6; x++)
				if ( sides[x] == part )
				{
					return ForgeDirection.getOrientation( x );
				}
		}
		throw new RuntimeException( "Uhh Bad Part on Side." );
	}

	public void writeToNBT(NBTTagCompound data)
	{
		for (ForgeDirection s : ForgeDirection.values())
		{
			fc.writeToNBT( data );

			IPart part = getPart( s );
			if ( part != null )
			{
				NBTTagCompound def = new NBTTagCompound();
				part.getItemStack( PartItemStack.World ).writeToNBT( def );

				NBTTagCompound extra = new NBTTagCompound();
				part.writeToNBT( extra );

				data.setTag( "def:" + getSide( part ).ordinal(), def );
				data.setTag( "extra:" + getSide( part ).ordinal(), extra );
			}
		}
	}

	public void readFromNBT(NBTTagCompound data)
	{
		fc.readFromNBT( data );

		for (int x = 0; x < 7; x++)
		{
			ForgeDirection side = ForgeDirection.getOrientation( x );

			NBTTagCompound def = data.getCompoundTag( "def:" + side.ordinal() );
			NBTTagCompound extra = data.getCompoundTag( "extra:" + side.ordinal() );
			if ( def != null && extra != null )
			{
				IPart p = getPart( side );
				ItemStack iss = ItemStack.loadItemStackFromNBT( def );
				if ( iss == null )
					continue;

				ItemStack current = p == null ? null : p.getItemStack( PartItemStack.World );

				if ( Platform.isSameItemType( iss, current ) )
					p.readFromNBT( extra );
				else
				{
					removePart( side, false );
					side = addPart( iss, side, null );
					if ( side != null )
					{
						p = getPart( side );
						p.readFromNBT( extra );
					}
					else
						throw new RuntimeException( "Invalid NBT For CableBus Container." );
				}
			}
			else
				removePart( side, false );
		}
	}

	public List getDrops(List drops)
	{
		for (ForgeDirection s : ForgeDirection.values())
		{
			IPart part = getPart( s );
			if ( part != null )
			{
				drops.add( part.getItemStack( PartItemStack.Break ) );
				part.getDrops( drops, false );
			}

			if ( s != ForgeDirection.UNKNOWN )
			{
				IFacadePart fp = getFacadeContainer().getFacade( s );
				if ( fp != null )
					drops.add( fp.getItemStack() );
			}
		}

		return drops;
	}

	@Override
	public void markForUpdate()
	{
		tcb.markForUpdate();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return tcb.getLocation();
	}

	@Override
	public TileEntity getTile()
	{
		return tcb.getTile();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		IPart part = getPart( dir );
		if ( part != null && part instanceof IGridHost )
		{
			AECableType t = ((IGridHost) part).getCableConnectionType( dir );
			if ( t != null && t != AECableType.NONE )
				return t;
		}

		if ( center != null )
		{
			IPartCable c = center;
			return c.getCableConnectionType();
		}
		return AECableType.NONE;
	}

	@Override
	public AEColor getColor()
	{
		if ( center != null )
		{
			IPartCable c = center;
			return c.getCableColor();
		}
		return AEColor.Transparent;
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		return fc;
	}

	@Override
	public void clearContainer()
	{
		throw new RuntimeException( "Now thats silly!" );
	}

	@Override
	public boolean isBlocked(ForgeDirection side)
	{
		return tcb.isBlocked( side );
	}

	public int getLightValue()
	{
		int light = 0;
		for (ForgeDirection d : ForgeDirection.values())
		{
			IPart p = getPart( d );
			if ( p != null )
				light = Math.max( p.getLightLevel(), light );
		}
		return light;
	}

	public boolean recolourBlock(ForgeDirection side, int colour)
	{
		IPart cable = getPart( ForgeDirection.UNKNOWN );
		if ( cable != null )
		{
			IPartCable pc = (IPartCable) cable;

			AEColor colors[] = AEColor.values();
			if ( colors.length > colour )
				return pc.changeColor( colors[colour] );
		}
		return false;
	}

	public boolean activate(EntityPlayer player, Vec3 pos)
	{
		SelectedPart p = selectPart( pos );
		if ( p != null && p.part != null )
		{
			return p.part.onActivate( player, pos );
		}
		return false;
	}

	@Override
	public SelectedPart selectPart(Vec3 pos)
	{
		for (ForgeDirection side : ForgeDirection.values())
		{
			IPart p = getPart( side );
			if ( p != null )
			{
				List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

				IPartCollsionHelper bch = new BusCollisionHelper( boxes, side, null, true );
				p.getBoxes( bch );
				for (AxisAlignedBB bb : boxes)
				{
					bb = bb.expand( 0.002, 0.002, 0.002 );
					if ( bb.isVecInside( pos ) )
					{
						return new SelectedPart( p, side );
					}
				}
			}
		}

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			IFacadePart p = fc.getFacade( side );
			if ( p != null )
			{
				List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

				IPartCollsionHelper bch = new BusCollisionHelper( boxes, side, null, true );
				p.getBoxes( bch );
				for (AxisAlignedBB bb : boxes)
				{
					bb = bb.expand( 0.01, 0.01, 0.01 );
					if ( bb.isVecInside( pos ) )
					{
						return new SelectedPart( p, side );
					}
				}
			}
		}

		return new SelectedPart();
	}

	@Override
	public void PartChanged()
	{
		tcb.PartChanged();
	}

	@Override
	public void markForSave()
	{
		tcb.markForSave();
	}

	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{
		for (ForgeDirection side : ForgeDirection.values())
		{
			IPart p = getPart( side );
			if ( p != null )
			{
				p.randomDisplayTick( world, x, y, z, r );
			}
		}
	}

	@Override
	public boolean hasRedstone(ForgeDirection side)
	{
		return hasRedstone;
	}

	public boolean isLadder(EntityLivingBase entity)
	{
		for (ForgeDirection side : ForgeDirection.values())
		{
			IPart p = getPart( side );
			if ( p != null )
			{
				if ( p.isLadder( entity ) )
					return true;
			}
		}

		return false;
	}

	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y, int z, ForgeDirection side)
	{
		return getPart( side ).canConnectRedstone() ? RedNetConnectionType.CableSingle : RedNetConnectionType.None;
	}

	@Override
	public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection side)
	{
		// never called
		return null;
	}

	@Override
	public int getOutputValue(World world, int x, int y, int z, ForgeDirection side, int subnet)
	{
		// never called
		return 0;
	}

	@Override
	public void onInputsChanged(World world, int x, int y, int z, ForgeDirection side, int[] inputValues)
	{
		// never called
	}

	@Override
	public void onInputChanged(World world, int x, int y, int z, ForgeDirection side, int inputValue)
	{
		// never called
	}

	@Override
	public void securityBreak()
	{
		for (ForgeDirection d : ForgeDirection.values())
		{
			IPart p = getPart( d );
			if ( p != null && p instanceof IGridHost )
				((IGridHost) p).securityBreak();
		}
	}

}
