package appeng.parts.p2p;

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
import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Interface(iface = "buildcraft.api.power.IPowerReceptor", modid = "BuildCraftAPI|core")
public class PartP2PBCPower extends PartP2PTunnel<PartP2PBCPower> implements IPowerReceptor, IGridTickable
{

	PowerHandler pp;

	public TunnelType getTunnelType()
	{
		return TunnelType.BC_POWER;
	}

	public PartP2PBCPower(ItemStack is) {
		super( is );
		pp = new PowerHandler( this, Type.MACHINE );
		pp.configure( 1f, 320f, 800f, 640f );
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 1, 20, false, false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( !output && proxy.isActive() )
		{
			float totalRequiredPower = 0.0f;
			TunnelCollection<PartP2PBCPower> tunnelset;

			try
			{
				tunnelset = getOutputs();
			}
			catch (GridAccessException e)
			{
				return TickRateModulation.IDLE;
			}

			for (PartP2PBCPower o : tunnelset)
			{
				IPowerReceptor target = o.getPowerTarget();
				if ( target != null )
				{
					PowerReceiver tp = target.getPowerReceiver( side.getOpposite() );
					if ( tp != null )
					{
						float howmuch = tp.powerRequest();

						if ( howmuch > tp.getMaxEnergyReceived() )
							howmuch = tp.getMaxEnergyReceived();

						if ( howmuch > 0.01 && howmuch > tp.getMinEnergyReceived() )
						{
							totalRequiredPower += howmuch;
						}
					}
				}
			}

			if ( totalRequiredPower < 0.1 )
				return TickRateModulation.SLOWER;

			float currentTotal = pp.getEnergyStored();
			if ( currentTotal < 0.01 )
				return TickRateModulation.SLOWER;

			for (PartP2PBCPower o : tunnelset)
			{
				IPowerReceptor target = o.getPowerTarget();
				if ( target != null )
				{
					PowerReceiver tp = target.getPowerReceiver( side.getOpposite() );
					if ( tp != null )
					{
						float howmuch = tp.powerRequest(); // orientation.getOpposite()
															// );
						if ( howmuch > tp.getMaxEnergyReceived() )
							howmuch = tp.getMaxEnergyReceived();

						if ( howmuch > 0.01 && howmuch > tp.getMinEnergyReceived() )
						{
							float toPull = currentTotal * (howmuch / totalRequiredPower);
							float pulled = pp.useEnergy( 0, toPull, true );
							QueueTunnelDrain( PowerUnits.MJ, pulled * AEConfig.TunnelPowerLoss );

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
	};

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
		pp.writeToNBT( tag );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		pp.readFromNBT( tag );
	}

	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.emerald_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if ( side.equals( side ) )
			return pp.getPowerReceiver();
		return null;
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{

	}

	@Override
	public World getWorld()
	{
		return tile.getWorldObj();
	}

}
