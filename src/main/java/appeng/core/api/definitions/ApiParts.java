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


import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.ItemStackSrc;
import appeng.core.features.registries.PartModels;
import appeng.items.parts.ItemPart;
import appeng.items.parts.ItemPartRendering;
import appeng.items.parts.PartType;


/**
 * Internal implementation for the API parts
 */
public final class ApiParts implements IParts
{
	private final AEColoredItemDefinition cableSmart;
	private final AEColoredItemDefinition cableCovered;
	private final AEColoredItemDefinition cableGlass;
	private final AEColoredItemDefinition cableDenseCovered;
	private final AEColoredItemDefinition cableDenseSmart;
	// private final AEColoredItemDefinition lumenCableSmart;
	// private final AEColoredItemDefinition lumenCableCovered;
	// private final AEColoredItemDefinition lumenCableGlass;
	// private final AEColoredItemDefinition lumenCableDense;
	private final IItemDefinition quartzFiber;
	private final IItemDefinition toggleBus;
	private final IItemDefinition invertedToggleBus;
	private final IItemDefinition storageBus;
	private final IItemDefinition oreDictStorageBus;
	private final IItemDefinition importBus;
	private final IItemDefinition exportBus;
	private final IItemDefinition iface;
	private final IItemDefinition fluidIface;
	private final IItemDefinition levelEmitter;
	private final IItemDefinition fluidLevelEmitter;
	private final IItemDefinition annihilationPlane;
	private final IItemDefinition identityAnnihilationPlane;
	private final IItemDefinition fluidAnnihilationPlane;
	private final IItemDefinition formationPlane;
	private final IItemDefinition fluidFormationPlane;
	private final IItemDefinition p2PTunnelME;
	private final IItemDefinition p2PTunnelRedstone;
	private final IItemDefinition p2PTunnelItems;
	private final IItemDefinition p2PTunnelFluids;
	private final IItemDefinition p2PTunnelEU;
	private final IItemDefinition p2PTunnelFE;
	private final IItemDefinition p2PTunnelGTEU;
	private final IItemDefinition p2PTunnelLight;
	// private final IItemDefinition p2PTunnelOpenComputers;
	private final IItemDefinition cableAnchor;
	private final IItemDefinition monitor;
	private final IItemDefinition semiDarkMonitor;
	private final IItemDefinition darkMonitor;
	private final IItemDefinition interfaceTerminal;
	private final IItemDefinition patternTerminal;
	private final IItemDefinition expandedProcessingPatternTerminal;
	private final IItemDefinition craftingTerminal;
	private final IItemDefinition terminal;
	private final IItemDefinition storageMonitor;
	private final IItemDefinition conversionMonitor;
	private final IItemDefinition fluidImportBus;
	private final IItemDefinition fluidExportBus;
	private final IItemDefinition fluidTerminal;
	private final IItemDefinition fluidStorageBus;

