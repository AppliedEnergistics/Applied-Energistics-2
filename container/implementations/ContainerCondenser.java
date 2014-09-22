package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;

public class ContainerCondenser extends AEBaseContainer
{

	TileCondenser myte;

	public ContainerCondenser(InventoryPlayer ip, TileCondenser te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.TRASH, te, 0, 51, 52, ip ) );
		addSlotToContainer( new SlotOutput( te, 1, 105, 52, -1 ) );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.STORAGE_COMPONENT, te.getInternalInventory(), 2, 101, 26, ip )).setStackLimit( 1 ) );

		bindPlayerInventory( ip, 0, 197 - /* height of playerinventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			double maxStorage = this.myte.getStorage();
			double requiredEnergy = this.myte.getRequiredPower();
			int maxDisplay = requiredEnergy == 0 ? (int) maxStorage : (int) Math.min( requiredEnergy, maxStorage );

			this.requiredEnergy = (int) maxDisplay;
			this.storedPower = (int) this.myte.storedPower;
			this.output = (CondenserOutput) this.myte.getConfigManager().getSetting( Settings.CONDENSER_OUTPUT );
		}

		super.detectAndSendChanges();
	}

	@GuiSync(0)
	public long requiredEnergy = 0;

	@GuiSync(1)
	public long storedPower = 0;

	@GuiSync(2)
	public CondenserOutput output = CondenserOutput.TRASH;

}
