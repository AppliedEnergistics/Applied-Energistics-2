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
import appeng.api.exceptions.MissingDefinition;
import appeng.api.parts.IPartHelper;
import appeng.api.util.AEColoredItemDefinition;
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
	private final IItemDefinition quartzFiber;
	private final IItemDefinition toggleBus;
	private final IItemDefinition invertedToggleBus;
	private final IItemDefinition storageBus;
	private final IItemDefinition importBus;
	private final IItemDefinition exportBus;
	private final IItemDefinition iface;
	private final IItemDefinition levelEmitter;
	private final IItemDefinition annihilationPlane;
	private final IItemDefinition formationPlane;
	private final IItemDefinition p2PTunnelME;
	private final IItemDefinition p2PTunnelRedstone;
	private final IItemDefinition p2PTunnelItems;
	private final IItemDefinition p2PTunnelLiquids;
	private final IItemDefinition p2PTunnelEU;
	private final IItemDefinition p2PTunnelRF;
	private final IItemDefinition p2PTunnelLight;
	private final IItemDefinition p2PTunnelOpenComputers;
	private final IItemDefinition cableAnchor;
	private final IItemDefinition monitor;
	private final IItemDefinition semiDarkMonitor;
	private final IItemDefinition darkMonitor;
	private final IItemDefinition interfaceTerminal;
	private final IItemDefinition patternTerminal;
	private final IItemDefinition craftingTerminal;
	private final IItemDefinition terminal;
	private final IItemDefinition storageMonitor;
	private final IItemDefinition conversionMonitor;

	public ApiParts( DefinitionConstructor constructor, IPartHelper partHelper )
	{
		final ItemMultiPart itemMultiPart = new ItemMultiPart( partHelper );
		constructor.registerItemDefinition( itemMultiPart );

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
		this.p2PTunnelEU = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelEU ) );
		this.p2PTunnelRF = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelRF ) );
		this.p2PTunnelLight = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelLight ) );
		this.p2PTunnelOpenComputers = new DamagedItemDefinition( itemMultiPart.createPart( PartType.P2PTunnelOpenComputers ) );
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
	public IItemDefinition p2PTunnelLiquids()
	{
		return this.p2PTunnelLiquids;
	}

	@Override
	public IItemDefinition p2PTunnelEU()
	{
		return this.p2PTunnelEU;
	}

	@Override
	public IItemDefinition p2PTunnelRF()
	{
		return this.p2PTunnelRF;
	}

	@Override
	public IItemDefinition p2PTunnelLight()
	{
		return this.p2PTunnelLight;
	}

	@Override
	public IItemDefinition p2PTunnelOpenComputers()
	{
		return this.p2PTunnelOpenComputers;
	}

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
}
