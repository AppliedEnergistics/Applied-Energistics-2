package appeng.parts.misc;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.implementations.ISegmentedInventory;
import appeng.api.implementations.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.parts.PartBasicState;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;

public class PartInterface extends PartBasicState implements IGridTickable, ISegmentedInventory, IStorageMonitorable, IInventoryDestination, IInterfaceHost,
		ISidedInventory, IAEAppEngInventory, ITileStorageMonitorable
{

	DualityInterface duality = new DualityInterface( proxy, this );

	public PartInterface(ItemStack is) {
		super( PartInterface.class, is );
	}

	@Override
	public void gridChanged()
	{
		duality.gridChanged();
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		duality.writeToNBT( data );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		duality.readFromNBT( data );
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
		rh.useSimpliedRendering( x, y, z, this );
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
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
	public TileEntity getTileEntity()
	{
		return (TileEntity) super.getHost().getTile();
	}

	@Override
	public boolean canInsert(ItemStack stack)
	{
		return duality.canInsert( stack );
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return duality.getItemInventory();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return duality.getFluidInventory();
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return duality.tickingRequest( node, TicksSinceLastCall );
	}

	@Override
	public int getSizeInventory()
	{
		return duality.getStorage().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return duality.getStorage().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return duality.getStorage().decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return duality.getStorage().getStackInSlotOnClosing( i );
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		duality.getStorage().setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getInvName()
	{
		return duality.getStorage().getInvName();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return duality.getStorage().isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return duality.getStorage().getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged()
	{
		duality.getStorage().onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return duality.getStorage().isUseableByPlayer( entityplayer );
	}

	@Override
	public void openChest()
	{
		duality.getStorage().openChest();
	}

	@Override
	public void closeChest()
	{
		duality.getStorage().closeChest();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return duality.getStorage().isItemValidForSlot( i, itemstack );
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int s)
	{
		return duality.getAccessibleSlotsFromSide( s );
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		duality.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public DualityInterface getInterfaceDuality()
	{
		return duality;
	}

	@Override
	public boolean onActivate(EntityPlayer p, Vec3 pos)
	{
		if ( p.isSneaking() )
			return false;

		if ( Platform.isServer() )
			Platform.openGUI( p, getTileEntity(), side, GuiBridge.GUI_INTERFACE );

		return true;
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side)
	{
		return this;
	}
}
