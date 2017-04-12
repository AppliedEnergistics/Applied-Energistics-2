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

package appeng.integration;


import appeng.api.exceptions.ModNotInstalled;
import appeng.core.AEConfig;
import appeng.core.AELog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;

import java.lang.reflect.Field;


public final class IntegrationNode
{

	private final String displayName;
	private final String modID;
	private final IntegrationType shortName;
	private IntegrationStage state = IntegrationStage.PRE_INIT;
	private IntegrationStage failedStage = IntegrationStage.PRE_INIT;
	private Throwable exception = null;
	private String name = null;
	private Class<?> classValue = null;
	private Object instance;
	private IIntegrationModule mod = null;

	public IntegrationNode( final String displayName, final String modID, final IntegrationType shortName, final String name )
	{
		this.displayName = displayName;
		this.shortName = shortName;
		this.modID = modID;
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.getShortName().name() + ':' + this.getState().name();
	}

	boolean isActive()
	{
		if( this.getState() == IntegrationStage.PRE_INIT )
		{
			this.call( IntegrationStage.PRE_INIT );
		}

		return this.getState() != IntegrationStage.FAILED;
	}

	void call( final IntegrationStage stage )
	{
		if( this.getState() != IntegrationStage.FAILED )
		{
			if( this.getState().ordinal() > stage.ordinal() )
			{
				return;
			}

			try
			{
				switch( stage )
				{
					case PRE_INIT:
						final ModAPIManager apiManager = ModAPIManager.INSTANCE;
						boolean enabled = this.modID == null || Loader.isModLoaded( this.modID ) || apiManager.hasAPI( this.modID );

						AEConfig.instance.addCustomCategoryComment( "ModIntegration", "Valid Values are 'AUTO', 'ON', or 'OFF' - defaults to 'AUTO' ; Suggested that you leave this alone unless your experiencing an issue, or wish to disable the integration for a reason." );
						final String mode = AEConfig.instance.get( "ModIntegration", this.displayName.replace( " ", "" ), "AUTO" ).getString();

						if( mode.toUpperCase().equals( "ON" ) )
						{
							enabled = true;
						}
						if( mode.toUpperCase().equals( "OFF" ) )
						{
							enabled = false;
						}

						if( enabled )
						{
							this.classValue = this.getClass().getClassLoader().loadClass( this.name );
							this.mod = (IIntegrationModule) this.classValue.getConstructor().newInstance();
							final Field f = this.classValue.getField( "instance" );
							f.set( this.classValue, this.setInstance( this.mod ) );
						}
						else
						{
							throw new ModNotInstalled( this.modID );
						}

						this.setState( IntegrationStage.INIT );

						break;
					case INIT:
						this.mod.init();
						this.setState( IntegrationStage.POST_INIT );

						break;
					case POST_INIT:
						this.mod.postInit();
						this.setState( IntegrationStage.READY );

						break;
					case FAILED:
					default:
						break;
				}
			}
			catch( final Throwable t )
			{
				this.failedStage = stage;
				this.exception = t;
				this.setState( IntegrationStage.FAILED );
			}
		}

		if( stage == IntegrationStage.POST_INIT )
		{
			if( this.getState() == IntegrationStage.FAILED )
			{
				AELog.info( this.displayName + " - Integration Disabled" );
				if( !( this.exception instanceof ModNotInstalled ) )
				{
					AELog.integration( this.exception );
				}
			}
			else
			{
				AELog.info( this.displayName + " - Integration Enable" );
			}
		}
	}

	Object getInstance()
	{
		return this.instance;
	}

	private Object setInstance( final Object instance )
	{
		this.instance = instance;
		return instance;
	}

	IntegrationType getShortName()
	{
		return this.shortName;
	}

	IntegrationStage getState()
	{
		return this.state;
	}

	private void setState( final IntegrationStage state )
	{
		this.state = state;
	}
}
