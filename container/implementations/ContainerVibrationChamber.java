package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerVibrationChamber extends AEBaseContainer
{

	TileVibrationChamber myte;

	public ContainerVibrationChamber(InventoryPlayer ip, TileVibrationChamber te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.FUEL, te, 0, 80, 37 ) );

		bindPlayerInventory( ip, 0, 166 - /* height of playerinventory */82 );
	}

	public int aePerTick = 5;
	public int burnSpeed = 100;
	public int burnProgress = 0;

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if ( Platform.isServer() )
		{
			int burnProgress = (int) (this.myte.maxBurnTime <= 0 ? 0 : 12 * this.myte.burnTime / this.myte.maxBurnTime);

			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.burnProgress != burnProgress )
				{
					icrafting.sendProgressBarUpdate( this, 0, (int) burnProgress );
				}

				if ( this.burnSpeed != this.myte.burnSpeed )
				{
					icrafting.sendProgressBarUpdate( this, 1, this.myte.burnSpeed );
				}
			}

			this.burnProgress = burnProgress;
			this.burnSpeed = this.myte.burnSpeed;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int idx, int value)
	{
		if ( idx == 0 )
		{
			this.burnProgress = value;
		}

		if ( idx == 1 )
		{
			this.burnSpeed = value;
		}

	}

}
