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


import appeng.api.definitions.IMaterials;
import appeng.api.util.AEItemDefinition;
import appeng.core.features.DamagedItemDefinition;
import appeng.items.materials.ItemMultiMaterial;
import appeng.items.materials.MaterialType;


/**
 * Internal implementation for the API materials
 */
public final class ApiMaterials implements IMaterials
{
	private final AEItemDefinition cell2SpatialPart;
	private final AEItemDefinition cell16SpatialPart;
	private final AEItemDefinition cell128SpatialPart;

	private final AEItemDefinition silicon;
	private final AEItemDefinition skyDust;

	private final AEItemDefinition calcProcessorPress;
	private final AEItemDefinition engProcessorPress;
	private final AEItemDefinition logicProcessorPress;

	private final AEItemDefinition calcProcessorPrint;
	private final AEItemDefinition engProcessorPrint;
	private final AEItemDefinition logicProcessorPrint;

	private final AEItemDefinition siliconPress;
	private final AEItemDefinition siliconPrint;

	private final AEItemDefinition namePress;

	private final AEItemDefinition logicProcessor;
	private final AEItemDefinition calcProcessor;
	private final AEItemDefinition engProcessor;

	private final AEItemDefinition basicCard;
	private final AEItemDefinition advCard;

	private final AEItemDefinition purifiedCertusQuartzCrystal;
	private final AEItemDefinition purifiedNetherQuartzCrystal;
	private final AEItemDefinition purifiedFluixCrystal;

	private final AEItemDefinition cell1kPart;
	private final AEItemDefinition cell4kPart;
	private final AEItemDefinition cell16kPart;
	private final AEItemDefinition cell64kPart;
	private final AEItemDefinition emptyStorageCell;

	private final AEItemDefinition cardRedstone;
	private final AEItemDefinition cardSpeed;
	private final AEItemDefinition cardCapacity;
	private final AEItemDefinition cardFuzzy;
	private final AEItemDefinition cardInverter;
	private final AEItemDefinition cardCrafting;

	private final AEItemDefinition enderDust;
	private final AEItemDefinition flour;
	private final AEItemDefinition goldDust;
	private final AEItemDefinition ironDust;
	private final AEItemDefinition fluixDust;
	private final AEItemDefinition certusQuartzDust;
	private final AEItemDefinition netherQuartzDust;

	private final AEItemDefinition matterBall;
	private final AEItemDefinition ironNugget;

	private final AEItemDefinition certusQuartzCrystal;
	private final AEItemDefinition certusQuartzCrystalCharged;
	private final AEItemDefinition fluixCrystal;
	private final AEItemDefinition fluixPearl;

	private final AEItemDefinition woodenGear;

	private final AEItemDefinition wireless;
	private final AEItemDefinition wirelessBooster;

	private final AEItemDefinition annihilationCore;
	private final AEItemDefinition formationCore;

	private final AEItemDefinition singularity;
	private final AEItemDefinition qESingularity;
	private final AEItemDefinition blankPattern;

