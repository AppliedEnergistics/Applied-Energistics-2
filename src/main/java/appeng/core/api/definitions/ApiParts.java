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

package appeng.core.api.definitions;


import appeng.api.definitions.IParts;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.parts.IPartHelper;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;
import appeng.core.features.DamagedItemDefinition;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;


/**
 * Internal implementation for the API parts
 */
public final class ApiParts implements IParts
{
	private final AEColoredItemDefinition cableSmart;
	private final AEColoredItemDefinition cableCovered;
	private final AEColoredItemDefinition cableGlass;
	private final AEColoredItemDefinition cableDense;
//	private final AEColoredItemDefinition lumenCableSmart;
//	private final AEColoredItemDefinition lumenCableCovered;
//	private final AEColoredItemDefinition lumenCableGlass;
//	private final AEColoredItemDefinition lumenCableDense;
	private final AEItemDefinition quartzFiber;
	private final AEItemDefinition toggleBus;
	private final AEItemDefinition invertedToggleBus;
	private final AEItemDefinition storageBus;
	private final AEItemDefinition importBus;
	private final AEItemDefinition exportBus;
	private final AEItemDefinition iface;
	private final AEItemDefinition levelEmitter;
	private final AEItemDefinition annihilationPlane;
	private final AEItemDefinition formationPlane;
	private final AEItemDefinition p2PTunnelME;
	private final AEItemDefinition p2PTunnelRedstone;
	private final AEItemDefinition p2PTunnelItems;
	private final AEItemDefinition p2PTunnelLiquids;
	private final AEItemDefinition p2PTunnelMJ;
	private final AEItemDefinition p2PTunnelEU;
	private final AEItemDefinition p2PTunnelRF;
	private final AEItemDefinition p2PTunnelLight;
	private final AEItemDefinition cableAnchor;
	private final AEItemDefinition monitor;
	private final AEItemDefinition semiDarkMonitor;
	private final AEItemDefinition darkMonitor;
	private final AEItemDefinition interfaceTerminal;
	private final AEItemDefinition patternTerminal;
	private final AEItemDefinition craftingTerminal;
	private final AEItemDefinition terminal;
	private final AEItemDefinition storageMonitor;
	private final AEItemDefinition conversionMonitor;

