package appeng.parts.p2p;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;


public class PartP2PGTCEPower extends PartP2PTunnel<PartP2PGTCEPower>
{
	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_gteu" );
	private static final IEnergyContainer NULL_ENERGY_STORAGE = new NullEnergyStorage();
	private final IEnergyContainer inputHandler = new InputEnergyStorage();
	private final Queue<PartP2PGTCEPower> outputs = new ArrayDeque<>();

	public PartP2PGTCEPower( ItemStack is )
	{
		super( is );
	}

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( this.isPowered(), this.isActive() );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.getHost().notifyNeighbors();
	}

	private IEnergyContainer getAttachedEnergyStorage()
	{
		if( this.isActive() )
		{
			final TileEntity self = this.getTile();
			final TileEntity te = self.getWorld().getTileEntity( self.getPos().offset( this.getSide().getFacing() ) );

			if( te != null && te.hasCapability( GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, this.getSide().getOpposite().getFacing() ) )
			{
				return te.getCapability( GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, this.getSide().getOpposite().getFacing() );
			}
		}
		return NULL_ENERGY_STORAGE;
	}

	@Override
	public boolean hasCapability( @Nonnull Capability<?> capability )
	{
		if( !this.isOutput() )
		{
			if( capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER )
			{
				return true;
			}
		}
		return super.hasCapability( capability );
	}

	@Nullable
	@Override
	public <T> T getCapability( @Nonnull Capability<T> capability )
	{
		if( capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER )
		{
			if( this.isOutput() )
			{
				return null;
			}
			return (T) this.inputHandler;
		}
		return super.getCapability( capability );
	}

	class InputEnergyStorage implements IEnergyContainer
	{
		@Override
		public long getEnergyCanBeInserted()
		{
			long canInsert = 0;
			if( outputs.isEmpty() )
			{
				try
				{
					for( PartP2PGTCEPower o : PartP2PGTCEPower.this.getOutputs() )
						outputs.add( o );
				}
				catch( GridAccessException e )
				{
					e.printStackTrace();
				}
			}

			while ( !outputs.isEmpty() )
			{
				PartP2PGTCEPower target = outputs.poll();
				final IEnergyContainer output = target.getAttachedEnergyStorage();
				if( output == this )
				{
					return 0;
				}
				if( output == null || output.getEnergyCanBeInserted() <= 0 )
				{
					continue;
				}
				canInsert += output.getEnergyCanBeInserted();
			}
			return canInsert;
		}

		@Override
		public long acceptEnergyFromNetwork( EnumFacing facing, long voltage, long amperage )
		{
			long amperesUsed = 0L;

			if( outputs.isEmpty() )
			{
				try
				{
					for( PartP2PGTCEPower o : PartP2PGTCEPower.this.getOutputs() )
						outputs.add( o );
				}
				catch( GridAccessException e )
				{
					e.printStackTrace();
				}
			}

			voltage = (long) ( voltage * 0.95 );

			if( voltage > 0 )
			{
				while ( !outputs.isEmpty() )
				{
					PartP2PGTCEPower target = outputs.poll();
					final IEnergyContainer output = target.getAttachedEnergyStorage();

					if( output == null || !output.inputsEnergy( target.getSide().getFacing().getOpposite() ) || output.getEnergyCanBeInserted() <= 0 )
					{
						continue;
					}

					amperesUsed += output.acceptEnergyFromNetwork( target.getSide().getFacing().getOpposite(), voltage, amperage - amperesUsed );

					if( amperage >= amperesUsed )
					{
						outputs.clear();
						break;
					}
				}
			}
			return amperesUsed;
		}

		@Override
		public boolean inputsEnergy( EnumFacing enumFacing )
		{
			return true;
		}

		@Override
		public long changeEnergy( long l )
		{
			return 0;
		}

		@Override
		public long getEnergyStored()
		{
			return 0;
		}

		@Override
		public long getEnergyCapacity()
		{
			return 0;
		}

		@Override
		public long getInputAmperage()
		{
			return 0;
		}

		@Override
		public long getInputVoltage()
		{
			return 0;
		}
	}

	static class NullEnergyStorage implements IEnergyContainer
	{
		@Override
		public long acceptEnergyFromNetwork( EnumFacing enumFacing, long l, long l1 )
		{
			return 0;
		}

		@Override
		public boolean inputsEnergy( EnumFacing enumFacing )
		{
			return false;
		}

		@Override
		public long changeEnergy( long l )
		{
			return 0;
		}

		@Override
		public long getEnergyStored()
		{
			return 0;
		}

		@Override
		public long getEnergyCapacity()
		{
			return 0;
		}

		@Override
		public long getInputAmperage()
		{
			return 0;
		}

		@Override
		public long getInputVoltage()
		{
			return 0;
		}
	}
}
