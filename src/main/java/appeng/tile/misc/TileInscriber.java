package appeng.tile.misc;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.parts.automation.UpgradeInventory;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.WrapperInventoryRange;
import appeng.util.item.AEItemStack;

public class TileInscriber extends AENetworkPowerTile implements IGridTickable, IUpgradeableHost, IConfigManagerHost
{

	final int top[] = new int[] { 0 };
	final int bottom[] = new int[] { 1 };
	final int sides[] = new int[] { 2, 3 };

	AppEngInternalInventory inv = new AppEngInternalInventory( this, 4 );

	public final int maxProcessingTime = 100;
	public int processingTime = 0;

	// cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the normal routine.
	public boolean smash;
	public int finalStep;

	public long clientStart;

	static final ItemStack inscriberStack = AEApi.instance().blocks().blockInscriber.stack( 1 );
	private IConfigManager settings = new ConfigManager( this );
	private UpgradeInventory upgrades = new UpgradeInventory( inscriberStack, this, getUpgradeSlots() );

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileInscriber(NBTTagCompound data)
	{
		inv.writeToNBT( data, "inscriberInv" );
		upgrades.writeToNBT( data, "upgrades" );
		settings.writeToNBT( data );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileInscriber(NBTTagCompound data)
	{
		inv.readFromNBT( data, "inscriberInv" );
		upgrades.readFromNBT( data, "upgrades" );
		settings.readFromNBT( data );
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileInscriber(ByteBuf data) throws IOException
	{
		int slot = data.readByte();

		boolean oldSmash = smash;
		boolean newSmash = (slot & 64) == 64;

		if ( oldSmash != newSmash && newSmash )
		{
			smash = true;
			clientStart = System.currentTimeMillis();
		}

		for (int num = 0; num < inv.getSizeInventory(); num++)
		{
			if ( (slot & (1 << num)) > 0 )
				inv.setInventorySlotContents( num, AEItemStack.loadItemStackFromPacket( data ).getItemStack() );
			else
				inv.setInventorySlotContents( num, null );
		}

		return false;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileInscriber(ByteBuf data) throws IOException
	{
		int slot = smash ? 64 : 0;

		for (int num = 0; num < inv.getSizeInventory(); num++)
		{
			if ( inv.getStackInSlot( num ) != null )
				slot = slot | (1 << num);
		}

		data.writeByte( slot );
		for (int num = 0; num < inv.getSizeInventory(); num++)
		{
			if ( (slot & (1 << num)) > 0 )
			{
				AEItemStack st = AEItemStack.create( inv.getStackInSlot( num ) );
				st.writeToPacket( data );
			}
		}
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

	public TileInscriber()
	{
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		internalMaxPower = 1500;
		gridProxy.setIdlePowerUsage( 0 );
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		gridProxy.setValidSides( EnumSet.complementOf( EnumSet.of( getForward() ) ) );
		setPowerSides( EnumSet.complementOf( EnumSet.of( getForward() ) ) );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection d)
	{
		if ( d == ForgeDirection.UP )
			return top;

		if ( d == ForgeDirection.DOWN )
			return bottom;

		return sides;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( smash )
			return false;

		if ( i == 0 || i == 1 )
		{
			if ( AEApi.instance().materials().materialNamePress.sameAsStack( itemstack ) )
				return true;

			for (ItemStack s : Inscribe.plates)
				if ( Platform.isSameItemPrecise( s, itemstack ) )
					return true;
		}

		if ( i == 2 )
		{
			return true;
			// for (ItemStack s : Inscribe.inputs)
			// if ( Platform.isSameItemPrecise( s, itemstack ) )
			// return true;
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		if ( smash )
			return false;

		return i == 0 || i == 1 || i == 3;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		try
		{
			if ( mc != InvOperation.markDirty )
			{
				if ( slot != 3 )
					processingTime = 0;

				if ( !smash )
					markForUpdate();

				gridProxy.getTick().wakeDevice( gridProxy.getNode() );
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	public InscriberRecipe getTask()
	{
		ItemStack PlateA = getStackInSlot( 0 );
		ItemStack PlateB = getStackInSlot( 1 );
		ItemStack renamedItem = getStackInSlot( 2 );

		if ( PlateA != null && PlateA.stackSize > 1 )
			return null;

		if ( PlateB != null && PlateB.stackSize > 1 )
			return null;

		if ( renamedItem != null && renamedItem.stackSize > 1 )
			return null;

		boolean isNameA = AEApi.instance().materials().materialNamePress.sameAsStack( PlateA );
		boolean isNameB = AEApi.instance().materials().materialNamePress.sameAsStack( PlateB );

		if ( (isNameA || isNameB) && (isNameA || PlateA == null) && (isNameB || PlateB == null) )
		{
			if ( renamedItem != null )
			{
				String name = "";

				if ( PlateA != null )
				{
					NBTTagCompound tag = Platform.openNbtData( PlateA );
					name += tag.getString( "InscribeName" );
				}

				if ( PlateB != null )
				{
					NBTTagCompound tag = Platform.openNbtData( PlateB );
					if ( name.length() > 0 )
						name += " ";
					name += tag.getString( "InscribeName" );
				}

				ItemStack startingItem = renamedItem.copy();
				renamedItem = renamedItem.copy();
				NBTTagCompound tag = Platform.openNbtData( renamedItem );

				NBTTagCompound display = tag.getCompoundTag( "display" );
				tag.setTag( "display", display );

				if ( name.length() > 0 )
					display.setString( "Name", name );
				else
					display.removeTag( "Name" );

				return new InscriberRecipe( new ItemStack[] { startingItem }, PlateA, PlateB, renamedItem, false );
			}
		}

		for (InscriberRecipe i : Inscribe.recipes)
		{

			boolean matchA = (PlateA == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateA, i.plateA )) && // and...
					(PlateB == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateB, i.plateB ));

			boolean matchB = (PlateB == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateB, i.plateA )) && // and...
					(PlateA == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateA, i.plateB ));

			if ( matchA || matchB )
			{
				for (ItemStack option : i.imprintable)
				{
					if ( Platform.isSameItemPrecise( option, getStackInSlot( 2 ) ) )
						return i;
				}
			}

		}
		return null;
	}

	private boolean hasWork()
	{
		if ( getTask() != null )
			return true;

		processingTime = 0;
		return false || smash;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.Inscriber.min, TickRates.Inscriber.max, !hasWork(), false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( smash )
		{

			finalStep++;
			if ( finalStep == 8 )
			{

				InscriberRecipe out = getTask();
				if ( out != null )
				{
					ItemStack is = out.output.copy();
					InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( inv, 3, 1, true ), ForgeDirection.UNKNOWN );

					if ( ad.addItems( is ) == null )
					{
						processingTime = 0;
						if ( out.usePlates )
						{
							setInventorySlotContents( 0, null );
							setInventorySlotContents( 1, null );
						}
						setInventorySlotContents( 2, null );
					}
				}

				markDirty();

			}
			else if ( finalStep == 16 )
			{
				finalStep = 0;
				smash = false;
				markForUpdate();
			}
		}
		else
		{
			IEnergyGrid eg;
			try
			{
				eg = gridProxy.getEnergy();
				IEnergySource src = this;

				// Base 1, increase by 1 for each card
				int speedFactor = 1 + upgrades.getInstalledUpgrades( Upgrades.SPEED );
				int powerConsumption = 10 * speedFactor;
				double powerThreshold = powerConsumption - 0.01;
				double powerReq = extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );

				if ( powerReq <= powerThreshold )
				{
					src = eg;
					powerReq = eg.extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );
				}

				if ( powerReq > powerThreshold )
				{
					src.extractAEPower( powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG );

					if ( processingTime == 0 )
						processingTime = processingTime + speedFactor;
					else
						processingTime += TicksSinceLastCall * speedFactor;
				}
			}
			catch (GridAccessException e)
			{
				// :P
			}

			if ( processingTime > maxProcessingTime )
			{
				processingTime = maxProcessingTime;
				InscriberRecipe out = getTask();
				if ( out != null )
				{
					ItemStack is = out.output.copy();
					InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( inv, 3, 1, true ), ForgeDirection.UNKNOWN );
					if ( ad.simulateAdd( is ) == null )
					{
						smash = true;
						finalStep = 0;
						markForUpdate();
					}
				}
			}
		}

		return hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return settings;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "inv" ) )
			return inv;

		if ( name.equals( "upgrades" ) )
			return upgrades;

		return null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return upgrades.getInstalledUpgrades( u );
	}

	protected int getUpgradeSlots()
	{
		return 3;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
	}
}
