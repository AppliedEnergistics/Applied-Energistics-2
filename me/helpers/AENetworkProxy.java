package appeng.me.helpers;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IOrientable;
import appeng.core.WorldSettings;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.parts.networking.PartCable;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;

public class AENetworkProxy implements IGridBlock
{

	final private IGridProxyable gp;
	final private boolean worldNode;

	private ItemStack myRepInstance;

	private boolean isReady = false;
	private IGridNode node = null;

	private EnumSet<ForgeDirection> validSides;
	public AEColor myColor = AEColor.Transparent;

	private EnumSet<GridFlags> flags = EnumSet.noneOf( GridFlags.class );
	private double idleDraw = 1.0;

	final private String nbtName; // name
	NBTTagCompound data = null; // input

	private EntityPlayer owner;

	@Override
	public ItemStack getMachineRepresentation()
	{
		return myRepInstance;
	}

	public void setVisualRepresentation(ItemStack is)
	{
		myRepInstance = is;
	}

	public AENetworkProxy(IGridProxyable te, String nbtName, ItemStack visual, boolean inWorld) {
		this.gp = te;
		this.nbtName = nbtName;
		worldNode = inWorld;
		myRepInstance = visual;
		validSides = EnumSet.allOf( ForgeDirection.class );
	}

	public void writeToNBT(NBTTagCompound tag)
	{
		if ( node != null )
			node.saveToNBT( nbtName, tag );
	}

	public void readFromNBT(NBTTagCompound tag)
	{
		data = tag;
		if ( node != null && data != null )
		{
			node.loadFromNBT( nbtName, data );
			data = null;
		}
		else if ( node != null && owner != null )
		{
			node.setPlayerID( WorldSettings.getInstance().getPlayerID( owner.getGameProfile() ) );
			owner = null;
		}
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return gp.getLocation();
	}

	@Override
	public AEColor getGridColor()
	{
		return myColor;
	}

	@Override
	public void onGridNotification(GridNotification notification)
	{
		if ( gp instanceof PartCable )
			((PartCable) gp).markForUpdate();
	}

	@Override
	public void setNetworkStatus(IGrid grid, int channelsInUse)
	{

	}

	@Override
	public EnumSet<ForgeDirection> getConnectableSides()
	{
		return validSides;
	}

	public void setValidSides(EnumSet<ForgeDirection> validSides)
	{
		this.validSides = validSides;
		if ( node != null )
			node.updateState();
	}

	public IGridNode getNode()
	{
		if ( node == null && Platform.isServer() && isReady )
		{
			node = AEApi.instance().createGridNode( this );
			readFromNBT( data );
			node.updateState();
		}

		return node;
	}

	public void validate()
	{
		if ( gp instanceof AEBaseTile )
			TickHandler.instance.addInit( (AEBaseTile) gp );
	}

	public void onChunkUnload()
	{
		isReady = false;
		invalidate();
	}

	public void invalidate()
	{
		isReady = false;
		if ( node != null )
		{
			node.destroy();
			node = null;
		}
	}

	public void onReady()
	{
		isReady = true;

		// send orientation based directionality to the node.
		if ( gp instanceof IOrientable )
		{
			IOrientable ori = (IOrientable) gp;
			if ( ori.canBeRotated() )
				ori.setOrientation( ori.getForward(), ori.getUp() );
		}

		getNode();
	}

	@Override
	public IGridHost getMachine()
	{
		return gp;
	}

	/**
	 * short cut!
	 * 
	 * @return
	 * @throws GridAccessException
	 */
	public IGrid getGrid() throws GridAccessException
	{
		if ( node == null )
			throw new GridAccessException();
		IGrid grid = node.getGrid();
		if ( grid == null )
			throw new GridAccessException();
		return grid;
	}

	public IEnergyGrid getEnergy() throws GridAccessException
	{
		IGrid grid = getGrid();
		if ( grid == null )
			throw new GridAccessException();
		IEnergyGrid eg = grid.getCache( IEnergyGrid.class );
		if ( eg == null )
			throw new GridAccessException();
		return eg;
	}

	public IPathingGrid getPath() throws GridAccessException
	{
		IGrid grid = getGrid();
		if ( grid == null )
			throw new GridAccessException();
		IPathingGrid pg = grid.getCache( IPathingGrid.class );
		if ( pg == null )
			throw new GridAccessException();
		return pg;
	}

	public ITickManager getTick() throws GridAccessException
	{
		IGrid grid = getGrid();
		if ( grid == null )
			throw new GridAccessException();
		ITickManager pg = grid.getCache( ITickManager.class );
		if ( pg == null )
			throw new GridAccessException();
		return pg;
	}

	public IStorageGrid getStorage() throws GridAccessException
	{
		IGrid grid = getGrid();
		if ( grid == null )
			throw new GridAccessException();

		IStorageGrid pg = grid.getCache( IStorageGrid.class );

		if ( pg == null )
			throw new GridAccessException();

		return pg;
	}

	public P2PCache getP2P() throws GridAccessException
	{
		IGrid grid = getGrid();
		if ( grid == null )
			throw new GridAccessException();

		P2PCache pg = grid.getCache( P2PCache.class );

		if ( pg == null )
			throw new GridAccessException();

		return pg;
	}

	public ISecurityGrid getSecurity() throws GridAccessException
	{
		IGrid grid = getGrid();
		if ( grid == null )
			throw new GridAccessException();

		ISecurityGrid sg = grid.getCache( ISecurityGrid.class );

		if ( sg == null )
			throw new GridAccessException();

		return sg;
	}

	public ICraftingGrid getCrafting() throws GridAccessException
	{
		IGrid grid = getGrid();
		if ( grid == null )
			throw new GridAccessException();

		ICraftingGrid sg = grid.getCache( ICraftingGrid.class );

		if ( sg == null )
			throw new GridAccessException();

		return sg;
	}

	@Override
	public boolean isWorldAccessible()
	{
		return worldNode;
	}

	@Override
	public EnumSet<GridFlags> getFlags()
	{
		return flags;
	}

	public void setFlags(GridFlags... requreChannel)
	{
		EnumSet<GridFlags> flags = EnumSet.noneOf( GridFlags.class );

		for (GridFlags gf : requreChannel)
			flags.add( gf );

		this.flags = flags;
	}

	@Override
	public double getIdlePowerUsage()
	{
		return idleDraw;
	}

	public void setIdlePowerUsage(double idle)
	{
		idleDraw = idle;

		if ( node != null )
		{
			try
			{
				IGrid g = getGrid();
				g.postEvent( new MENetworkPowerIdleChange( node ) );
			}
			catch (GridAccessException e)
			{
				// not ready for this yet..
			}
		}
	}

	public boolean isReady()
	{
		return isReady;
	}

	public boolean isActive()
	{
		if ( node == null )
			return false;

		return node.isActive();
	}

	public boolean isPowered()
	{
		try
		{
			return getEnergy().isNetworkPowered();
		}
		catch (GridAccessException e)
		{
			return false;
		}
	}

	@Override
	public void gridChanged()
	{
		gp.gridChanged();
	}

	public void setOwner(EntityPlayer player)
	{
		owner = player;
	}

}
