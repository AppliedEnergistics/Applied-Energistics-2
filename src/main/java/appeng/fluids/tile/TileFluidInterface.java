
package appeng.fluids.tile;


import java.io.IOException;
import java.util.EnumSet;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

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
import appeng.helpers.ICustomRotatable;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;


public class TileFluidInterface extends AENetworkTile implements IGridTickable, IFluidInterfaceHost, ICustomRotatable
{
	private final DualityFluidInterface duality = new DualityFluidInterface( this.getProxy(), this );
	// Indicates that this interface has no specific direction set
	private boolean omniDirectional = true;

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
	public void onReady()
	{
		this.configureNodeSides();
		super.onReady();
	}

	@Override
	public NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setBoolean( "omniDirectional", this.omniDirectional );
		this.duality.writeToNBT( data );
		return data;
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.omniDirectional = data.getBoolean( "omniDirectional" );

		this.duality.readFromNBT( data );
	}

	@Override
	protected boolean readFromStream( final ByteBuf data ) throws IOException
	{
		final boolean c = super.readFromStream( data );
		boolean oldOmniDirectional = this.omniDirectional;
		this.omniDirectional = data.readBoolean();
		return oldOmniDirectional != this.omniDirectional || c;
	}

	@Override
	protected void writeToStream( final ByteBuf data ) throws IOException
	{
		super.writeToStream( data );
		data.writeBoolean( this.omniDirectional );
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
		if( this.omniDirectional )
		{
			return EnumSet.allOf( EnumFacing.class );
		}
		return EnumSet.of( this.getForward() );
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

	public void setSide( final EnumFacing facing )
	{
		if( Platform.isClient() )
		{
			return;
		}

		EnumFacing newForward = facing;

		if( !this.omniDirectional && this.getForward() == facing.getOpposite() )
		{
			newForward = facing;
		}
		else if( !this.omniDirectional && ( this.getForward() == facing || this.getForward() == facing.getOpposite() ) )
		{
			this.omniDirectional = true;
		}
		else if( this.omniDirectional )
		{
			newForward = facing.getOpposite();
			this.omniDirectional = false;
		}
		else
		{
			newForward = Platform.rotateAround( this.getForward(), facing );
		}

		if( this.omniDirectional )
		{
			this.setOrientation( EnumFacing.NORTH, EnumFacing.UP );
		}
		else
		{
			EnumFacing newUp = EnumFacing.UP;
			if( newForward == EnumFacing.UP || newForward == EnumFacing.DOWN )
			{
				newUp = EnumFacing.NORTH;
			}
			this.setOrientation( newForward, newUp );
		}

		this.configureNodeSides();
		this.markForUpdate();
		this.markDirty();
	}

	private void configureNodeSides()
	{
		if( this.omniDirectional )
		{
			this.getProxy().setValidSides( EnumSet.allOf( EnumFacing.class ) );
		}
		else
		{
			this.getProxy().setValidSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
		}
	}

}
