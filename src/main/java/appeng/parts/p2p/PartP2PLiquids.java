package appeng.parts.p2p;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.config.TunnelType;
import appeng.me.GridAccessException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartP2PLiquids extends PartP2PTunnel<PartP2PLiquids> implements IFluidHandler
{

	private final static FluidTankInfo[] activeTank = new FluidTankInfo[] { new FluidTankInfo( null, 10000 ) };
	private final static FluidTankInfo[] inactiveTank = new FluidTankInfo[] { new FluidTankInfo( null, 0 ) };

	public TunnelType getTunnelType()
	{
		return TunnelType.FLUID;
	}

	public PartP2PLiquids(ItemStack is) {
		super( is );
	}

	private FluidTankInfo[] getTank()
	{
		if ( output )
		{
			PartP2PLiquids tun = getInput();
			if ( tun != null )
				return activeTank;
		}
		else
		{
			try
			{
				if ( !getOutputs().isEmpty() )
					return activeTank;
			}
			catch (GridAccessException e)
			{
				// :(
			}
		}
		return inactiveTank;
	}

	IFluidHandler cachedTank;

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	private int tmpUsed;

	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.lapis_block.getBlockTextureFromSide( 0 );
	}

	List<PartP2PLiquids> getOutputs(Fluid input)
	{
		List<PartP2PLiquids> outs = new LinkedList<PartP2PLiquids>();

		try
		{
			for (PartP2PLiquids l : getOutputs())
			{
				IFluidHandler handler = l.getTarget();
				if ( handler != null )
				{
					if ( handler.canFill( l.side.getOpposite(), input ) )
						outs.add( l );
				}
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return outs;
	}

	@Override
	public void onNeighborChanged()
	{
		cachedTank = null;
		if ( output )
		{
			PartP2PLiquids in = getInput();
			if ( in != null )
				in.onTunnelNetworkChange();
		}
	}

	@Override
	public void onTunnelNetworkChange()
	{
		cachedTank = null;
	}

	IFluidHandler getTarget()
	{
		if ( !proxy.isActive() )
			return null;

		if ( cachedTank != null )
			return cachedTank;

		TileEntity te = tile.getWorldObj().getTileEntity( tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ );
		if ( te instanceof IFluidHandler )
			return cachedTank = (IFluidHandler) te;

		return null;
	}

	static final ThreadLocal<Stack<PartP2PLiquids>> depth = new ThreadLocal<Stack<PartP2PLiquids>>();

	private Stack<PartP2PLiquids> getDepth()
	{
		Stack<PartP2PLiquids> s = depth.get();

		if ( s == null )
			depth.set( s = new Stack<PartP2PLiquids>() );

		return s;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		Stack<PartP2PLiquids> stack = getDepth();

		for (PartP2PLiquids t : stack)
			if ( t == this )
				return 0;

		stack.push( this );

		List<PartP2PLiquids> list = getOutputs( resource.getFluid() );
		int requestTotal = 0;

		Iterator<PartP2PLiquids> i = list.iterator();
		while (i.hasNext())
		{
			PartP2PLiquids l = i.next();
			IFluidHandler tank = l.getTarget();
			if ( tank != null )
				l.tmpUsed = tank.fill( l.side.getOpposite(), resource.copy(), false );
			else
				l.tmpUsed = 0;

			if ( l.tmpUsed <= 0 )
				i.remove();
			else
				requestTotal += l.tmpUsed;
		}

		if ( requestTotal <= 0 )
		{
			if ( stack.pop() != this )
				throw new RuntimeException( "Invalid Recursion detected." );

			return 0;
		}

		if ( !doFill )
		{
			if ( stack.pop() != this )
				throw new RuntimeException( "Invalid Recursion detected." );

			return Math.min( resource.amount, requestTotal );
		}

		int available = resource.amount;
		int used = 0;

		i = list.iterator();
		while (i.hasNext())
		{
			PartP2PLiquids l = i.next();

			FluidStack insert = resource.copy();
			insert.amount = (int) Math.ceil( insert.amount * ((double) l.tmpUsed / (double) requestTotal) );
			if ( insert.amount > available )
				insert.amount = available;

			IFluidHandler tank = l.getTarget();
			if ( tank != null )
				l.tmpUsed = tank.fill( l.side.getOpposite(), insert.copy(), true );
			else
				l.tmpUsed = 0;

			available -= insert.amount;
			used += insert.amount;
		}

		if ( stack.pop() != this )
			throw new RuntimeException( "Invalid Recursion detected." );

		return used;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return !output && from.equals( side ) && !getOutputs( fluid ).isEmpty();
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if ( from.equals( side ) )
			return getTank();
		return new FluidTankInfo[0];
	}

}
