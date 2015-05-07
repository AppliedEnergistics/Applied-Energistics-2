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


import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import appeng.api.parts.IPart;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationType;
import appeng.parts.automation.PartAnnihilationPlane;
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartImportBus;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartCableAnchor;
import appeng.parts.misc.PartInterface;
import appeng.parts.misc.PartInvertedToggleBus;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.misc.PartToggleBus;
import appeng.parts.networking.PartCableCovered;
import appeng.parts.networking.PartCableGlass;
import appeng.parts.networking.PartCableSmart;
import appeng.parts.networking.PartDenseCable;
import appeng.parts.networking.PartQuartzFiber;
import appeng.parts.p2p.PartP2PIC2Power;
import appeng.parts.p2p.PartP2PItems;
import appeng.parts.p2p.PartP2PLight;
import appeng.parts.p2p.PartP2PLiquids;
import appeng.parts.p2p.PartP2POpenComputers;
import appeng.parts.p2p.PartP2PRFPower;
import appeng.parts.p2p.PartP2PRedstone;
import appeng.parts.p2p.PartP2PTunnelME;
import appeng.parts.reporting.PartConversionMonitor;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartDarkMonitor;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.parts.reporting.PartMonitor;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartSemiDarkMonitor;
import appeng.parts.reporting.PartStorageMonitor;
import appeng.parts.reporting.PartTerminal;


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

	Monitor( 160, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartMonitor.class ),

	SemiDarkMonitor( 180, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartSemiDarkMonitor.class ),

	DarkMonitor( 200, EnumSet.of( AEFeature.Core ), EnumSet.noneOf( IntegrationType.class ), PartDarkMonitor.class ),

	StorageBus( 220, EnumSet.of( AEFeature.StorageBus ), EnumSet.noneOf( IntegrationType.class ), PartStorageBus.class ),

	ImportBus( 240, EnumSet.of( AEFeature.ImportBus ), EnumSet.noneOf( IntegrationType.class ), PartImportBus.class ),

	ExportBus( 260, EnumSet.of( AEFeature.ExportBus ), EnumSet.noneOf( IntegrationType.class ), PartExportBus.class ),

	LevelEmitter( 280, EnumSet.of( AEFeature.LevelEmitter ), EnumSet.noneOf( IntegrationType.class ), PartLevelEmitter.class ),

	AnnihilationPlane( 300, EnumSet.of( AEFeature.AnnihilationPlane ), EnumSet.noneOf( IntegrationType.class ), PartAnnihilationPlane.class ),

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

	P2PTunnelOpenComputers(468, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelOpenComputers ), EnumSet.of( IntegrationType.OpenComputers ), PartP2POpenComputers.class, GuiText.OCTunnel ),

	InterfaceTerminal( 480, EnumSet.of( AEFeature.InterfaceTerminal ), EnumSet.noneOf( IntegrationType.class ), PartInterfaceTerminal.class );

	public final int baseDamage;
	private final Set<AEFeature> features;
	private final Set<IntegrationType> integrations;
	private final Class<? extends IPart> myPart;
	private final GuiText extraName;
	public Constructor<? extends IPart> constructor;

	PartType( int baseMetaValue, Set<AEFeature> features, Set<IntegrationType> integrations, Class<? extends IPart> c )
	{
		this( baseMetaValue, features, integrations, c, null );
	}

	PartType( int baseMetaValue, Set<AEFeature> features, Set<IntegrationType> integrations, Class<? extends IPart> c, GuiText en )
	{
		this.features = Collections.unmodifiableSet( features ) ;
		this.integrations = Collections.unmodifiableSet( integrations );
		this.myPart = c;
		this.extraName = en;
		this.baseDamage = baseMetaValue;
	}

	public boolean isCable()
	{
		return false;
	}

	public Set<AEFeature> getFeature()
	{
		return this.features;
	}

	public Set<IntegrationType> getIntegrations()
	{
		return this.integrations;
	}

	public Class<? extends IPart> getPart()
	{
		return this.myPart;
	}

	public GuiText getExtraName()
	{
		return this.extraName;
	}

}
