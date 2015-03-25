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
import appeng.api.util.AEColor;
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
	InvalidType( -1, AEFeature.Core, null ),

	CableGlass( 0, AEFeature.Core, PartCableGlass.class ),

	CableCovered( 20, AEFeature.Core, PartCableCovered.class ),

	CableSmart( 40, AEFeature.Channels, PartCableSmart.class ),

	CableDense( 60, AEFeature.Channels, PartDenseCable.class ),

	ToggleBus( 80, AEFeature.Core, PartToggleBus.class ),

	InvertedToggleBus( 100, AEFeature.Core, PartInvertedToggleBus.class ),

	CableAnchor( 120, AEFeature.Core, PartCableAnchor.class ),

	QuartzFiber( 140, AEFeature.Core, PartQuartzFiber.class ),

	Monitor( 160, AEFeature.Core, PartMonitor.class ),

	SemiDarkMonitor( 180, AEFeature.Core, PartSemiDarkMonitor.class ),

	DarkMonitor( 200, AEFeature.Core, PartDarkMonitor.class ),

	StorageBus( 220, AEFeature.StorageBus, PartStorageBus.class ),

	ImportBus( 240, AEFeature.ImportBus, PartImportBus.class ),

	ExportBus( 260, AEFeature.ExportBus, PartExportBus.class ),

	LevelEmitter( 280, AEFeature.LevelEmitter, PartLevelEmitter.class ),

	AnnihilationPlane( 300, AEFeature.AnnihilationPlane, PartAnnihilationPlane.class ),

	FormationPlane( 320, AEFeature.FormationPlane, PartFormationPlane.class ),

	PatternTerminal( 340, AEFeature.Patterns, PartPatternTerminal.class ),

	CraftingTerminal( 360, AEFeature.CraftingTerminal, PartCraftingTerminal.class ),

	Terminal( 380, AEFeature.Core, PartTerminal.class ),

	StorageMonitor( 400, AEFeature.StorageMonitor, PartStorageMonitor.class ),

	ConversionMonitor( 420, AEFeature.PartConversionMonitor, PartConversionMonitor.class ),

	Interface( 440, AEFeature.Core, PartInterface.class ),

	P2PTunnelME( 460, AEFeature.P2PTunnelME, PartP2PTunnelME.class, GuiText.METunnel ),

	P2PTunnelRedstone( 461, AEFeature.P2PTunnelRedstone, PartP2PRedstone.class, GuiText.RedstoneTunnel ),

	P2PTunnelItems( 462, AEFeature.P2PTunnelItems, PartP2PItems.class, GuiText.ItemTunnel ),

	P2PTunnelLiquids( 463, AEFeature.P2PTunnelLiquids, PartP2PLiquids.class, GuiText.FluidTunnel ),

	P2PTunnelEU( 465, AEFeature.P2PTunnelEU, PartP2PIC2Power.class, GuiText.EUTunnel ),

	P2PTunnelRF( 466, AEFeature.P2PTunnelRF, PartP2PRFPower.class, GuiText.RFTunnel ),

	P2PTunnelLight( 467, AEFeature.P2PTunnelLight, PartP2PLight.class, GuiText.LightTunnel ),

	InterfaceTerminal( 480, AEFeature.InterfaceTerminal, PartInterfaceTerminal.class );

	public final int baseDamage;
	private final EnumSet<AEFeature> features;
	private final Class<? extends IPart> myPart;
	private final GuiText extraName;
	public Constructor<? extends IPart> constructor;

	PartType( int baseMetaValue, AEFeature part, Class<? extends IPart> c )
	{
		this( baseMetaValue, part, c, null );
	}

	PartType( int baseMetaValue, AEFeature part, Class<? extends IPart> c, GuiText en )
	{
		this.features = EnumSet.of( part );
		this.myPart = c;
		this.extraName = en;
		this.baseDamage = baseMetaValue;
	}

	public Enum<AEColor>[] getVariants()
	{
		if( this == CableSmart || this == CableCovered || this == CableGlass || this == CableDense )
			return AEColor.values();

		return null;
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
