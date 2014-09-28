package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;

public class ContainerVibrationChamber extends AEBaseContainer
{

	TileVibrationChamber vibrationChamber;

	public ContainerVibrationChamber(InventoryPlayer ip, TileVibrationChamber vibrationChamber) {
		super( ip, vibrationChamber, null );
		this.vibrationChamber = vibrationChamber;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.FUEL, vibrationChamber, 0, 80, 37, invPlayer ) );

		bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
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
			this.burnProgress = (int) (this.vibrationChamber.maxBurnTime <= 0 ? 0 : 12 * this.vibrationChamber.burnTime / this.vibrationChamber.maxBurnTime);
			this.burnSpeed = this.vibrationChamber.burnSpeed;
		}

		super.detectAndSendChanges();
	}

}
