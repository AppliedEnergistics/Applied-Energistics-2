package appeng.parts.p2p;

import appeng.integration.abstraction.helpers.BaseMJPerdition;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.api.config.TunnelType;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IMJ5;
import appeng.integration.abstraction.IMJ6;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.transformer.annotations.integration.Method;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.ISidedBatteryProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@InterfaceList(value = { @Interface(iface = "buildcraft.api.mj.ISidedBatteryProvider", iname = "MJ6"),
		@Interface(iface = "buildcraft.api.mj.IBatteryObject", iname = "MJ6"), @Interface(iface = "buildcraft.api.power.IPowerReceptor", iname = "MJ5"),
		@Interface(iface = "appeng.api.networking.ticking.IGridTickable", iname = "MJ5") })
public class PartP2PBCPower extends PartP2PTunnel<PartP2PBCPower> implements IPowerReceptor, ISidedBatteryProvider, IBatteryObject, IGridTickable
{

	BaseMJPerdition pp;

	@Override
	public TunnelType getTunnelType()
	{
		return TunnelType.BC_POWER;
	}

	public PartP2PBCPower(ItemStack is) {
		super( is );

		if ( !AppEng.instance.isIntegrationEnabled( IntegrationType.MJ5 ) && !AppEng.instance.isIntegrationEnabled( IntegrationType.MJ6 ) )
			throw new RuntimeException( "MJ Not installed!" );

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.MJ5 ) )
		{
			pp = (BaseMJPerdition) ((IMJ5) AppEng.instance.getIntegration( IntegrationType.MJ5 )).createPerdition( this );
			if ( pp != null )
				pp.configure( 1, 380, 1.0f / 5.0f, 1000 );
		}
	}

	@Override
	@Method(iname = "MJ5")
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.MJTunnel.min, TickRates.MJTunnel.max, false, false );
	}

	@Override
	@Method(iname = "MJ5")
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( !output && proxy.isActive() )
		{
			float totalRequiredPower = 0.0f;
			TunnelCollection<PartP2PBCPower> tunnels;

			try
			{
				tunnels = getOutputs();
			}
			catch (GridAccessException e)
			{
				return TickRateModulation.IDLE;
			}

			for (PartP2PBCPower o : tunnels)
			{
				IPowerReceptor target = o.getPowerTarget();
				if ( target != null )
				{
					PowerReceiver tp = target.getPowerReceiver( side.getOpposite() );
					if ( tp != null )
					{
						double request = tp.powerRequest();

						if ( request > tp.getMaxEnergyReceived() )
							request = tp.getMaxEnergyReceived();

						if ( request > 0.01 && request > tp.getMinEnergyReceived() )
						{
							totalRequiredPower += request;
						}
					}
				}
			}

			if ( totalRequiredPower < 0.1 )
				return TickRateModulation.SLOWER;

			double currentTotal = pp.getPowerReceiver().getEnergyStored();
			if ( currentTotal < 0.01 )
				return TickRateModulation.SLOWER;

			for (PartP2PBCPower o : tunnels)
			{
				IPowerReceptor target = o.getPowerTarget();
				if ( target != null )
				{
					PowerReceiver tp = target.getPowerReceiver( side.getOpposite() );
					if ( tp != null )
					{
						double request = tp.powerRequest();

						if ( request > tp.getMaxEnergyReceived() )
							request = tp.getMaxEnergyReceived();

						if ( request > 0.01 && request > tp.getMinEnergyReceived() )
						{
							double toPull = currentTotal * (request / totalRequiredPower);
							double pulled = pp.useEnergy( 0, toPull, true );
							QueueTunnelDrain( PowerUnits.MJ, pulled );

							tp.receiveEnergy( Type.PIPE, pulled, o.side.getOpposite() );
						}
					}
				}
			}

			return TickRateModulation.FASTER;
		}

		return TickRateModulation.SLOWER;
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}

	@Method(iname = "MJ6")
	private IBatteryObject getTargetBattery()
	{
		TileEntity te = getWorld().getTileEntity( tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ );
		if ( te != null )
		{
			IBatteryObject bo = MjAPI.getMjBattery( te, MjAPI.DEFAULT_POWER_FRAMEWORK, side.getOpposite() );
			if ( bo != null )
				return bo;
			
			return ((IMJ6) AppEng.instance.getIntegration( IntegrationType.MJ6 )).provider( te, side.getOpposite() );
		}
		return null;
	}

	@Method(iname = "MJ5")
	private IPowerReceptor getPowerTarget()
	{
		TileEntity te = getWorld().getTileEntity( tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ );
		if ( te != null )
		{
			if ( te instanceof IPowerReceptor )
				return (IPowerReceptor) te;
		}
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
		if ( pp != null )
			pp.writeToNBT( tag );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		if ( pp != null )
			pp.readFromNBT( tag );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.emerald_block.getBlockTextureFromSide( 0 );
	}

	@Override
	@Method(iname = "MJ5")
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if ( side.equals( side ) )
			return ((BaseMJPerdition) pp).getPowerReceiver();
		return null;
	}

	@Override
	@Method(iname = "MJ5")
	public void doWork(PowerHandler workProvider)
	{

	}

	@Override
	public World getWorld()
	{
		return tile.getWorldObj();
	}

	@Override
	@Method(iname = "MJ6")
	public IBatteryObject getMjBattery(String kind, ForgeDirection direction)
	{
		return this;
	}

	@Override
	@Method(iname = "MJ6")
	public double getEnergyRequested()
	{
		try
		{
			double totalRequiredPower = 0.0f;

			for (PartP2PBCPower g : getOutputs())
			{
				IBatteryObject o = g.getTargetBattery();
				if ( o != null )
					totalRequiredPower += o.getEnergyRequested();
			}

			return totalRequiredPower;
		}
		catch (GridAccessException e)
		{
			return 0;
		}
	}

	@Override
	@Method(iname = "MJ6")
	public double addEnergy(double mj)
	{
		return addEnergyInternal( mj, false, false );
	}

	@Override
	@Method(iname = "MJ6")
	public double addEnergy(double mj, boolean ignoreCycleLimit)
	{
		return addEnergyInternal( mj, true, ignoreCycleLimit );
	}

	@Method(iname = "MJ6")
	private double addEnergyInternal(double mj, boolean cycleLimitMode, boolean ignoreCycleLimit)
	{
		if ( output || !proxy.isActive() )
			return 0;

		double originalInput = mj;

		try
		{
			TunnelCollection<PartP2PBCPower> outs = getOutputs();

			double outputs = 0;
			for (PartP2PBCPower g : outs)
			{
				IBatteryObject o = g.getTargetBattery();
				if ( o != null )
				{
					outputs = outputs + 1.0;
				}
			}

			if ( outputs < 0.0000001 )
				return 0;

			for (PartP2PBCPower g : outs)
			{
				IBatteryObject o = g.getTargetBattery();
				if ( o != null )
				{
					double fraction = originalInput / outputs;
					if ( cycleLimitMode )
						fraction = o.addEnergy( fraction );
					else
						fraction = o.addEnergy( fraction, ignoreCycleLimit );
					mj -= fraction;
				}
			}

			if ( mj > 0 )
			{
				for (PartP2PBCPower g : outs)
				{
					IBatteryObject o = g.getTargetBattery();
					if ( o != null )
					{
						if ( cycleLimitMode )
							mj = mj - o.addEnergy( mj );
						else
							mj = mj - o.addEnergy( mj, ignoreCycleLimit );
					}
				}
			}

			return originalInput - mj;
		}
		catch (GridAccessException e)
		{
			return 0;
		}
	}

	@Override
	@Method(iname = "MJ6")
	public double getEnergyStored()
	{
		try
		{
			double totalRequiredPower = 0.0f;

			for (PartP2PBCPower g : getOutputs())
			{
				IBatteryObject o = g.getTargetBattery();
				if ( o != null )
					totalRequiredPower += o.getEnergyStored();
			}

			return totalRequiredPower;
		}
		catch (GridAccessException e)
		{
			return 0;
		}
	}

	@Override
	@Method(iname = "MJ6")
	public void setEnergyStored(double mj)
	{
		// EHh?!
	}

	@Override
	public double maxCapacity()
	{
		try
		{
			double totalRequiredPower = 0.0f;

			for (PartP2PBCPower g : getOutputs())
			{
				IBatteryObject o = g.getTargetBattery();
				if ( o != null )
					totalRequiredPower += o.maxCapacity();
			}

			return totalRequiredPower;
		}
		catch (GridAccessException e)
		{
			return 0;
		}
	}

	@Override
	@Method(iname = "MJ6")
	public double minimumConsumption()
	{
		try
		{
			double totalRequiredPower = 1000000000000.0;

			for (PartP2PBCPower g : getOutputs())
			{
				IBatteryObject o = g.getTargetBattery();
				if ( o != null )
					totalRequiredPower = Math.min( totalRequiredPower, o.minimumConsumption() );
			}

			return totalRequiredPower;
		}
		catch (GridAccessException e)
		{
			return 0;
		}
	}

	@Override
	@Method(iname = "MJ6")
	public double maxReceivedPerCycle()
	{
		try
		{
			double totalRequiredPower = 1000000.0;

			for (PartP2PBCPower g : getOutputs())
			{
				IBatteryObject o = g.getTargetBattery();
				if ( o != null )
					totalRequiredPower = Math.min( totalRequiredPower, o.maxReceivedPerCycle() );
			}

			return totalRequiredPower;
		}
		catch (GridAccessException e)
		{
			return 0;
		}
	}

	@Override
	@Method(iname = "MJ6")
	public IBatteryObject reconfigure(double maxCapacity, double maxReceivedPerCycle, double minimumConsumption)
	{
		return this;
	}

	@Override
	@Method(iname = "MJ6")
	public String kind()
	{
		return "tunnel";
	}

}