	public ApiParts( FeatureFactory registry, PartModels partModels )
	{
		final ItemPart itemPart = new ItemPart();
		registry.item( "part", () -> itemPart ).rendering( new ItemPartRendering( partModels, itemPart ) ).build();

		// Register all part models
		for( PartType partType : PartType.values() )
		{
			partModels.registerModels( partType.getModels() );
		}

		this.cableSmart = constructColoredDefinition( itemPart, PartType.CABLE_SMART );
		this.cableCovered = constructColoredDefinition( itemPart, PartType.CABLE_COVERED );
		this.cableGlass = constructColoredDefinition( itemPart, PartType.CABLE_GLASS );
		this.cableDenseCovered = constructColoredDefinition( itemPart, PartType.CABLE_DENSE_COVERED );
		this.cableDenseSmart = constructColoredDefinition( itemPart, PartType.CABLE_DENSE_SMART );
		// this.lumenCableSmart = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		// this.lumenCableCovered = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		// this.lumenCableGlass = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		// this.lumenCableDense = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		this.quartzFiber = new DamagedItemDefinition( "part.quartz_fiber", itemPart.createPart( PartType.QUARTZ_FIBER ) );
		this.toggleBus = new DamagedItemDefinition( "part.toggle_bus", itemPart.createPart( PartType.TOGGLE_BUS ) );
		this.invertedToggleBus = new DamagedItemDefinition( "part.toggle_bus.inverted", itemPart.createPart( PartType.INVERTED_TOGGLE_BUS ) );
		this.storageBus = new DamagedItemDefinition( "part.bus.storage", itemPart.createPart( PartType.STORAGE_BUS ) );
		this.oreDictStorageBus = new DamagedItemDefinition( "part.bus.oredict_storage", itemPart.createPart( PartType.OREDICT_STORAGE_BUS ) );
		this.importBus = new DamagedItemDefinition( "part.bus.import", itemPart.createPart( PartType.IMPORT_BUS ) );
		this.exportBus = new DamagedItemDefinition( "part.bus.export", itemPart.createPart( PartType.EXPORT_BUS ) );
		this.iface = new DamagedItemDefinition( "part.interface", itemPart.createPart( PartType.INTERFACE ) );
		this.fluidIface = new DamagedItemDefinition( "part.fluid_interface", itemPart.createPart( PartType.FLUID_INTERFACE ) );
		this.levelEmitter = new DamagedItemDefinition( "part.level_emitter", itemPart.createPart( PartType.LEVEL_EMITTER ) );
		this.fluidLevelEmitter = new DamagedItemDefinition( "part.fluid_level_emitter", itemPart.createPart( PartType.FLUID_LEVEL_EMITTER ) );
		this.annihilationPlane = new DamagedItemDefinition( "part.plane.annihilation", itemPart.createPart( PartType.ANNIHILATION_PLANE ) );
		this.identityAnnihilationPlane = new DamagedItemDefinition( "part.plane.annihiliation.identity", itemPart.createPart( PartType.IDENTITY_ANNIHILATION_PLANE ) );
		this.fluidAnnihilationPlane = new DamagedItemDefinition( "part.plane.fluid_annihilation", itemPart.createPart( PartType.FLUID_ANNIHILATION_PLANE ) );
		this.formationPlane = new DamagedItemDefinition( "part.plane.formation", itemPart.createPart( PartType.FORMATION_PLANE ) );
		this.fluidFormationPlane = new DamagedItemDefinition( "part.plane.fluid_formation", itemPart.createPart( PartType.FLUID_FORMATION_PLANE ) );
		this.p2PTunnelME = new DamagedItemDefinition( "part.tunnel.me", itemPart.createPart( PartType.P2P_TUNNEL_ME ) );
		this.p2PTunnelRedstone = new DamagedItemDefinition( "part.tunnel.redstone", itemPart.createPart( PartType.P2P_TUNNEL_REDSTONE ) );
		this.p2PTunnelItems = new DamagedItemDefinition( "part.tunnel.item", itemPart.createPart( PartType.P2P_TUNNEL_ITEMS ) );
		this.p2PTunnelFluids = new DamagedItemDefinition( "part.tunnel.fluid", itemPart.createPart( PartType.P2P_TUNNEL_FLUIDS ) );
		this.p2PTunnelEU = new DamagedItemDefinition( "part.tunnel.eu", itemPart.createPart( PartType.P2P_TUNNEL_IC2 ) );
		this.p2PTunnelFE = new DamagedItemDefinition( "part.tunnel.fe", itemPart.createPart( PartType.P2P_TUNNEL_FE ) );
		this.p2PTunnelGTEU = new DamagedItemDefinition( "part.tunnel.gteu", itemPart.createPart( PartType.P2P_TUNNEL_GTEU ) );
		this.p2PTunnelLight = new DamagedItemDefinition( "part.tunnel.light", itemPart.createPart( PartType.P2P_TUNNEL_LIGHT ) );
		// this.p2PTunnelOpenComputers = new DamagedItemDefinition( itemMultiPart.createPart(
		// PartType.P2PTunnelOpenComputers ) );
		this.cableAnchor = new DamagedItemDefinition( "part.cable_anchor", itemPart.createPart( PartType.CABLE_ANCHOR ) );
		this.monitor = new DamagedItemDefinition( "part.monitor", itemPart.createPart( PartType.MONITOR ) );
		this.semiDarkMonitor = new DamagedItemDefinition( "part.monitor.semi_dark", itemPart.createPart( PartType.SEMI_DARK_MONITOR ) );
		this.darkMonitor = new DamagedItemDefinition( "part.monitor.dark", itemPart.createPart( PartType.DARK_MONITOR ) );
		this.interfaceTerminal = new DamagedItemDefinition( "part.terminal.interface", itemPart.createPart( PartType.INTERFACE_TERMINAL ) );
		this.patternTerminal = new DamagedItemDefinition( "part.terminal.pattern", itemPart.createPart( PartType.PATTERN_TERMINAL ) );
		this.expandedProcessingPatternTerminal = new DamagedItemDefinition( "part.terminal.expanded_processing_pattern", itemPart.createPart( PartType.EXPANDED_PROCESSING_PATTERN_TERMINAL ) );
		this.craftingTerminal = new DamagedItemDefinition( "part.terminal.crafting", itemPart.createPart( PartType.CRAFTING_TERMINAL ) );
		this.terminal = new DamagedItemDefinition( "part.terminal", itemPart.createPart( PartType.TERMINAL ) );
		this.storageMonitor = new DamagedItemDefinition( "part.monitor.storage", itemPart.createPart( PartType.STORAGE_MONITOR ) );
		this.conversionMonitor = new DamagedItemDefinition( "part.monitor.conversion", itemPart.createPart( PartType.CONVERSION_MONITOR ) );
		this.fluidImportBus = new DamagedItemDefinition( "part.bus.import.fluid", itemPart.createPart( PartType.FLUID_IMPORT_BUS ) );
		this.fluidExportBus = new DamagedItemDefinition( "part.bus.export.fluid", itemPart.createPart( PartType.FLUID_EXPORT_BUS ) );
		this.fluidTerminal = new DamagedItemDefinition( "part.terminal.fluid", itemPart.createPart( PartType.FLUID_TERMINAL ) );
		this.fluidStorageBus = new DamagedItemDefinition( "part.bus.storage.fluid", itemPart.createPart( PartType.FLUID_STORAGE_BUS ) );
	}

