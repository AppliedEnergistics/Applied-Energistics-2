package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;

public class ContainerVibrationChamber extends AEBaseContainer
{

	TileVibrationChamber myte;

	public ContainerVibrationChamber(InventoryPlayer ip, TileVibrationChamber te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.FUEL, te, 0, 80, 37, invPlayer ) );

		bindPlayerInventory( ip, 0, 166 - /* height of playerinventory */82 );
	}

	public int aePerTick = 5;

	@GuiSync(0)
	public int burnProgress = 0;

	@GuiSync(1)
	public int burnSpeed = 100;

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			this.burnProgress = (int) (this.myte.maxBurnTime <= 0 ? 0 : 12 * this.myte.burnTime / this.myte.maxBurnTime);
			this.burnSpeed = this.myte.burnSpeed;
		}

		super.detectAndSendChanges();
	}

}
