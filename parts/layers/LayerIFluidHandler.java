package appeng.api.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;

public class LayerIFluidHandler extends LayerBase implements IFluidHandler
{

	static final FluidTankInfo[] emptyList = new FluidTankInfo[0];

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		IPart part = getPart( from );
		if ( part instanceof IFluidHandler )
			return ((IFluidHandler) part).fill( from, resource, doFill );
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		IPart part = getPart( from );
		if ( part instanceof IFluidHandler )
			return ((IFluidHandler) part).drain( from, resource, doDrain );
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		IPart part = getPart( from );
		if ( part instanceof IFluidHandler )
			return ((IFluidHandler) part).drain( from, maxDrain, doDrain );
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, net.minecraftforge.fluids.Fluid fluid)
	{
		IPart part = getPart( from );
		if ( part instanceof IFluidHandler )
			return ((IFluidHandler) part).canFill( from, fluid );
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, net.minecraftforge.fluids.Fluid fluid)
	{
		IPart part = getPart( from );
		if ( part instanceof IFluidHandler )
			return ((IFluidHandler) part).canDrain( from, fluid );
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		IPart part = getPart( from );
		if ( part instanceof IFluidHandler )
			return ((IFluidHandler) part).getTankInfo( from );
		return emptyList;
	}

}
