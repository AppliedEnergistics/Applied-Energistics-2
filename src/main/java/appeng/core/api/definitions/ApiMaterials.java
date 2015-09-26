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
import appeng.api.definitions.IMaterials;
import appeng.core.features.DamagedItemDefinition;
import appeng.items.materials.ItemMultiMaterial;
import appeng.items.materials.MaterialType;


/**
 * Internal implementation for the API materials
 */
public final class ApiMaterials implements IMaterials
{
	private final IItemDefinition cell2SpatialPart;
	private final IItemDefinition cell16SpatialPart;
	private final IItemDefinition cell128SpatialPart;

	private final IItemDefinition silicon;
	private final IItemDefinition skyDust;

	private final IItemDefinition calcProcessorPress;
	private final IItemDefinition engProcessorPress;
	private final IItemDefinition logicProcessorPress;

	private final IItemDefinition calcProcessorPrint;
	private final IItemDefinition engProcessorPrint;
	private final IItemDefinition logicProcessorPrint;

	private final IItemDefinition siliconPress;
	private final IItemDefinition siliconPrint;

	private final IItemDefinition namePress;

	private final IItemDefinition logicProcessor;
	private final IItemDefinition calcProcessor;
	private final IItemDefinition engProcessor;

	private final IItemDefinition basicCard;
	private final IItemDefinition advCard;

	private final IItemDefinition purifiedCertusQuartzCrystal;
	private final IItemDefinition purifiedNetherQuartzCrystal;
	private final IItemDefinition purifiedFluixCrystal;

	private final IItemDefinition cell1kPart;
	private final IItemDefinition cell4kPart;
	private final IItemDefinition cell16kPart;
	private final IItemDefinition cell64kPart;
	private final IItemDefinition emptyStorageCell;

	private final IItemDefinition cardRedstone;
	private final IItemDefinition cardSpeed;
	private final IItemDefinition cardCapacity;
	private final IItemDefinition cardFuzzy;
	private final IItemDefinition cardInverter;
	private final IItemDefinition cardCrafting;

	private final IItemDefinition enderDust;
	private final IItemDefinition flour;
	private final IItemDefinition goldDust;
	private final IItemDefinition ironDust;
	private final IItemDefinition fluixDust;
	private final IItemDefinition certusQuartzDust;
	private final IItemDefinition netherQuartzDust;

	private final IItemDefinition matterBall;
	private final IItemDefinition ironNugget;

	private final IItemDefinition certusQuartzCrystal;
	private final IItemDefinition certusQuartzCrystalCharged;
	private final IItemDefinition fluixCrystal;
	private final IItemDefinition fluixPearl;

	private final IItemDefinition woodenGear;

	private final IItemDefinition wireless;
	private final IItemDefinition wirelessBooster;

	private final IItemDefinition annihilationCore;
	private final IItemDefinition formationCore;

	private final IItemDefinition singularity;
	private final IItemDefinition qESingularity;
	private final IItemDefinition blankPattern;

