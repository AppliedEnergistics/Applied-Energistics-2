package appeng.container.implementations;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketProgressBar;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.util.Platform;

public class ContainerSpatialIOPort extends AEBaseContainer
{

	TileSpatialIOPort myte;

	IGrid network;

	public long reqPower;
	public long currentPower;
	public long maxPower;
	public long eff;

	int delay = 40;

	@Override
	public void updateFullProgressBar(int id, long value)
	{
		if ( id == 0 )
			currentPower = value;

		if ( id == 1 )
			maxPower = value;

		if ( id == 2 )
			reqPower = value;

		if ( id == 3 )
			eff = value;
	}

	public ContainerSpatialIOPort(InventoryPlayer ip, TileSpatialIOPort te) {
		super( ip, te, null );
		myte = te;

		if ( Platform.isServer() )
			network = te.getGridNode( ForgeDirection.UNKNOWN ).getGrid();

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.SPATIAL_STORAGE_CELLS, te, 0, 52, 48 ) );
		addSlotToContainer( new SlotOutput( te, 1, 113, 48, PlaceableItemType.SPATIAL_STORAGE_CELLS.IIcon ) );

		bindPlayerInventory( ip, 0, 197 - /* height of playerinventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			delay++;
			if ( delay > 15 && network != null )
			{
				delay = 0;

				IEnergyGrid eg = network.getCache( IEnergyGrid.class );
				ISpatialCache sc = network.getCache( ISpatialCache.class );
				if ( eg != null )
				{
					currentPower = (long) (100.0 * eg.getStoredPower());
					maxPower = (long) (100.0 * eg.getMaxStoredPower());
					reqPower = (long) (100.0 * sc.requiredPower());
					eff = (long) (100.0f * sc.currentEffiency());

					for (Object c : this.crafters)
					{
						ICrafting icrafting = (ICrafting) c;
						try
						{
							NetworkHandler.instance.sendTo( new PacketProgressBar( 0, currentPower ), (EntityPlayerMP) icrafting );
							NetworkHandler.instance.sendTo( new PacketProgressBar( 1, maxPower ), (EntityPlayerMP) icrafting );
							NetworkHandler.instance.sendTo( new PacketProgressBar( 2, reqPower ), (EntityPlayerMP) icrafting );
							NetworkHandler.instance.sendTo( new PacketProgressBar( 3, eff ), (EntityPlayerMP) icrafting );
						}
						catch (IOException e)
						{
							AELog.error( e );
						}
					}
				}
			}
		}

		super.detectAndSendChanges();
	}
}
