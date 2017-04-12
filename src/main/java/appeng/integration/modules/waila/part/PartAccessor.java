/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.waila.part;


import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import com.google.common.base.Optional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;


/**
 * Accessor to access specific parts for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class PartAccessor
{
	/**
	 * Hits a {@link appeng.api.parts.IPartHost} with {@link net.minecraft.util.MovingObjectPosition}.
	 * <p/>
	 * You can derive the looked at {@link appeng.api.parts.IPart} by doing that. If a facade is being looked at, it is
	 * defined as being absent.
	 *
	 * @param te  being looked at {@link net.minecraft.tileentity.TileEntity}
	 * @param mop type of ray-trace
	 * @return maybe the looked at {@link appeng.api.parts.IPart}
	 */
	public Optional<IPart> getMaybePart( final TileEntity te, final MovingObjectPosition mop )
	{
		if( te instanceof IPartHost )
		{
			final Vec3 position = mop.hitVec.addVector( -mop.blockX, -mop.blockY, -mop.blockZ );
			final IPartHost host = (IPartHost) te;
			final SelectedPart sp = host.selectPart( position );

			if( sp.part != null )
			{
				return Optional.of( sp.part );
			}
		}

		return Optional.absent();
	}
}
