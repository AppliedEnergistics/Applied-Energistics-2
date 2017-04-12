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

package appeng.items.parts;


import appeng.api.parts.IPart;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationType;
import appeng.parts.automation.*;
import appeng.parts.misc.*;
import appeng.parts.networking.*;
import appeng.parts.p2p.*;
import appeng.parts.reporting.*;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;


public enum PartType
{
	InvalidType( -1, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), null ),

	CableGlass( 0, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartCableGlass.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	CableCovered( 20, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartCableCovered.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	CableSmart( 40, EnumSet.of( AEFeature.Channels ), EnumSet.noneOf( IntegrationType.class ), PartCableSmart.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	CableDense( 60, EnumSet.of( AEFeature.Channels ), EnumSet.noneOf( IntegrationType.class ), PartDenseCable.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	ToggleBus( 80, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartToggleBus.class ),

	InvertedToggleBus( 100, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartInvertedToggleBus.class ),

	CableAnchor( 120, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartCableAnchor.class ),

	QuartzFiber( 140, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartQuartzFiber.class ),

	Monitor( 160, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartPanel.class ),

	SemiDarkMonitor( 180, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartSemiDarkPanel.class ),

	DarkMonitor( 200, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartDarkPanel.class ),

	StorageBus( 220, EnumSet.of( AEFeature.StorageBus ), EnumSet.noneOf( IntegrationType.class ), PartStorageBus.class ),

	ImportBus( 240, EnumSet.of( AEFeature.ImportBus ), EnumSet.noneOf( IntegrationType.class ), PartImportBus.class ),

	ExportBus( 260, EnumSet.of( AEFeature.ExportBus ), EnumSet.noneOf( IntegrationType.class ), PartExportBus.class ),

	LevelEmitter( 280, EnumSet.of( AEFeature.LevelEmitter ), EnumSet.noneOf( IntegrationType.class ), PartLevelEmitter.class ),

	AnnihilationPlane( 300, EnumSet.of( AEFeature.AnnihilationPlane ), EnumSet.noneOf( IntegrationType.class ), PartAnnihilationPlane.class ),

	IdentityAnnihilationPlane( 301, EnumSet.of( AEFeature.AnnihilationPlane, AEFeature.IdentityAnnihilationPlane ), EnumSet.noneOf( IntegrationType.class ), PartIdentityAnnihilationPlane.class ),

	FormationPlane( 320, EnumSet.of( AEFeature.FormationPlane ), EnumSet.noneOf( IntegrationType.class ), PartFormationPlane.class ),

	PatternTerminal( 340, EnumSet.of( AEFeature.Patterns ), EnumSet.noneOf( IntegrationType.class ), PartPatternTerminal.class ),

	CraftingTerminal( 360, EnumSet.of( AEFeature.CraftingTerminal ), EnumSet.noneOf( IntegrationType.class ), PartCraftingTerminal.class ),

	Terminal( 380, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartTerminal.class ),

	StorageMonitor( 400, EnumSet.of( AEFeature.StorageMonitor ), EnumSet.noneOf( IntegrationType.class ), PartStorageMonitor.class ),

	ConversionMonitor( 420, EnumSet.of( AEFeature.PartConversionMonitor ), EnumSet.noneOf( IntegrationType.class ), PartConversionMonitor.class ),

	Interface( 440, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartInterface.class ),

	P2PTunnelME( 460, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelME ), EnumSet.noneOf( IntegrationType.class ), PartP2PTunnelME.class, GuiText.METunnel ),

	P2PTunnelRedstone( 461, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelRedstone ), EnumSet.noneOf( IntegrationType.class ), PartP2PRedstone.class, GuiText.RedstoneTunnel ),

	P2PTunnelItems( 462, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelItems ), EnumSet.noneOf( IntegrationType.class ), PartP2PItems.class, GuiText.ItemTunnel ),

	P2PTunnelLiquids( 463, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelLiquids ), EnumSet.noneOf( IntegrationType.class ), PartP2PLiquids.class, GuiText.FluidTunnel ),

	P2PTunnelEU( 465, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelEU ), EnumSet.of( IntegrationType.IC2 ), PartP2PIC2Power.class, GuiText.EUTunnel ),

	P2PTunnelRF( 466, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelRF ), EnumSet.of( IntegrationType.RF ), PartP2PRFPower.class, GuiText.RFTunnel ),

	P2PTunnelLight( 467, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelLight ), EnumSet.noneOf( IntegrationType.class ), PartP2PLight.class, GuiText.LightTunnel ),

	P2PTunnelOpenComputers( 468, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelOpenComputers ), EnumSet.of( IntegrationType.OpenComputers ), PartP2POpenComputers.class, GuiText.OCTunnel ),

	P2PTunnelPressure( 469, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelPressure ), EnumSet.of( IntegrationType.PneumaticCraft ), PartP2PPressure.class, GuiText.PressureTunnel ),

	InterfaceTerminal( 480, EnumSet.of( AEFeature.InterfaceTerminal ), EnumSet.noneOf( IntegrationType.class ), PartInterfaceTerminal.class );

	private final int baseDamage;
	private final Set<AEFeature> features;
	private final Set<IntegrationType> integrations;
	private final Class<? extends IPart> myPart;
	private final GuiText extraName;
	private Constructor<? extends IPart> constructor;

	PartType( final int baseMetaValue, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c )
	{
		this( baseMetaValue, features, integrations, c, null );
	}

	PartType( final int baseMetaValue, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c, final GuiText en )
	{
		this.features = Collections.unmodifiableSet( features );
		this.integrations = Collections.unmodifiableSet( integrations );
		this.myPart = c;
		this.extraName = en;
		this.baseDamage = baseMetaValue;
	}

	public boolean isCable()
	{
		return false;
	}

	Set<AEFeature> getFeature()
	{
		return this.features;
	}

	Set<IntegrationType> getIntegrations()
	{
		return this.integrations;
	}

	Class<? extends IPart> getPart()
	{
		return this.myPart;
	}

	GuiText getExtraName()
	{
		return this.extraName;
	}

	Constructor<? extends IPart> getConstructor()
	{
		return this.constructor;
	}

	void setConstructor( final Constructor<? extends IPart> constructor )
	{
		this.constructor = constructor;
	}

	int getBaseDamage()
	{
		return this.baseDamage;
	}

}
