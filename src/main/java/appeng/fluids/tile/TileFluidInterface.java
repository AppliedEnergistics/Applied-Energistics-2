
package appeng.fluids.tile;


import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.tile.grid.AENetworkTile;


public class TileFluidInterface extends AENetworkTile implements IGridTickable, IFluidInterfaceHost
{
	private final DualityFluidInterface duality = new DualityFluidInterface( this.getProxy(), this );

	@MENetworkEventSubscribe
	public void stateChange( final MENetworkChannelsChanged c )
	{
		this.duality.notifyNeighbors();
	}

	@MENetworkEventSubscribe
	public void stateChange( final MENetworkPowerStatusChange c )
	{
		this.duality.notifyNeighbors();
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return this.duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int ticksSinceLastCall )
	{
		return this.duality.tickingRequest( node, ticksSinceLastCall );
	}

	@Override
	public DualityFluidInterface getDualityFluidInterface()
	{
		return this.duality;
	}

	@Override
	public TileEntity getTileEntity()
	{
		return this;
	}

	@Override
	public void markDirty()
	{
		this.duality.markDirty();
	}

	@Override
	public void gridChanged()
	{
		this.duality.gridChanged();
	}

	@Override
	public NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.duality.writeToNBT( data );
		return data;
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.duality.readFromNBT( data );
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return this.duality.getCableConnectionType( dir );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return this.duality.getLocation();
	}

	@Override
	public EnumSet<EnumFacing> getTargets()
	{
		return EnumSet.allOf( EnumFacing.class );
	}

	@Override
	public boolean hasCapability( Capability<?> capability, @Nullable EnumFacing facing )
	{
		return this.duality.hasCapability( capability, facing ) || super.hasCapability( capability, facing );
	}

	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable EnumFacing facing )
	{
		T result = this.duality.getCapability( capability, facing );
		if( result != null )
		{
			return result;
		}
		return super.getCapability( capability, facing );
	}
}
