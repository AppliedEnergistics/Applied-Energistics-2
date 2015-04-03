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
import java.util.EnumSet;

import appeng.api.parts.IPart;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
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
	InvalidType( -1, EnumSet.of( AEFeature.Core ), null ),

	CableGlass( 0, EnumSet.of( AEFeature.Core ), PartCableGlass.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	CableCovered( 20, EnumSet.of( AEFeature.Core ), PartCableCovered.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	CableSmart( 40, EnumSet.of( AEFeature.Channels ), PartCableSmart.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	CableDense( 60, EnumSet.of( AEFeature.Channels ), PartDenseCable.class )
			{
				@Override
				public boolean isCable()
				{
					return true;
				}
			},

	ToggleBus( 80, EnumSet.of( AEFeature.Core ), PartToggleBus.class ),

	InvertedToggleBus( 100, EnumSet.of( AEFeature.Core ), PartInvertedToggleBus.class ),

	CableAnchor( 120, EnumSet.of( AEFeature.Core ), PartCableAnchor.class ),

	QuartzFiber( 140, EnumSet.of( AEFeature.Core ), PartQuartzFiber.class ),

	Monitor( 160, EnumSet.of( AEFeature.Core ), PartMonitor.class ),

	SemiDarkMonitor( 180, EnumSet.of( AEFeature.Core ), PartSemiDarkMonitor.class ),

	DarkMonitor( 200, EnumSet.of( AEFeature.Core ), PartDarkMonitor.class ),

	StorageBus( 220, EnumSet.of( AEFeature.StorageBus ), PartStorageBus.class ),

	ImportBus( 240, EnumSet.of( AEFeature.ImportBus ), PartImportBus.class ),

	ExportBus( 260, EnumSet.of( AEFeature.ExportBus ), PartExportBus.class ),

	LevelEmitter( 280, EnumSet.of( AEFeature.LevelEmitter ), PartLevelEmitter.class ),

	AnnihilationPlane( 300, EnumSet.of( AEFeature.AnnihilationPlane ), PartAnnihilationPlane.class ),

	FormationPlane( 320, EnumSet.of( AEFeature.FormationPlane ), PartFormationPlane.class ),

	PatternTerminal( 340, EnumSet.of( AEFeature.Patterns ), PartPatternTerminal.class ),

	CraftingTerminal( 360, EnumSet.of( AEFeature.CraftingTerminal ), PartCraftingTerminal.class ),

	Terminal( 380, EnumSet.of( AEFeature.Core ), PartTerminal.class ),

	StorageMonitor( 400, EnumSet.of( AEFeature.StorageMonitor ), PartStorageMonitor.class ),

	ConversionMonitor( 420, EnumSet.of( AEFeature.PartConversionMonitor ), PartConversionMonitor.class ),

	Interface( 440, EnumSet.of( AEFeature.Core ), PartInterface.class ),

	P2PTunnelME( 460, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelME ), PartP2PTunnelME.class, GuiText.METunnel ),

	P2PTunnelRedstone( 461, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelRedstone ), PartP2PRedstone.class, GuiText.RedstoneTunnel ),

	P2PTunnelItems( 462, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelItems ), PartP2PItems.class, GuiText.ItemTunnel ),

	P2PTunnelLiquids( 463, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelLiquids ), PartP2PLiquids.class, GuiText.FluidTunnel ),

	P2PTunnelEU( 465, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelEU ), PartP2PIC2Power.class, GuiText.EUTunnel ),

	P2PTunnelRF( 466, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelRF ), PartP2PRFPower.class, GuiText.RFTunnel ),

	P2PTunnelLight( 467, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelLight ), PartP2PLight.class, GuiText.LightTunnel ),

	InterfaceTerminal( 480, EnumSet.of( AEFeature.InterfaceTerminal ), PartInterfaceTerminal.class );

	public final int baseDamage;
	private final EnumSet<AEFeature> features;
	private final Class<? extends IPart> myPart;
	private final GuiText extraName;
	public Constructor<? extends IPart> constructor;

	PartType( int baseMetaValue, EnumSet<AEFeature> features, Class<? extends IPart> c )
	{
		this( baseMetaValue, features, c, null );
	}

	PartType( int baseMetaValue, EnumSet<AEFeature> features, Class<? extends IPart> c, GuiText en )
	{
		this.features = features;
		this.myPart = c;
		this.extraName = en;
		this.baseDamage = baseMetaValue;
	}

	public boolean isCable()
	{
		return false;
	}

	public EnumSet<AEFeature> getFeature()
	{
		return this.features;
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