	public ApiParts( DefinitionConstructor constructor, IPartHelper partHelper )
	{
		final ItemMultiPart itemMultiPart = new ItemMultiPart( partHelper );
		constructor.registerAndConstructDefinition( itemMultiPart );

		this.cableSmart = constructor.constructColoredDefinition( itemMultiPart, PartType.CableSmart );
		this.cableCovered = constructor.constructColoredDefinition( itemMultiPart, PartType.CableCovered );
		this.cableGlass = constructor.constructColoredDefinition( itemMultiPart, PartType.CableGlass );
		this.cableDense = constructor.constructColoredDefinition( itemMultiPart, PartType.CableDense );
//		this.lumenCableSmart = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
//		this.lumenCableCovered = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
//		this.lumenCableGlass = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
//		this.lumenCableDense = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		this.quartzFiber = new DamagedItemDefinition( itemMultiPart.createPart( PartType.QuartzFiber ) );
		this.toggleBus = new DamagedItemDefinition( itemMultiPart.createPart( PartType.ToggleBus ) );
		this.invertedToggleBus = new DamagedItemDefinition( itemMultiPart.createPart( PartType.InvertedToggleBus ) );
		this.storageBus = new DamagedItemDefinition( itemMultiPart.createPart( PartType.StorageBus ) );
		this.importBus = new DamagedItemDefinition( itemMultiPart.createPart( PartType.ImportBus ) );
		this.exportBus = new DamagedItemDefinition( itemMultiPart.createPart( PartType.ExportBus ) );
		this.iface = new DamagedItemDefinition( itemMultiPart.createPart( PartType.Interface ) );
		this.levelEmitter = new DamagedItemDefinition( itemMultiPart.createPart( PartType.LevelEmitter ) );
		this.annihilationPlane = new DamagedItemDefinition( itemMultiPart.createPart( PartType.AnnihilationPlane ) );
		this.formationPlane = new DamagedItemDefinition( itemMultiPart.createPart( PartType.FormationPlane ) );
		this.p2PTunnelME = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelME ) );
		this.p2PTunnelRedstone = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelRedstone ) );
		this.p2PTunnelItems = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelItems ) );
		this.p2PTunnelLiquids = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelLiquids ) );
		this.p2PTunnelMJ = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelMJ ) );
		this.p2PTunnelEU = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelEU ) );
		this.p2PTunnelRF = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelRF ) );
		this.p2PTunnelLight = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelLight ) );
		this.cableAnchor = new DamagedItemDefinition( itemMultiPart.createPart( PartType.CableAnchor ) );
		this.monitor = new DamagedItemDefinition( itemMultiPart.createPart( PartType.Monitor ) );
		this.semiDarkMonitor = new DamagedItemDefinition( itemMultiPart.createPart( PartType.SemiDarkMonitor ) );
		this.darkMonitor = new DamagedItemDefinition( itemMultiPart.createPart( PartType.DarkMonitor ) );
		this.interfaceTerminal = new DamagedItemDefinition( itemMultiPart.createPart( PartType.InterfaceTerminal ) );
		this.patternTerminal = new DamagedItemDefinition( itemMultiPart.createPart( PartType.PatternTerminal ) );
		this.craftingTerminal = new DamagedItemDefinition( itemMultiPart.createPart( PartType.CraftingTerminal ) );
		this.terminal = new DamagedItemDefinition( itemMultiPart.createPart( PartType.Terminal ) );
		this.storageMonitor = new DamagedItemDefinition( itemMultiPart.createPart( PartType.StorageMonitor ) );
		this.conversionMonitor = new DamagedItemDefinition( itemMultiPart.createPart( PartType.ConversionMonitor ) );
	}

	@Override
	public AEColoredItemDefinition cableSmart()
	{
		return this.cableSmart;
	}

	@Override
	public AEColoredItemDefinition cableCovered()
	{
		return this.cableCovered;
	}

	@Override
	public AEColoredItemDefinition cableGlass()
	{
		return this.cableGlass;
	}

	@Override
	public AEColoredItemDefinition cableDense()
	{
		return this.cableDense;
	}

	@Override
	public AEColoredItemDefinition lumenCableSmart()
	{
		throw new MissingDefinition( "Lumen Smart Cable has yet to be implemented." );
//		return this.lumenCableSmart;
	}

	@Override
	public AEColoredItemDefinition lumenCableCovered()
	{
		throw new MissingDefinition( "Lumen Covered Cable has yet to be implemented." );
//		return this.lumenCableCovered;
	}

	@Override
	public AEColoredItemDefinition lumenCableGlass()
	{
		throw new MissingDefinition( "Lumen Glass Cable has yet to be implemented." );
//		return this.lumenCableGlass;
	}

	@Override
	public AEColoredItemDefinition lumenCableDense()
	{
		throw new MissingDefinition( "Lumen Dense Cable has yet to be implemented." );
//		return this.lumenCableDense;
	}

	@Override
	public AEItemDefinition quartzFiber()
	{
		return this.quartzFiber;
	}

	@Override
	public AEItemDefinition toggleBus()
	{
		return this.toggleBus;
	}

	@Override
	public AEItemDefinition invertedToggleBus()
	{
		return this.invertedToggleBus;
	}

	@Override
	public AEItemDefinition storageBus()
	{
		return this.storageBus;
	}

	@Override
	public AEItemDefinition importBus()
	{
		return this.importBus;
	}

	@Override
	public AEItemDefinition exportBus()
	{
		return this.exportBus;
	}

	@Override
	public AEItemDefinition iface()
	{
		return this.iface;
	}

	@Override
	public AEItemDefinition levelEmitter()
	{
		return this.levelEmitter;
	}

	@Override
	public AEItemDefinition annihilationPlane()
	{
		return this.annihilationPlane;
	}

	@Override
	public AEItemDefinition formationPlane()
	{
		return this.formationPlane;
	}

	@Override
	public AEItemDefinition p2PTunnelME()
	{
		return this.p2PTunnelME;
	}

	@Override
	public AEItemDefinition p2PTunnelRedstone()
	{
		return this.p2PTunnelRedstone;
	}

	@Override
	public AEItemDefinition p2PTunnelItems()
	{
		return this.p2PTunnelItems;
	}

	@Override
	public AEItemDefinition p2PTunnelLiquids()
	{
		return this.p2PTunnelLiquids;
	}

	@Override
	public AEItemDefinition p2PTunnelMJ()
	{
		return this.p2PTunnelMJ;
	}

	@Override
	public AEItemDefinition p2PTunnelEU()
	{
		return this.p2PTunnelEU;
	}

	@Override
	public AEItemDefinition p2PTunnelRF()
	{
		return this.p2PTunnelRF;
	}

	@Override
	public AEItemDefinition p2PTunnelLight()
	{
		return this.p2PTunnelLight;
	}

	@Override
	public AEItemDefinition cableAnchor()
	{
		return this.cableAnchor;
	}

	@Override
	public AEItemDefinition monitor()
	{
		return this.monitor;
	}

	@Override
	public AEItemDefinition semiDarkMonitor()
	{
		return this.semiDarkMonitor;
	}

	@Override
	public AEItemDefinition darkMonitor()
	{
		return this.darkMonitor;
	}

	@Override
	public AEItemDefinition interfaceTerminal()
	{
		return this.interfaceTerminal;
	}

	@Override
	public AEItemDefinition patternTerminal()
	{
		return this.patternTerminal;
	}

	@Override
	public AEItemDefinition craftingTerminal()
	{
		return this.craftingTerminal;
	}

	@Override
	public AEItemDefinition terminal()
	{
		return this.terminal;
	}

	@Override
	public AEItemDefinition storageMonitor()
	{
		return this.storageMonitor;
	}

	@Override
	public AEItemDefinition conversionMonitor()
	{
		return this.conversionMonitor;
	}
}
