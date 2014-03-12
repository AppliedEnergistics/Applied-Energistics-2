package appeng.tile.misc;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.WrapperInventoryRange;
import appeng.util.item.AEItemStack;

public class TileInscriber extends AENetworkPowerTile implements IGridTickable
{

	final int top[] = new int[] { 0 };
	final int bottom[] = new int[] { 1 };
	final int sides[] = new int[] { 2, 3 };

	AppEngInternalInventory inv = new AppEngInternalInventory( this, 4 );

	public final int maxProessingTime = 100;
	public int processingTime = 0;

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	private class TileInscriberHandler extends AETileEventHandler
	{

		public TileInscriberHandler() {
			super( TileEventType.TICK, TileEventType.WORLD_NBT, TileEventType.NETWORK );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			inv.writeToNBT( data, "inscriberInv" );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			inv.readFromNBT( data, "inscriberInv" );
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			int slot = data.readByte();

			for (int num = 0; num < inv.getSizeInventory(); num++)
			{
				if ( (slot & (1 << num)) > 0 )
					inv.setInventorySlotContents( num, AEItemStack.loadItemStackFromPacket( data ).getItemStack() );
				else
					inv.setInventorySlotContents( num, null );
			}

			return false;
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			int slot = 0;

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
		public void Tick()
		{

		}

	};

	public TileInscriber() {
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		internalMaxPower = 1500;
		gridProxy.setIdlePowerUsage( 0 );
		addNewHandler( new TileInscriberHandler() );
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		ForgeDirection right = Platform.crossProduct( getForward(), getUp() );
		gridProxy.setValidSides( EnumSet.of( getForward().getOpposite(), right, right.getOpposite() ) );
		setPowerSides( EnumSet.of( getForward().getOpposite(), right, right.getOpposite() ) );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		ForgeDirection d = ForgeDirection.getOrientation( side );

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
		if ( i == 0 || i == 1 )
		{
			for (ItemStack s : Inscribe.plates)
				if ( Platform.isSameItemPrecise( s, itemstack ) )
					return true;
		}

		if ( i == 2 )
		{
			for (ItemStack s : Inscribe.inputs)
				if ( Platform.isSameItemPrecise( s, itemstack ) )
					return true;
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
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
				gridProxy.getTick().wakeDevice( gridProxy.getNode() );
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	private InscriberRecipe getTask()
	{

		for (InscriberRecipe i : Inscribe.recipes)
		{
			ItemStack PlateA = getStackInSlot( 0 );
			ItemStack PlateB = getStackInSlot( 1 );

			boolean matchA = (PlateA == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateA, i.plateA )) && // and...
					(PlateB == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateB, i.plateB ));

			boolean matchB = (PlateB == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateB, i.plateA )) && // and...
					(PlateA == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateA, i.plateB ));

			if ( matchA || matchB )
			{
				for (ItemStack opion : i.imprintable)
				{
					if ( Platform.isSameItemPrecise( opion, getStackInSlot( 2 ) ) )
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
		return false;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 1, 1, !hasWork(), false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		IEnergyGrid eg;
		try
		{
			eg = gridProxy.getEnergy();
			IEnergySource src = this;

			double powerReq = extractAEPower( 10, Actionable.SIMULATE, PowerMultiplier.CONFIG );

			if ( powerReq < 9.99 )
			{
				src = eg;
				powerReq = eg.extractAEPower( 10, Actionable.SIMULATE, PowerMultiplier.CONFIG );
			}

			if ( powerReq > 9.99 )
			{
				src.extractAEPower( 10, Actionable.MODULATE, PowerMultiplier.CONFIG );

				if ( processingTime == 0 )
					processingTime++;
				else
					processingTime += TicksSinceLastCall;
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}

		if ( processingTime > maxProessingTime )
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
		}

		return hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
	}
}
