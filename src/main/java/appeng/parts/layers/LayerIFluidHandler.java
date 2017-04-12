/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts.layers;


import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;


public class LayerIFluidHandler extends LayerBase implements IFluidHandler
{

	private static final FluidTankInfo[] EMPTY_LIST = new FluidTankInfo[0];

	@Override
	public int fill( final ForgeDirection from, final FluidStack resource, final boolean doFill )
	{
		final IPart part = this.getPart( from );
		if( part instanceof IFluidHandler )
		{
			return ( (IFluidHandler) part ).fill( from, resource, doFill );
		}
		return 0;
	}

	@Override
	public FluidStack drain( final ForgeDirection from, final FluidStack resource, final boolean doDrain )
	{
		final IPart part = this.getPart( from );
		if( part instanceof IFluidHandler )
		{
			return ( (IFluidHandler) part ).drain( from, resource, doDrain );
		}
		return null;
	}

	@Override
	public FluidStack drain( final ForgeDirection from, final int maxDrain, final boolean doDrain )
	{
		final IPart part = this.getPart( from );
		if( part instanceof IFluidHandler )
		{
			return ( (IFluidHandler) part ).drain( from, maxDrain, doDrain );
		}
		return null;
	}

	@Override
	public boolean canFill( final ForgeDirection from, final net.minecraftforge.fluids.Fluid fluid )
	{
		final IPart part = this.getPart( from );
		if( part instanceof IFluidHandler )
		{
			return ( (IFluidHandler) part ).canFill( from, fluid );
		}
		return false;
	}

	@Override
	public boolean canDrain( final ForgeDirection from, final net.minecraftforge.fluids.Fluid fluid )
	{
		final IPart part = this.getPart( from );
		if( part instanceof IFluidHandler )
		{
			return ( (IFluidHandler) part ).canDrain( from, fluid );
		}
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo( final ForgeDirection from )
	{
		final IPart part = this.getPart( from );
		if( part instanceof IFluidHandler )
		{
			return ( (IFluidHandler) part ).getTankInfo( from );
		}
		return EMPTY_LIST;
	}
}
