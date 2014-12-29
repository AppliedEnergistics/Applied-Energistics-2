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

package appeng.container.guisync;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketProgressBar;
import appeng.core.sync.packets.PacketValueConfig;

public class SyncData
{

	private Object clientVersion;

	private final AEBaseContainer source;
	private final Field field;

	private final int channel;

	public SyncData(AEBaseContainer container, Field field, GuiSync annotation) {
		this.clientVersion = null;
		this.source = container;
		this.field = field;
		this.channel = annotation.value();
	}

	public int getChannel()
	{
		return this.channel;
	}

	public void tick(ICrafting c)
	{
		try
		{
			Object val = this.field.get( this.source );
			if ( val != null && this.clientVersion == null )
				this.send( c, val );
			else if ( !val.equals( this.clientVersion ) )
				this.send( c, val );
		}
		catch (IllegalArgumentException e)
		{
			AELog.error( e );
		}
		catch (IllegalAccessException e)
		{
			AELog.error( e );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	public void update(Object val)
	{
		try
		{
			Object oldValue = this.field.get( this.source );
			if ( val instanceof String )
				this.updateString( oldValue, (String) val );
			else
				this.updateValue( oldValue, (Long) val );
		}
		catch (IllegalArgumentException e)
		{
			AELog.error( e );
		}
		catch (IllegalAccessException e)
		{
			AELog.error( e );
		}

	}

	private void updateString(Object oldValue, String val)
	{
		try
		{
			this.field.set( this.source, val );
		}
		catch (IllegalArgumentException e)
		{
			AELog.error( e );
		}
		catch (IllegalAccessException e)
		{
			AELog.error( e );
		}
	}

	private void updateValue(Object oldValue, long val)
	{
		try
		{
			if ( this.field.getType().isEnum() )
			{
				EnumSet<? extends Enum> valList = EnumSet.allOf( (Class<? extends Enum>) this.field.getType() );
				for (Enum e : valList)
				{
					if ( e.ordinal() == val )
					{
						this.field.set( this.source, e );
						break;
					}
				}
			}
			else
			{
				if ( this.field.getType().equals( int.class ) )
					this.field.set( this.source, (int) val );
				else if ( this.field.getType().equals( long.class ) )
					this.field.set( this.source, val );
				else if ( this.field.getType().equals( boolean.class ) )
					this.field.set( this.source, val == 1 );
				else if ( this.field.getType().equals( Integer.class ) )
					this.field.set( this.source, (int) val );
				else if ( this.field.getType().equals( Long.class ) )
					this.field.set( this.source, val );
				else if ( this.field.getType().equals( Boolean.class ) )
					this.field.set( this.source, val == 1 );
			}

			this.source.onUpdate( this.field.getName(), oldValue, this.field.get( this.source ) );
		}
		catch (IllegalArgumentException e)
		{
			AELog.error( e );
		}
		catch (IllegalAccessException e)
		{
			AELog.error( e );
		}
	}

	private void send(ICrafting o, Object val) throws IOException
	{
		if ( val instanceof String )
		{
			if ( o instanceof EntityPlayerMP )
				NetworkHandler.instance.sendTo( new PacketValueConfig( "SyncDat." + this.channel, (String) val ), (EntityPlayerMP) o );
		}
		else if ( this.field.getType().isEnum() )
		{
			o.sendProgressBarUpdate( this.source, this.channel, ((Enum) val).ordinal() );
		}
		else if ( val instanceof Long || val.getClass() == long.class )
		{
			NetworkHandler.instance.sendTo( new PacketProgressBar( this.channel, (Long) val ), (EntityPlayerMP) o );
		}
		else if ( val instanceof Boolean || val.getClass() == boolean.class )
		{
			o.sendProgressBarUpdate( this.source, this.channel, ((Boolean) val) ? 1 : 0 );
		}
		else
		{
			o.sendProgressBarUpdate( this.source, this.channel, (Integer) val );
		}

		this.clientVersion = val;
	}
}