	private static AEColoredItemDefinition constructColoredDefinition( final ItemPart target, final PartType type )
	{
		final ColoredItemDefinition definition = new ColoredItemDefinition();

		for( final AEColor color : AEColor.values() )
		{
			final ItemStackSrc multiPartSource = target.createPart( type, color );

			definition.add( color, multiPartSource );
		}

		return definition;
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
	public AEColoredItemDefinition cableDenseCovered()
	{
		return this.cableDenseCovered;
	}

	@Override
	public AEColoredItemDefinition cableDenseSmart()
	{
		return this.cableDenseSmart;
	}

	@Override
	public AEColoredItemDefinition lumenCableSmart()
	{
		throw new MissingDefinitionException( "Lumen Smart Cable has yet to be implemented." );
		// return this.lumenCableSmart;
	}

	@Override
	public AEColoredItemDefinition lumenCableCovered()
	{
		throw new MissingDefinitionException( "Lumen Covered Cable has yet to be implemented." );
		// return this.lumenCableCovered;
	}

	@Override
	public AEColoredItemDefinition lumenCableGlass()
	{
		throw new MissingDefinitionException( "Lumen Glass Cable has yet to be implemented." );
		// return this.lumenCableGlass;
	}

	@Override
	public AEColoredItemDefinition lumenDenseCableSmart()
	{
		throw new MissingDefinitionException( "Lumen Dense Cable has yet to be implemented." );
		// return this.lumenCableDense;
	}

	@Override
	public IItemDefinition quartzFiber()
	{
		return this.quartzFiber;
	}

	@Override
	public IItemDefinition toggleBus()
	{
		return this.toggleBus;
	}

	@Override
	public IItemDefinition invertedToggleBus()
	{
		return this.invertedToggleBus;
	}

	@Override
	public IItemDefinition storageBus()
	{
		return this.storageBus;
	}

	@Override
	public IItemDefinition oreDictStorageBus()
	{
		return this.oreDictStorageBus;
	}

	@Override
	public IItemDefinition importBus()
	{
		return this.importBus;
	}

	@Override
	public IItemDefinition exportBus()
	{
		return this.exportBus;
	}

	@Override
	public IItemDefinition iface()
	{
		return this.iface;
	}

	@Override
	public IItemDefinition fluidIface()
	{
		return this.fluidIface;
	}

	@Override
	public IItemDefinition levelEmitter()
	{
		return this.levelEmitter;
	}

	@Override
	public IItemDefinition annihilationPlane()
	{
		return this.annihilationPlane;
	}

	@Override
	public IItemDefinition identityAnnihilationPlane()
	{
		return this.identityAnnihilationPlane;
	}

	@Override
	public IItemDefinition formationPlane()
	{
		return this.formationPlane;
	}

	@Override
	public IItemDefinition p2PTunnelME()
	{
		return this.p2PTunnelME;
	}

	@Override
	public IItemDefinition p2PTunnelRedstone()
	{
		return this.p2PTunnelRedstone;
	}

	@Override
	public IItemDefinition p2PTunnelItems()
	{
		return this.p2PTunnelItems;
	}

	@Override
	public IItemDefinition p2PTunnelFluids()
	{
		return this.p2PTunnelFluids;
	}

	@Override
	public IItemDefinition p2PTunnelEU()
	{
		return this.p2PTunnelEU;
	}

	@Override
	public IItemDefinition p2PTunnelFE()
	{
		return this.p2PTunnelFE;
	}

	public IItemDefinition p2PTunnelGTEU()
	{
		return this.p2PTunnelGTEU;
	}

	@Override
	public IItemDefinition p2PTunnelLight()
	{
		return this.p2PTunnelLight;
	}

	/*
	 * @Override
	 * public IItemDefinition p2PTunnelOpenComputers()
	 * {
	 * return this.p2PTunnelOpenComputers;
	 * }
	 */

	@Override
	public IItemDefinition cableAnchor()
	{
		return this.cableAnchor;
	}

	@Override
	public IItemDefinition monitor()
	{
		return this.monitor;
	}

	@Override
	public IItemDefinition semiDarkMonitor()
	{
		return this.semiDarkMonitor;
	}

	@Override
	public IItemDefinition darkMonitor()
	{
		return this.darkMonitor;
	}

	@Override
	public IItemDefinition interfaceTerminal()
	{
		return this.interfaceTerminal;
	}

	@Override
	public IItemDefinition patternTerminal()
	{
		return this.patternTerminal;
	}

	@Override
	public IItemDefinition expandedProcessingPatternTerminal()
	{
		return this.expandedProcessingPatternTerminal;
	}

	@Override
	public IItemDefinition craftingTerminal()
	{
		return this.craftingTerminal;
	}

	@Override
	public IItemDefinition terminal()
	{
		return this.terminal;
	}

	@Override
	public IItemDefinition storageMonitor()
	{
		return this.storageMonitor;
	}

	@Override
	public IItemDefinition conversionMonitor()
	{
		return this.conversionMonitor;
	}

	@Override
	public IItemDefinition fluidTerminal()
	{
		return this.fluidTerminal;
	}

	@Override
	public IItemDefinition fluidImportBus()
	{
		return this.fluidImportBus;
	}

	@Override
	public IItemDefinition fluidExportBus()
	{
		return this.fluidExportBus;
	}

	@Override
	public IItemDefinition fluidStorageBus()
	{
		return this.fluidStorageBus;
	}

	@Override
	public IItemDefinition fluidLevelEmitter()
	{
		return this.fluidLevelEmitter;
	}

	@Override
	public IItemDefinition fluidAnnihilationPlane()
	{
		return this.fluidAnnihilationPlane;
	}

	@Override
	public IItemDefinition fluidFormationnPlane()
	{
		return this.fluidFormationPlane;
	}
}
