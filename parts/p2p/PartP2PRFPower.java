package appeng.parts.p2p;

import java.util.Stack;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.api.config.TunnelType;
import appeng.core.AppEng;
import appeng.integration.modules.helpers.NullRFHandler;
import appeng.me.GridAccessException;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.util.Platform;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@InterfaceList(value = { @Interface(iface = "cofh.api.energy.IEnergyHandler", iname = "RF") })
public class PartP2PRFPower extends PartP2PTunnel<PartP2PRFPower> implements cofh.api.energy.IEnergyHandler
{

	private static final IEnergyHandler myNullHandler = new NullRFHandler();

	boolean cachedTarget = false;
	IEnergyHandler outputTarget;

	public TunnelType getTunnelType()
	{
		return TunnelType.RF_POWER;
	}

	public PartP2PRFPower(ItemStack is) {
		super( is );

		if ( !AppEng.instance.isIntegrationEnabled( "RF" ) )
			throw new RuntimeException( "RF Not installed!" );
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		cachedTarget = false;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.iron_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public void onChange()
	{
		getHost().partChanged();
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}

	static final ThreadLocal<Stack<PartP2PRFPower>> depth = new ThreadLocal<Stack<PartP2PRFPower>>();

	private Stack<PartP2PRFPower> getDepth()
	{
		Stack<PartP2PRFPower> s = depth.get();

		if ( s == null )
			depth.set( s = new Stack<PartP2PRFPower>() );

		return s;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if ( output )
			return 0;

		if ( isActive() )
		{
			Stack<PartP2PRFPower> stack = getDepth();

			for (PartP2PRFPower t : stack)
				if ( t == this )
					return 0;

			stack.push( this );

			int total = 0;

			try
			{
				for (PartP2PRFPower t : getOutputs())
				{
					if ( Platform.getRandomInt() % 2 > 0 )
					{
						int recv = t.getOutput().receiveEnergy( t.side.getOpposite(), maxReceive, simulate );
						maxReceive -= recv;
						total += recv;

						if ( maxReceive <= 0 )
							break;
					}
				}

				if ( maxReceive > 0 )
				{
					for (PartP2PRFPower t : getOutputs())
					{
						int recv = t.getOutput().receiveEnergy( t.side.getOpposite(), maxReceive, simulate );
						maxReceive -= recv;
						total += recv;

						if ( maxReceive <= 0 )
							break;
					}
				}

				QueueTunnelDrain( PowerUnits.RF, total );
			}
			catch (GridAccessException e)
			{
			}

			if ( stack.pop() != this )
				throw new RuntimeException( "Invalid Recursion detected." );

			return total;
		}

		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if ( output || !isActive() )
			return 0;

		int total = 0;

		Stack<PartP2PRFPower> stack = getDepth();

		for (PartP2PRFPower t : stack)
			if ( t == this )
				return 0;

		stack.push( this );

		try
		{
			for (PartP2PRFPower t : getOutputs())
			{
				total += t.getOutput().getEnergyStored( t.side.getOpposite() );
			}
		}
		catch (GridAccessException e)
		{
			return 0;
		}

		if ( stack.pop() != this )
			throw new RuntimeException( "Invalid Recursion detected." );

		return total;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if ( output || !isActive() )
			return 0;

		int total = 0;

		Stack<PartP2PRFPower> stack = getDepth();

		for (PartP2PRFPower t : stack)
			if ( t == this )
				return 0;

		stack.push( this );

		try
		{
			for (PartP2PRFPower t : getOutputs())
			{
				total += t.getOutput().getMaxEnergyStored( t.side.getOpposite() );
			}
		}
		catch (GridAccessException e)
		{
			return 0;
		}

		if ( stack.pop() != this )
			throw new RuntimeException( "Invalid Recursion detected." );

		return total;
	}

	private IEnergyHandler getOutput()
	{
		if ( output )
		{
			if ( !cachedTarget )
			{
				TileEntity self = getTile();
				TileEntity te = self.getWorldObj().getTileEntity( self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );
				outputTarget = te instanceof IEnergyHandler ? (IEnergyHandler) te : null;
				cachedTarget = true;
			}

			if ( outputTarget == null )
				return myNullHandler;

			return outputTarget;
		}
		return myNullHandler;
	}

	@Deprecated
	public boolean canInterface(ForgeDirection from)
	{
		return true;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}
}
