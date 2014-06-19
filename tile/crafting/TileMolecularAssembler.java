package appeng.tile.crafting;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerNull;
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

public class TileMolecularAssembler extends AENetworkInvTile implements IAEAppEngInventory, ISidedInventory, IUpgradeableHost, IConfigManagerHost,
		IGridTickable
{

	static final int[] sides = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	static final ItemStack is = AEApi.instance().blocks().blockMolecularAssembler.stack( 1 );

	private InventoryCrafting craftingInv = new InventoryCrafting( new ContainerNull(), 3, 3 );
	private AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 + 2 );
	private IConfigManager settings = new ConfigManager( this );
	private UpgradeInventory upgrades = new UpgradeInventory( is, this, getUpgradeSlots() );

	private ForgeDirection pushDirection = ForgeDirection.UNKNOWN;
	private ItemStack myPattern = null;
	private ICraftingPatternDetails myPlan = null;
	private double progress = 0;
	private boolean isAwake = false;
	private boolean forcePlan = false;

	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, ForgeDirection where)
	{
		if ( myPattern == null )
		{
			boolean isEmpty = true;
			for (int x = 0; x < inv.getSizeInventory(); x++)
				isEmpty = inv.getStackInSlot( x ) == null && isEmpty;

			if ( isEmpty )
			{
				forcePlan = true;
				myPlan = patternDetails;
				pushDirection = where;

				for (int x = 0; x < table.getSizeInventory(); x++)
					inv.setInventorySlotContents( x, table.getStackInSlot( x ) );

				updateSleepyness();
				return true;
			}
		}
		return false;
	}

	private void recalculatePlan()
	{
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

				progress = 0;
				myPattern = is;
				myPlan = ph;
			}
		}
		else
		{
			progress = 0;
			myPlan = null;
			myPattern = null;
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
			super( TileEventType.WORLD_NBT );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			if ( forcePlan )
			{
				ItemStack pattern = myPlan.getPattern();
				if ( pattern != null )
				{
					NBTTagCompound pdata = new NBTTagCompound();
					pattern.writeToNBT( pdata );
					data.setTag( "myPlan", pdata );
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
					ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
					ICraftingPatternDetails ph = iep.getPatternForItem( is, w );
					if ( ph != null )
					{
						forcePlan = true;
						myPlan = ph;
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
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 1, 5, isAwake = hasPattern() && hasMats() || canPush(), false );
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
			return inv.getStackInSlot( 9 ) == null ? TickRateModulation.SLEEP : TickRateModulation.IDLE;
		}

		if ( myPlan == null )
		{
			isAwake = false;
			return TickRateModulation.SLEEP;
		}

		switch (upgrades.getInstalledUpgrades( Upgrades.SPEED ))
		{
		case 0:
			progress += userPower( TicksSinceLastCall );
			break;
		case 1:
			progress += userPower( TicksSinceLastCall * 2 );
			break;
		case 2:
			progress += userPower( TicksSinceLastCall * 6 );
			break;
		case 3:
			progress += userPower( TicksSinceLastCall * 12 );
			break;
		case 4:
			progress += userPower( TicksSinceLastCall * 30 );
			break;
		case 5:
			progress += userPower( TicksSinceLastCall * 120 );
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
				for (int x = 0; x < craftingInv.getSizeInventory(); x++)
					inv.setInventorySlotContents( x, null );

				pushOut( output.copy() );
				isAwake = false;
				return inv.getStackInSlot( 9 ) == null ? TickRateModulation.SLEEP : TickRateModulation.IDLE;
			}
		}

		return TickRateModulation.FASTER;
	}

	private int userPower(int i)
	{
		try
		{
			return (int) gridProxy.getEnergy().extractAEPower( i, Actionable.MODULATE, PowerMultiplier.CONFIG );
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

		return adaptor.addItems( output );
	}

}
