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

package appeng.services.version;


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * AE prints version like rv2-beta-8
 * GitHub prints version like rv2.beta.8
 */
public final class DefaultVersion extends BaseVersion
{
	/**
	 * @param revision natural number
	 * @param channel  either alpha, beta or release
	 * @param build    natural number
	 */
	public DefaultVersion( @Nonnegative final int revision, @Nonnull final Channel channel, @Nonnegative final int build )
	{
		super( revision, channel, build );
	}

	@Override
	public boolean isNewerAs( final Version maybeOlder )
	{
		if( this.revision() == maybeOlder.revision() )
		{
			if( this.channel().compareTo( maybeOlder.channel() ) == 0 )
			{
				return this.build() > maybeOlder.build();
			}
			return this.channel().compareTo( maybeOlder.channel() ) > 0;
		}
		else
			return this.revision() > maybeOlder.revision();
	}
}
