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


import java.lang.reflect.Field;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;

import appeng.api.exceptions.ModNotInstalled;
import appeng.core.AEConfig;
import appeng.core.AELog;


public final class IntegrationNode
{

	final String displayName;
	final String modID;
	final IntegrationType shortName;
	IntegrationStage state = IntegrationStage.PRE_INIT;
	IntegrationStage failedStage = IntegrationStage.PRE_INIT;
	Throwable exception = null;
	String name = null;
	Class<?> classValue = null;
	Object instance;
	IIntegrationModule mod = null;

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
		return this.shortName.name() + ':' + this.state.name();
	}

	public boolean isActive()
	{
		if( this.state == IntegrationStage.PRE_INIT )
		{
			this.call( IntegrationStage.PRE_INIT );
		}

		return this.state != IntegrationStage.FAILED;
	}

	void call( final IntegrationStage stage )
	{
		if( this.state != IntegrationStage.FAILED )
		{
			if( this.state.ordinal() > stage.ordinal() )
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
							f.set( this.classValue, this.instance = this.mod );
						}
						else
						{
							throw new ModNotInstalled( this.modID );
						}

						this.state = IntegrationStage.INIT;

						break;
					case INIT:
						this.mod.init();
						this.state = IntegrationStage.POST_INIT;

						break;
					case POST_INIT:
						this.mod.postInit();
						this.state = IntegrationStage.READY;

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
				this.state = IntegrationStage.FAILED;
			}
		}

		if( stage == IntegrationStage.POST_INIT )
		{
			if( this.state == IntegrationStage.FAILED )
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
}
