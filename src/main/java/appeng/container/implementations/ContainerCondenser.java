package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;

public class ContainerCondenser extends AEBaseContainer implements IProgressProvider
{

	final TileCondenser condenser;

	public ContainerCondenser(InventoryPlayer ip, TileCondenser condenser) {
		super( ip, condenser, null );
		this.condenser = condenser;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.TRASH, condenser, 0, 51, 52, ip ) );
		addSlotToContainer( new SlotOutput( condenser, 1, 105, 52, -1 ) );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.STORAGE_COMPONENT, condenser.getInternalInventory(), 2, 101, 26, ip )).setStackLimit( 1 ) );

		bindPlayerInventory( ip, 0, 197 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			double maxStorage = this.condenser.getStorage();
			double requiredEnergy = this.condenser.getRequiredPower();

			this.requiredEnergy = requiredEnergy == 0 ? (int) maxStorage : (int) Math.min( requiredEnergy, maxStorage );
			this.storedPower = (int) this.condenser.storedPower;
			this.output = (CondenserOutput) this.condenser.getConfigManager().getSetting( Settings.CONDENSER_OUTPUT );
		}

		super.detectAndSendChanges();
	}

	@GuiSync(0)
	public long requiredEnergy = 0;

	@GuiSync(1)
	public long storedPower = 0;

	@GuiSync(2)
	public CondenserOutput output = CondenserOutput.TRASH;

	@Override
	public int getCurrentProgress()
	{
		return (int) storedPower;
	}

	@Override
	public int getMaxProgress()
	{
		return (int) requiredEnergy;
	}

}
