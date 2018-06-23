
package appeng.fluids.container;


import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFakeFluid;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;


public class ContainerFluidInterface extends AEBaseContainer
{

	private final DualityFluidInterface myDuality;

	public ContainerFluidInterface( final InventoryPlayer ip, final IFluidInterfaceHost te )
	{
		super( ip, (TileEntity) ( te instanceof TileEntity ? te : null ), (IPart) ( te instanceof IPart ? te : null ) );

		this.myDuality = te.getDualityFluidInterface();

		for( int x = 0; x < DualityFluidInterface.NUMBER_OF_TANKS; x++ )
		{
			this.addSlotToContainer( new SlotFakeFluid( this.myDuality.getConfig(), x, 8 + 18 * x, 115 ) );
		}

		this.bindPlayerInventory( ip, 0, 231 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );
		super.detectAndSendChanges();
	}

	@Override
	public boolean isValidForSlot( Slot s, ItemStack i )
	{
		return s instanceof SlotFakeFluid ? i.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) : super.isValidForSlot( s, i );
	}
}