	public ApiMaterials( final DefinitionConstructor constructor )
	{
		final ItemMultiMaterial itemMultiMaterial = new ItemMultiMaterial();
		constructor.registerItemDefinition( itemMultiMaterial );

		this.cell2SpatialPart = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Cell2SpatialPart ) );
		this.cell16SpatialPart = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Cell16SpatialPart ) );
		this.cell128SpatialPart = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Cell128SpatialPart ) );

		this.silicon = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Silicon ) );
		this.skyDust = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.SkyDust ) );

		this.calcProcessorPress = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CalcProcessorPress ) );
		this.engProcessorPress = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.EngProcessorPress ) );
		this.logicProcessorPress = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.LogicProcessorPress ) );

		this.calcProcessorPrint = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CalcProcessorPrint ) );
		this.engProcessorPrint = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.EngProcessorPrint ) );
		this.logicProcessorPrint = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.LogicProcessorPrint ) );

		this.siliconPress = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.SiliconPress ) );
		this.siliconPrint = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.SiliconPrint ) );

		this.namePress = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.NamePress ) );

		this.logicProcessor = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.LogicProcessor ) );
		this.calcProcessor = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CalcProcessor ) );
		this.engProcessor = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.EngProcessor ) );

		this.basicCard = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.BasicCard ) );
		this.advCard = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.AdvCard ) );

		this.purifiedCertusQuartzCrystal = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.PurifiedCertusQuartzCrystal ) );
		this.purifiedNetherQuartzCrystal = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.PurifiedNetherQuartzCrystal ) );
		this.purifiedFluixCrystal = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.PurifiedFluixCrystal ) );

		this.cell1kPart = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Cell1kPart ) );
		this.cell4kPart = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Cell4kPart ) );
		this.cell16kPart = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Cell16kPart ) );
		this.cell64kPart = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Cell64kPart ) );
		this.emptyStorageCell = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.EmptyStorageCell ) );

		this.cardRedstone = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CardRedstone ) );
		this.cardSpeed = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CardSpeed ) );
		this.cardCapacity = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CardCapacity ) );
		this.cardFuzzy = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CardFuzzy ) );
		this.cardInverter = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CardInverter ) );
		this.cardCrafting = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CardCrafting ) );

		this.enderDust = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.EnderDust ) );
		this.flour = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Flour ) );
		this.goldDust = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.GoldDust ) );
		this.ironDust = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.IronDust ) );
		this.fluixDust = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.FluixDust ) );
		this.certusQuartzDust = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CertusQuartzDust ) );
		this.netherQuartzDust = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.NetherQuartzDust ) );

		this.matterBall = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.MatterBall ) );
		this.ironNugget = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.IronNugget ) );

		this.certusQuartzCrystal = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CertusQuartzCrystal ) );
		this.certusQuartzCrystalCharged = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.CertusQuartzCrystalCharged ) );
		this.fluixCrystal = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.FluixCrystal ) );
		this.fluixPearl = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.FluixPearl ) );

		this.woodenGear = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.WoodenGear ) );

		this.wireless = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Wireless ) );
		this.wirelessBooster = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.WirelessBooster ) );

		this.annihilationCore = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.AnnihilationCore ) );
		this.formationCore = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.FormationCore ) );

		this.singularity = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.Singularity ) );
		this.qESingularity = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.QESingularity ) );
		this.blankPattern = new DamagedItemDefinition( itemMultiMaterial.createMaterial( MaterialType.BlankPattern ) );
	}

	@Override
	public IItemDefinition cell2SpatialPart()
	{
		return this.cell2SpatialPart;
	}

	@Override
	public IItemDefinition cell16SpatialPart()
	{
		return this.cell16SpatialPart;
	}

	@Override
	public IItemDefinition cell128SpatialPart()
	{
		return this.cell128SpatialPart;
	}

	@Override
	public IItemDefinition silicon()
	{
		return this.silicon;
	}

	@Override
	public IItemDefinition skyDust()
	{
		return this.skyDust;
	}

	@Override
	public IItemDefinition calcProcessorPress()
	{
		return this.calcProcessorPress;
	}

	@Override
	public IItemDefinition engProcessorPress()
	{
		return this.engProcessorPress;
	}

	@Override
	public IItemDefinition logicProcessorPress()
	{
		return this.logicProcessorPress;
	}

	@Override
	public IItemDefinition calcProcessorPrint()
	{
		return this.calcProcessorPrint;
	}

	@Override
	public IItemDefinition engProcessorPrint()
	{
		return this.engProcessorPrint;
	}

	@Override
	public IItemDefinition logicProcessorPrint()
	{
		return this.logicProcessorPrint;
	}

	@Override
	public IItemDefinition siliconPress()
	{
		return this.siliconPress;
	}

	@Override
	public IItemDefinition siliconPrint()
	{
		return this.siliconPrint;
	}

	@Override
	public IItemDefinition namePress()
	{
		return this.namePress;
	}

	@Override
	public IItemDefinition logicProcessor()
	{
		return this.logicProcessor;
	}

	@Override
	public IItemDefinition calcProcessor()
	{
		return this.calcProcessor;
	}

	@Override
	public IItemDefinition engProcessor()
	{
		return this.engProcessor;
	}

	@Override
	public IItemDefinition basicCard()
	{
		return this.basicCard;
	}

	@Override
	public IItemDefinition advCard()
	{
		return this.advCard;
	}

	@Override
	public IItemDefinition purifiedCertusQuartzCrystal()
	{
		return this.purifiedCertusQuartzCrystal;
	}

	@Override
	public IItemDefinition purifiedNetherQuartzCrystal()
	{
		return this.purifiedNetherQuartzCrystal;
	}

	@Override
	public IItemDefinition purifiedFluixCrystal()
	{
		return this.purifiedFluixCrystal;
	}

	@Override
	public IItemDefinition cell1kPart()
	{
		return this.cell1kPart;
	}

	@Override
	public IItemDefinition cell4kPart()
	{
		return this.cell4kPart;
	}

	@Override
	public IItemDefinition cell16kPart()
	{
		return this.cell16kPart;
	}

	@Override
	public IItemDefinition cell64kPart()
	{
		return this.cell64kPart;
	}

	@Override
	public IItemDefinition emptyStorageCell()
	{
		return this.emptyStorageCell;
	}

	@Override
	public IItemDefinition cardRedstone()
	{
		return this.cardRedstone;
	}

	@Override
	public IItemDefinition cardSpeed()
	{
		return this.cardSpeed;
	}

	@Override
	public IItemDefinition cardCapacity()
	{
		return this.cardCapacity;
	}

	@Override
	public IItemDefinition cardFuzzy()
	{
		return this.cardFuzzy;
	}

	@Override
	public IItemDefinition cardInverter()
	{
		return this.cardInverter;
	}

	@Override
	public IItemDefinition cardCrafting()
	{
		return this.cardCrafting;
	}

	@Override
	public IItemDefinition enderDust()
	{
		return this.enderDust;
	}

	@Override
	public IItemDefinition flour()
	{
		return this.flour;
	}

	@Override
	public IItemDefinition goldDust()
	{
		return this.goldDust;
	}

	@Override
	public IItemDefinition ironDust()
	{
		return this.ironDust;
	}

	@Override
	public IItemDefinition fluixDust()
	{
		return this.fluixDust;
	}

	@Override
	public IItemDefinition certusQuartzDust()
	{
		return this.certusQuartzDust;
	}

	@Override
	public IItemDefinition netherQuartzDust()
	{
		return this.netherQuartzDust;
	}

	@Override
	public IItemDefinition matterBall()
	{
		return this.matterBall;
	}

	@Override
	public IItemDefinition ironNugget()
	{
		return this.ironNugget;
	}

	@Override
	public IItemDefinition certusQuartzCrystal()
	{
		return this.certusQuartzCrystal;
	}

	@Override
	public IItemDefinition certusQuartzCrystalCharged()
	{
		return this.certusQuartzCrystalCharged;
	}

	@Override
	public IItemDefinition fluixCrystal()
	{
		return this.fluixCrystal;
	}

	@Override
	public IItemDefinition fluixPearl()
	{
		return this.fluixPearl;
	}

	@Override
	public IItemDefinition woodenGear()
	{
		return this.woodenGear;
	}

	@Override
	public IItemDefinition wireless()
	{
		return this.wireless;
	}

	@Override
	public IItemDefinition wirelessBooster()
	{
		return this.wirelessBooster;
	}

	@Override
	public IItemDefinition annihilationCore()
	{
		return this.annihilationCore;
	}

	@Override
	public IItemDefinition formationCore()
	{
		return this.formationCore;
	}

	@Override
	public IItemDefinition singularity()
	{
		return this.singularity;
	}

	@Override
	public IItemDefinition qESingularity()
	{
		return this.qESingularity;
	}

	@Override
	public IItemDefinition blankPattern()
	{
		return this.blankPattern;
	}
}
