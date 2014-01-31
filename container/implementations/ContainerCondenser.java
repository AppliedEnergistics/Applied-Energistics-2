package appeng.container.implementations;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import appeng.api.config.CondenserOuput;
import appeng.api.config.Settings;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerCondenser extends AEBaseContainer
{

	TileCondenser myte;

	public ContainerCondenser(InventoryPlayer ip, TileCondenser te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.TRASH, te, 0, 51, 52 ) );
		addSlotToContainer( new SlotOutput( te, 1, 105, 52, -1 ) );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.STORAGE_COMPONENT, te.getInternalInventory(), 2, 101, 26 )).setStackLimit( 1 ) );

		bindPlayerInventory( ip, 0, 197 - /* height of playerinventory */82 );
	}

	public int requiredEnergy = 0;
	public int storedPower = 0;
	public CondenserOuput output = CondenserOuput.TRASH;

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			double maxStorage = this.myte.getStorage();
			double requiredEnergy = this.myte.getRequiredPower();
			int maxDisplay = requiredEnergy == 0 ? (int) maxStorage : (int) Math.min( requiredEnergy, maxStorage );

			for (int i = 0; i < this.crafters.size(); ++i)
			{
				boolean changed = false;

				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.requiredEnergy != maxDisplay )
				{
					icrafting.sendProgressBarUpdate( this, 0, (int) maxDisplay );
					changed = true;
				}

				if ( this.storedPower != this.myte.storedPower )
				{
					icrafting.sendProgressBarUpdate( this, 1, (int) this.myte.storedPower );
					changed = true;
				}

				if ( this.output != this.myte.getConfigManager().getSetting( Settings.CONDENSER_OUTPUT ) )
				{
					icrafting.sendProgressBarUpdate( this, 2, (int) this.myte.getConfigManager().getSetting( Settings.CONDENSER_OUTPUT ).ordinal() );
					changed = true;
				}

				if ( changed )
				{
					// if the bars changed an item was probably made, so just send shit!
					((EntityPlayerMP) icrafting).playerInventoryBeingManipulated = false;
				}
			}

			this.requiredEnergy = (int) maxDisplay;
			this.storedPower = (int) this.myte.storedPower;
			this.output = (CondenserOuput) this.myte.getConfigManager().getSetting( Settings.CONDENSER_OUTPUT );
		}

		super.detectAndSendChanges();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int idx, int value)
	{
		if ( idx == 0 )
		{
			this.requiredEnergy = value;
		}

		if ( idx == 1 )
		{
			this.storedPower = value;
		}

		if ( idx == 2 )
		{
			this.output = CondenserOuput.values()[value];
		}

	}

}
