package appeng.parts.misc;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.parts.PartBasicState;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartInterface extends PartBasicState implements IGridTickable, ISegmentedInventory, IStorageMonitorable, IInventoryDestination, IInterfaceHost,
		ISidedInventory, IAEAppEngInventory, ITileStorageMonitorable, IPriorityHost
{

	final DualityInterface duality = new DualityInterface( proxy, this );

	public PartInterface(ItemStack is) {
		super( PartInterface.class, is );
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		duality.initialize();
	}

	@MENetworkEventSubscribe
	public void stateChange(MENetworkChannelsChanged c)
	{
		duality.notifyNeighbors();
	}

	@MENetworkEventSubscribe
	public void stateChange(MENetworkPowerStatusChange c)
	{
		duality.notifyNeighbors();
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
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{
		duality.addDrops( drops );
	}

	@Override
	@SideOnly(Side.CLIENT)
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
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimplifiedRendering( x, y, z, this, renderCache );
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
	public IIcon getBreakingTexture()
	{
		return is.getIconIndex();
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 2, 2, 14, 14, 14, 16 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 4;
	}

	@Override
	public TileEntity getTileEntity()
	{
		return super.getHost().getTile();
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
	public String getInventoryName()
	{
		return duality.getStorage().getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return duality.getStorage().hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return duality.getStorage().getInventoryStackLimit();
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return duality.getConfigManager();
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		return duality.getInventoryByName( name );
	}

	@Override
	public void markDirty()
	{
		duality.getStorage().markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return duality.getStorage().isUseableByPlayer( entityplayer );
	}

	@Override
	public void openInventory()
	{
		duality.getStorage().openInventory();
	}

	@Override
	public void closeInventory()
	{
		duality.getStorage().closeInventory();
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
	public boolean onPartActivate(EntityPlayer p, Vec3 pos)
	{
		if ( p.isSneaking() )
			return false;

		if ( Platform.isServer() )
			Platform.openGUI( p, getTileEntity(), side, GuiBridge.GUI_INTERFACE );

		return true;
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src)
	{
		return duality.getMonitorable( side, src, this );
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table)
	{
		return duality.pushPattern( patternDetails, table );
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker)
	{
		duality.provideCrafting( craftingTracker );
	}

	@Override
	public EnumSet<ForgeDirection> getTargets()
	{
		return EnumSet.of( side );
	}

	@Override
	public boolean isBusy()
	{
		return duality.isBusy();
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return duality.getInstalledUpgrades( u );
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode)
	{
		return duality.injectCraftedItems( link, items, mode );
	}

	@Override
	public void jobStateChange(ICraftingLink link)
	{
		duality.jobStateChange( link );
	}

	@Override
	public int getPriority()
	{
		return duality.getPriority();
	}

	@Override
	public void setPriority(int newValue)
	{
		duality.setPriority( newValue );
	}

}
