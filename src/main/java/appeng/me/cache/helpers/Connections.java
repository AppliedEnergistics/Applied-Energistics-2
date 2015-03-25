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

package appeng.me.cache.helpers;


import java.util.HashMap;
import java.util.concurrent.Callable;

import appeng.api.networking.IGridNode;
import appeng.parts.p2p.PartP2PTunnelME;


public class Connections implements Callable
{

	final public HashMap<IGridNode, TunnelConnection> connections = new HashMap<IGridNode, TunnelConnection>();
	final private PartP2PTunnelME me;
	public boolean create = false;
	public boolean destroy = false;

	public Connections( PartP2PTunnelME o )
	{
		this.me = o;
	}

	@Override
	public Object call() throws Exception
	{
		this.me.updateConnections( this );

		return null;
	}

	public void markDestroy()
	{
		this.create = false;
		this.destroy = true;
	}

	public void markCreate()
	{
		this.create = true;
		this.destroy = false;
	}
}
