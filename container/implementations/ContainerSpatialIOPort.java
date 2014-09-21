package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.util.Platform;

public class ContainerSpatialIOPort extends AEBaseContainer
{

	TileSpatialIOPort myte;

	IGrid network;

	@GuiSync(0)
	public long currentPower;
	@GuiSync(1)
	public long maxPower;
	@GuiSync(2)
	public long reqPower;
	@GuiSync(3)
	public long eff;

	int delay = 40;

	public ContainerSpatialIOPort(InventoryPlayer ip, TileSpatialIOPort te) {
		super( ip, te, null );
		myte = te;

		if ( Platform.isServer() )
			network = te.getGridNode( ForgeDirection.UNKNOWN ).getGrid();

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.SPATIAL_STORAGE_CELLS, te, 0, 52, 48, invPlayer ) );
		addSlotToContainer( new SlotOutput( te, 1, 113, 48, SlotRestrictedInput.PlacableItemType.SPATIAL_STORAGE_CELLS.IIcon ) );

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
					eff = (long) (100.0f * sc.currentEfficiency());
				}
			}
		}

		super.detectAndSendChanges();
	}
}