	public ApiMaterials( DefinitionConstructor constructor )
	{
		final ItemMultiMaterial itemMultiMaterial = new ItemMultiMaterial();
		constructor.registerAndConstructDefinition( itemMultiMaterial );

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
	public AEItemDefinition cell2SpatialPart()
	{
		return this.cell2SpatialPart;
	}

	@Override
	public AEItemDefinition cell16SpatialPart()
	{
		return this.cell16SpatialPart;
	}

	@Override
	public AEItemDefinition cell128SpatialPart()
	{
		return this.cell128SpatialPart;
	}

	@Override
	public AEItemDefinition silicon()
	{
		return this.silicon;
	}

	@Override
	public AEItemDefinition skyDust()
	{
		return this.skyDust;
	}

	@Override
	public AEItemDefinition calcProcessorPress()
	{
		return this.calcProcessorPress;
	}

	@Override
	public AEItemDefinition engProcessorPress()
	{
		return this.engProcessorPress;
	}

	@Override
	public AEItemDefinition logicProcessorPress()
	{
		return this.logicProcessorPress;
	}

	@Override
	public AEItemDefinition calcProcessorPrint()
	{
		return this.calcProcessorPrint;
	}

	@Override
	public AEItemDefinition engProcessorPrint()
	{
		return this.engProcessorPrint;
	}

	@Override
	public AEItemDefinition logicProcessorPrint()
	{
		return this.logicProcessorPrint;
	}

	@Override
	public AEItemDefinition siliconPress()
	{
		return this.siliconPress;
	}

	@Override
	public AEItemDefinition siliconPrint()
	{
		return this.siliconPrint;
	}

	@Override
	public AEItemDefinition namePress()
	{
		return this.namePress;
	}

	@Override
	public AEItemDefinition logicProcessor()
	{
		return this.logicProcessor;
	}

	@Override
	public AEItemDefinition calcProcessor()
	{
		return this.calcProcessor;
	}

	@Override
	public AEItemDefinition engProcessor()
	{
		return this.engProcessor;
	}

	@Override
	public AEItemDefinition basicCard()
	{
		return this.basicCard;
	}

	@Override
	public AEItemDefinition advCard()
	{
		return this.advCard;
	}

	@Override
	public AEItemDefinition purifiedCertusQuartzCrystal()
	{
		return this.purifiedCertusQuartzCrystal;
	}

	@Override
	public AEItemDefinition purifiedNetherQuartzCrystal()
	{
		return this.purifiedNetherQuartzCrystal;
	}

	@Override
	public AEItemDefinition purifiedFluixCrystal()
	{
		return this.purifiedFluixCrystal;
	}

	@Override
	public AEItemDefinition cell1kPart()
	{
		return this.cell1kPart;
	}

	@Override
	public AEItemDefinition cell4kPart()
	{
		return this.cell4kPart;
	}

	@Override
	public AEItemDefinition cell16kPart()
	{
		return this.cell16kPart;
	}

	@Override
	public AEItemDefinition cell64kPart()
	{
		return this.cell64kPart;
	}

	@Override
	public AEItemDefinition emptyStorageCell()
	{
		return this.emptyStorageCell;
	}

	@Override
	public AEItemDefinition cardRedstone()
	{
		return this.cardRedstone;
	}

	@Override
	public AEItemDefinition cardSpeed()
	{
		return this.cardSpeed;
	}

	@Override
	public AEItemDefinition cardCapacity()
	{
		return this.cardCapacity;
	}

	@Override
	public AEItemDefinition cardFuzzy()
	{
		return this.cardFuzzy;
	}

	@Override
	public AEItemDefinition cardInverter()
	{
		return this.cardInverter;
	}

	@Override
	public AEItemDefinition cardCrafting()
	{
		return this.cardCrafting;
	}

	@Override
	public AEItemDefinition enderDust()
	{
		return this.enderDust;
	}

	@Override
	public AEItemDefinition flour()
	{
		return this.flour;
	}

	@Override
	public AEItemDefinition goldDust()
	{
		return this.goldDust;
	}

	@Override
	public AEItemDefinition ironDust()
	{
		return this.ironDust;
	}

	@Override
	public AEItemDefinition fluixDust()
	{
		return this.fluixDust;
	}

	@Override
	public AEItemDefinition certusQuartzDust()
	{
		return this.certusQuartzDust;
	}

	@Override
	public AEItemDefinition netherQuartzDust()
	{
		return this.netherQuartzDust;
	}

	@Override
	public AEItemDefinition matterBall()
	{
		return this.matterBall;
	}

	@Override
	public AEItemDefinition ironNugget()
	{
		return this.ironNugget;
	}

	@Override
	public AEItemDefinition certusQuartzCrystal()
	{
		return this.certusQuartzCrystal;
	}

	@Override
	public AEItemDefinition certusQuartzCrystalCharged()
	{
		return this.certusQuartzCrystalCharged;
	}

	@Override
	public AEItemDefinition fluixCrystal()
	{
		return this.fluixCrystal;
	}

	@Override
	public AEItemDefinition fluixPearl()
	{
		return this.fluixPearl;
	}

	@Override
	public AEItemDefinition woodenGear()
	{
		return this.woodenGear;
	}

	@Override
	public AEItemDefinition wireless()
	{
		return this.wireless;
	}

	@Override
	public AEItemDefinition wirelessBooster()
	{
		return this.wirelessBooster;
	}

	@Override
	public AEItemDefinition annihilationCore()
	{
		return this.annihilationCore;
	}

	@Override
	public AEItemDefinition formationCore()
	{
		return this.formationCore;
	}

	@Override
	public AEItemDefinition singularity()
	{
		return this.singularity;
	}

	@Override
	public AEItemDefinition qESingularity()
	{
		return this.qESingularity;
	}

	@Override
	public AEItemDefinition blankPattern()
	{
		return this.blankPattern;
	}
}
