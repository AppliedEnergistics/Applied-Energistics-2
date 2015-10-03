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

package appeng.core.sync.network;


import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import appeng.core.sync.AppEngPacket;


public class PacketLogger extends LoggingHandler
{
	private String side;

	public PacketLogger( String side )
	{
		super( LogLevel.INFO );

		this.side = side;
	}

	@Override
	protected String formatMessage( String eventName, Object msg )
	{
		if( msg instanceof AppEngPacket )
		{
			AppEngPacket packet = (AppEngPacket) msg;
			return msg.getClass().getName() + " { side=" + this.side + ", toString=" + packet.toString() + " }";
		}
		else
		{
			return super.formatMessage( eventName, msg );
		}
	}
}