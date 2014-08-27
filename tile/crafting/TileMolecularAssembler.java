package appeng.tile.crafting;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.ISimplifiedBundle;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerNull;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.GridAccessException;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileMolecularAssembler extends AENetworkInvTile implements IAEAppEngInventory, ISidedInventory, IUpgradeableHost, IConfigManagerHost,
		IGridTickable, ICraftingMachine, IPowerChannelState
{

	static final int[] sides = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	static final ItemStack assemblerStack = AEApi.instance().blocks().blockMolecularAssembler.stack( 1 );

	private InventoryCrafting craftingInv = new InventoryCrafting( new ContainerNull(), 3, 3 );
	private AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 + 2 );
	private IConfigManager settings = new ConfigManager( this );
	private UpgradeInventory upgrades = new UpgradeInventory( assemblerStack, this, getUpgradeSlots() );

	private ForgeDirection pushDirection = ForgeDirection.UNKNOWN;
	private ItemStack myPattern = null;
	private ICraftingPatternDetails myPlan = null;
	private double progress = 0;
	private boolean isAwake = false;
	private boolean forcePlan = false;

	private boolean reboot = true;
	public ISimplifiedBundle lightCache;

	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, ForgeDirection where)
	{
		if ( myPattern == null )
		{
			boolean isEmpty = true;
			for (int x = 0; x < inv.getSizeInventory(); x++)
				isEmpty = inv.getStackInSlot( x ) == null && isEmpty;

			if ( isEmpty && patternDetails.isCraftable() )
			{
				forcePlan = true;
				myPlan = patternDetails;
				pushDirection = where;

				for (int x = 0; x < table.getSizeInventory(); x++)
					inv.setInventorySlotContents( x, table.getStackInSlot( x ) );

				updateSleepyness();
				markDirty();
				return true;
			}
		}
		return false;
	}

	private void recalculatePlan()
	{
		reboot = true;

		if ( forcePlan )
			return;

		ItemStack is = inv.getStackInSlot( 10 );

		if ( is != null && is.getItem() instanceof ItemEncodedPattern )
		{
			if ( !Platform.isSameItem( is, myPattern ) )
			{
				World w = getWorldObj();
				ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
				ICraftingPatternDetails ph = iep.getPatternForItem( is, w );

				if ( ph != null && ph.isCraftable() )
				{
					progress = 0;
					myPattern = is;
					myPlan = ph;
				}
			}
		}
		else
		{
			progress = 0;
			forcePlan = false;
			myPlan = null;
			myPattern = null;
			pushDirection = ForgeDirection.UNKNOWN;
		}

		updateSleepyness();
	}

	private void updateSleepyness()
	{
		boolean wasEnabled = isAwake;
		isAwake = myPlan != null && hasMats() || canPush();
		if ( wasEnabled != isAwake )
		{
			try
			{
				if ( isAwake )
					gridProxy.getTick().wakeDevice( gridProxy.getNode() );
				else
					gridProxy.getTick().sleepDevice( gridProxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
	}

	private boolean canPush()
	{
		return inv.getStackInSlot( 9 ) != null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return upgrades.getInstalledUpgrades( u );
	}

	protected int getUpgradeSlots()
	{
		return 5;
	}

	private class TileMolecularAssemblerHandler extends AETileEventHandler
	{

		public TileMolecularAssemblerHandler() {
			super( TileEventType.WORLD_NBT, TileEventType.NETWORK );
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			boolean oldPower = isPowered;
			isPowered = data.readBoolean();
			return isPowered != oldPower;
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			data.writeBoolean( isPowered );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			if ( forcePlan && myPlan != null )
			{
				ItemStack pattern = myPlan.getPattern();
				if ( pattern != null )
				{
					NBTTagCompound pdata = new NBTTagCompound();
					pattern.writeToNBT( pdata );
					data.setTag( "myPlan", pdata );
					data.setInteger( "pushDirection", pushDirection.ordinal() );
				}
			}

			upgrades.writeToNBT( data, "upgrades" );
			inv.writeToNBT( data, "inv" );
			settings.writeToNBT( data );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			if ( data.hasKey( "myPlan" ) )
			{
				ItemStack myPat = ItemStack.loadItemStackFromNBT( data.getCompoundTag( "myPlan" ) );

				if ( myPat != null && myPat.getItem() instanceof ItemEncodedPattern )
				{
					World w = getWorldObj();
					ItemEncodedPattern iep = (ItemEncodedPattern) myPat.getItem();
					ICraftingPatternDetails ph = iep.getPatternForItem( myPat, w );
					if ( ph != null && ph.isCraftable() )
					{
						forcePlan = true;
						myPlan = ph;
						pushDirection = ForgeDirection.getOrientation( data.getInteger( "pushDirection" ) );
					}
				}
			}

			upgrades.readFromNBT( data, "upgrades" );
			inv.readFromNBT( data, "inv" );
			settings.readFromNBT( data );
			recalculatePlan();
		}

	};

	public TileMolecularAssembler() {
		settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		inv.setMaxStackSize( 1 );
		gridProxy.setIdlePowerUsage( 0.0 );
		addNewHandler( new TileMolecularAssemblerHandler() );
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( i >= 9 )
			return false;

		if ( hasPattern() )
			return myPlan.isValidItemForSlot( i, itemstack, getWorldObj() );

		return false;
	}

	@Override
	public boolean acceptsPlans()
	{
		return inv.getStackInSlot( 10 ) == null;
	}

	private boolean hasPattern()
	{
		return myPlan != null && inv.getStackInSlot( 10 ) != null;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 9;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( inv == this.inv )
			recalculatePlan();
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection whichSide)
	{
		return sides;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return settings;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "upgrades" ) )
			return upgrades;

		if ( name.equals( "mac" ) )
			return inv;

		return null;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{

	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	public int getCraftingProgress()
	{
		return (int) progress;
	}

	@Override
	public void getDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{
		super.getDrops( w, x, y, z, drops );

		for (int h = 0; h < upgrades.getSizeInventory(); h++)
		{
			ItemStack is = upgrades.getStackInSlot( h );
			if ( is != null )
				drops.add( is );
		}
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		recalculatePlan();
		updateSleepyness();
		return new TickingRequest( 1, 1, !isAwake, false );
	}

	private boolean hasMats()
	{
		if ( myPlan == null )
			return false;

		for (int x = 0; x < craftingInv.getSizeInventory(); x++)
			craftingInv.setInventorySlotContents( x, inv.getStackInSlot( x ) );

		return myPlan.getOutput( craftingInv, getWorldObj() ) != null;
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( inv.getStackInSlot( 9 ) != null )
		{
			pushOut( inv.getStackInSlot( 9 ) );
			ejectHeldItems();
			updateSleepyness();
			progress = 0;
			return isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
		}

		if ( myPlan == null )
		{
			updateSleepyness();
			return TickRateModulation.SLEEP;
		}

		if ( reboot )
			TicksSinceLastCall = 1;

		if ( !isAwake )
			return TickRateModulation.SLEEP;

		reboot = false;
		int speed = 10;
		switch (upgrades.getInstalledUpgrades( Upgrades.SPEED ))
		{
		case 0:
			progress += userPower( TicksSinceLastCall, speed = 10, 1.0 );
			break;
		case 1:
			progress += userPower( TicksSinceLastCall, speed = 13, 1.3 );
			break;
		case 2:
			progress += userPower( TicksSinceLastCall, speed = 17, 1.7 );
			break;
		case 3:
			progress += userPower( TicksSinceLastCall, speed = 20, 2.0 );
			break;
		case 4:
			progress += userPower( TicksSinceLastCall, speed = 25, 2.5 );
			break;
		case 5:
			progress += userPower( TicksSinceLastCall, speed = 50, 5.0 );
			break;
		}

		if ( progress >= 100 )
		{
			for (int x = 0; x < craftingInv.getSizeInventory(); x++)
				craftingInv.setInventorySlotContents( x, inv.getStackInSlot( x ) );

			progress = 0;
			ItemStack output = myPlan.getOutput( craftingInv, getWorldObj() );
			if ( output != null )
			{
				FMLCommonHandler.instance().firePlayerCraftingEvent( Platform.getPlayer( (WorldServer) getWorldObj() ), output, craftingInv );

				pushOut( output.copy() );

				for (int x = 0; x < craftingInv.getSizeInventory(); x++)
					inv.setInventorySlotContents( x, Platform.getContainerItem( craftingInv.getStackInSlot( x ) ) );

				if ( inv.getStackInSlot( 10 ) == null )
				{
					forcePlan = false;
					myPlan = null;
					pushDirection = ForgeDirection.UNKNOWN;
				}

				ejectHeldItems();

				try
				{
					TargetPoint where = new TargetPoint( worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 32 );
					IAEItemStack item = AEItemStack.create( output );
					NetworkHandler.instance.sendToAllAround( new PacketAssemblerAnimation( xCoord, yCoord, zCoord, (byte) speed, item ), where );
				}
				catch (IOException e)
				{
					// ;P
				}

				updateSleepyness();
				return isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
			}
		}

		return TickRateModulation.FASTER;
	}

	private void ejectHeldItems()
	{
		if ( inv.getStackInSlot( 9 ) == null )
		{
			for (int x = 0; x < 9; x++)
			{
				ItemStack is = inv.getStackInSlot( x );
				if ( is != null )
				{
					if ( myPlan == null || !myPlan.isValidItemForSlot( x, is, worldObj ) )
					{
						inv.setInventorySlotContents( 9, is );
						inv.setInventorySlotContents( x, null );
						markDirty();
						return;
					}
				}
			}
		}
	}

	private int userPower(int ticksPassed, int bonusValue, double acceleratorTax)
	{
		try
		{
			return (int) (gridProxy.getEnergy().extractAEPower( ticksPassed * bonusValue * acceleratorTax, Actionable.MODULATE, PowerMultiplier.CONFIG ) / acceleratorTax);
		}
		catch (GridAccessException e)
		{
			return 0;
		}
	}

	private void pushOut(ItemStack output)
	{
		if ( pushDirection == ForgeDirection.UNKNOWN )
		{
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
				output = pushTo( output, d );
		}
		else
			output = pushTo( output, pushDirection );

		if ( output == null && forcePlan )
		{
			forcePlan = false;
			recalculatePlan();
		}

		inv.setInventorySlotContents( 9, output );
	}

	private ItemStack pushTo(ItemStack output, ForgeDirection d)
	{
		if ( output == null )
			return output;

		TileEntity te = getWorldObj().getTileEntity( xCoord + d.offsetX, yCoord + d.offsetY, zCoord + d.offsetZ );

		if ( te == null )
			return output;

		InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( te, d.getOpposite() );

		if ( adaptor == null )
			return output;

		int size = output.stackSize;
		output = adaptor.addItems( output );
		int newSize = output == null ? 0 : output.stackSize;

		if ( size != newSize )
			markDirty();

		return output;
	}

	boolean isPowered = false;

	@MENetworkEventSubscribe
	public void onPowerEvent(MENetworkPowerStatusChange p)
	{
		boolean newState = gridProxy.isActive();
		if ( newState != isPowered )
		{
			isPowered = newState;
			markForUpdate();
		}
	}

	@Override
	public boolean isPowered()
	{
		return isPowered;
	}

	@Override
	public boolean isActive()
	{
		return isPowered;
	}

}
