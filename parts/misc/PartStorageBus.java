package appeng.parts.misc;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMontorHandlerReciever;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEMonitorIInventory;
import appeng.parts.PartBasicState;
import appeng.util.Platform;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;

@Interface(modid = "BuildCraft|Transport", iface = "buildcraft.api.transport.IPipeConnection")
public class PartStorageBus extends PartBasicState implements IGridTickable, ICellContainer, IMEMontorHandlerReciever<IAEItemStack>, IPipeConnection
{

	int priority = 0;

	public PartStorageBus(ItemStack is) {
		super( PartStorageBus.class, is );
	}

	boolean cached = false;
	MEMonitorIInventory monitor = null;
	MEInventoryHandler handler = null;

	int handlerHash = 0;

	@Override
	public boolean isValid(Object verificationToken)
	{
		return handler == verificationToken;
	}

	@Override
	public void onNeighborChanged()
	{
		cached = false;
		try
		{
			proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch (GridAccessException e)
		{
			// :3
		}
	}

	private MEInventoryHandler getHandler()
	{
		if ( cached )
			return handler;

		boolean wasSleeping = monitor == null;

		cached = true;
		TileEntity self = getHost().getTile();
		TileEntity target = self.worldObj.getBlockTileEntity( self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );

		int newHandlerHash = Platform.generateTileHash( target );

		if ( handlerHash == newHandlerHash )
			return handler;

		handlerHash = newHandlerHash;
		handler = null;
		monitor = null;
		if ( target != null )
		{
			IExternalStorageHandler esh = AEApi.instance().registries().externalStorage().getHandler( target, side.getOpposite(), StorageChannel.ITEMS );
			if ( esh != null )
			{
				IMEInventory inv = esh.getInventory( target, side.getOpposite(), StorageChannel.ITEMS );

				if ( inv instanceof MEMonitorIInventory )
					monitor = (MEMonitorIInventory) inv;

				if ( inv != null )
				{
					handler = new MEInventoryHandler( inv );

					if ( inv instanceof IMEMonitor )
						((IMEMonitor) inv).addListener( this, handler );
				}
			}
		}

		// update sleep state...
		if ( wasSleeping != (monitor == null) )
		{
			try
			{
				ITickManager tm = proxy.getTick();
				if ( monitor == null )
					tm.sleepDevice( proxy.getNode() );
				else
					tm.wakeDevice( proxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :(
			}
		}

		return handler;
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 2, 2, 14, 14, 14, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 12, 11, 11, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 2, 2, 14, 14, 14, 15 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 5, 5, 12, 11, 11, 13 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 3, 3, 15, 13, 13, 16 );
		bch.addBox( 2, 2, 14, 14, 14, 15 );
		bch.addBox( 5, 5, 12, 11, 11, 13 );
		bch.addBox( 5, 5, 13, 11, 11, 14 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 4;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 5, 20 * 3, monitor == null, false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( monitor != null )
			return monitor.onTick();

		return TickRateModulation.SLEEP;
	}

	@Override
	public void writeToNBT(NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		extra.setInteger( "priority", priority );
	}

	public void readFromNBT(NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		priority = extra.getInteger( "priority" );
	};

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		IMEInventoryHandler out = getHandler();
		if ( out == null )
			return Arrays.asList( new IMEInventoryHandler[] {} );
		return Arrays.asList( new IMEInventoryHandler[] { out } );
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void blinkCell(int slot)
	{
	}

	@Override
	public void postChange(IAEItemStack change)
	{
		try
		{
			proxy.getStorage().postAlterationOfStoredItems( StorageChannel.ITEMS, change );
		}
		catch (GridAccessException e)
		{
			// :(
		}
	}

	@Override
	@Method(modid = "BuildCraft|Transport")
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with)
	{
		return type == PipeType.ITEM && with == side ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT;
	}

}
