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

package appeng.integration.modules;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import rblocks.api.IOrientable;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IRB;

public class RB extends BaseModule implements IRB
{

	private class RBWrapper implements appeng.api.util.IOrientable
	{

		final private IOrientable internal;

		public RBWrapper(IOrientable ww) {
			internal = ww;
		}

		@Override
		public boolean canBeRotated()
		{
			return internal.canBeRotated();
		}

		@Override
		public ForgeDirection getForward()
		{
			return internal.getForward();
		}

		@Override
		public ForgeDirection getUp()
		{
			return internal.getUp();
		}

		@Override
		public void setOrientation(ForgeDirection Forward, ForgeDirection Up)
		{
			internal.setOrientation( Forward, Up );
		}

	}

	public static RB instance;

	@Override
	public void Init() throws Throwable
	{
		TestClass( IOrientable.class );
	}

	@Override
	public void PostInit()
	{

	}

	@Override
	public appeng.api.util.IOrientable getOrientable(TileEntity te)
	{
		if ( te instanceof IOrientable )
			return new RBWrapper( (IOrientable) te );
		return null;
	}

}
