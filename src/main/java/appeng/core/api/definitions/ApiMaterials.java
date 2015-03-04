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


import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import appeng.api.definitions.IMaterials;
import appeng.api.util.AEItemDefinition;
import appeng.core.FeatureHandlerRegistry;
import appeng.core.FeatureRegistry;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.IStackSrc;
import appeng.core.features.NullItemDefinition;
import appeng.items.materials.ItemMultiMaterial;
import appeng.items.materials.MaterialType;


/**
 * Internal implementation for the API materials
 */
public final class ApiMaterials implements IMaterials
{
	private final FeatureRegistry features;
	private final FeatureHandlerRegistry handlers;

	private final Optional<AEItemDefinition> cell2SpatialPart;
	private final Optional<AEItemDefinition> cell16SpatialPart;
	private final Optional<AEItemDefinition> cell128SpatialPart;

	private final Optional<AEItemDefinition> silicon;
	private final Optional<AEItemDefinition> skyDust;

	private final Optional<AEItemDefinition> calcProcessorPress;
	private final Optional<AEItemDefinition> engProcessorPress;
	private final Optional<AEItemDefinition> logicProcessorPress;

	private final Optional<AEItemDefinition> calcProcessorPrint;
	private final Optional<AEItemDefinition> engProcessorPrint;
	private final Optional<AEItemDefinition> logicProcessorPrint;

	private final Optional<AEItemDefinition> siliconPress;
	private final Optional<AEItemDefinition> siliconPrint;

	private final Optional<AEItemDefinition> namePress;

	private final Optional<AEItemDefinition> logicProcessor;
	private final Optional<AEItemDefinition> calcProcessor;
	private final Optional<AEItemDefinition> engProcessor;

	private final Optional<AEItemDefinition> basicCard;
	private final Optional<AEItemDefinition> advCard;

	private final Optional<AEItemDefinition> purifiedCertusQuartzCrystal;
	private final Optional<AEItemDefinition> purifiedNetherQuartzCrystal;
	private final Optional<AEItemDefinition> purifiedFluixCrystal;

	private final Optional<AEItemDefinition> cell1kPart;
	private final Optional<AEItemDefinition> cell4kPart;
	private final Optional<AEItemDefinition> cell16kPart;
	private final Optional<AEItemDefinition> cell64kPart;
	private final Optional<AEItemDefinition> emptyStorageCell;

	private final Optional<AEItemDefinition> cardRedstone;
	private final Optional<AEItemDefinition> cardSpeed;
	private final Optional<AEItemDefinition> cardCapacity;
	private final Optional<AEItemDefinition> cardFuzzy;
	private final Optional<AEItemDefinition> cardInverter;
	private final Optional<AEItemDefinition> cardCrafting;

	private final Optional<AEItemDefinition> enderDust;
	private final Optional<AEItemDefinition> flour;
	private final Optional<AEItemDefinition> goldDust;
	private final Optional<AEItemDefinition> ironDust;
	private final Optional<AEItemDefinition> fluixDust;
	private final Optional<AEItemDefinition> certusQuartzDust;
	private final Optional<AEItemDefinition> netherQuartzDust;

	private final Optional<AEItemDefinition> matterBall;
	private final Optional<AEItemDefinition> ironNugget;

	private final Optional<AEItemDefinition> certusQuartzCrystal;
	private final Optional<AEItemDefinition> certusQuartzCrystalCharged;
	private final Optional<AEItemDefinition> fluixCrystal;
	private final Optional<AEItemDefinition> fluixPearl;

	private final Optional<AEItemDefinition> woodenGear;

	private final Optional<AEItemDefinition> wireless;
	private final Optional<AEItemDefinition> wirelessBooster;

	private final Optional<AEItemDefinition> annihilationCore;
	private final Optional<AEItemDefinition> formationCore;

	private final Optional<AEItemDefinition> singularity;
	private final Optional<AEItemDefinition> qESingularity;
	private final Optional<AEItemDefinition> blankPattern;

	public ApiMaterials( FeatureRegistry features, FeatureHandlerRegistry handlers )
	{
		this.features = features;
		this.handlers = handlers;

		final ItemMultiMaterial itemMultiMaterial = new ItemMultiMaterial();
		final Optional<AEItemDefinition> multiMaterial = this.getMaybeDefinition( itemMultiMaterial );

		this.cell2SpatialPart = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Cell2SpatialPart ) );
		this.cell16SpatialPart = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Cell16SpatialPart ) );
		this.cell128SpatialPart = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Cell128SpatialPart ) );

		this.silicon = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Silicon ) );
		this.skyDust = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.SkyDust ) );

		this.calcProcessorPress = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CalcProcessorPress ) );
		this.engProcessorPress = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.EngProcessorPress ) );
		this.logicProcessorPress = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.LogicProcessorPress ) );

		this.calcProcessorPrint = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CalcProcessorPrint ) );
		this.engProcessorPrint = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.EngProcessorPrint ) );
		this.logicProcessorPrint = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.LogicProcessorPrint ) );

		this.siliconPress = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.SiliconPress ) );
		this.siliconPrint = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.SiliconPrint ) );

		this.namePress = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.NamePress ) );

		this.logicProcessor = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.LogicProcessor ) );
		this.calcProcessor = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CalcProcessor ) );
		this.engProcessor = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.EngProcessor ) );

		this.basicCard = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.BasicCard ) );
		this.advCard = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.AdvCard ) );

		this.purifiedCertusQuartzCrystal = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.PurifiedCertusQuartzCrystal ) );
		this.purifiedNetherQuartzCrystal = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.PurifiedNetherQuartzCrystal ) );
		this.purifiedFluixCrystal = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.PurifiedFluixCrystal ) );

		this.cell1kPart = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Cell1kPart ) );
		this.cell4kPart = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Cell4kPart ) );
		this.cell16kPart = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Cell16kPart ) );
		this.cell64kPart = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Cell64kPart ) );
		this.emptyStorageCell = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.EmptyStorageCell ) );

		this.cardRedstone = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CardRedstone ) );
		this.cardSpeed = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CardSpeed ) );
		this.cardCapacity = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CardCapacity ) );
		this.cardFuzzy = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CardFuzzy ) );
		this.cardInverter = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CardInverter ) );
		this.cardCrafting = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CardCrafting ) );

		this.enderDust = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.EnderDust ) );
		this.flour = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Flour ) );
		this.goldDust = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.GoldDust ) );
		this.ironDust = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.IronDust ) );
		this.fluixDust = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.FluixDust ) );
		this.certusQuartzDust = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CertusQuartzDust ) );
		this.netherQuartzDust = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.NetherQuartzDust ) );

		this.matterBall = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.MatterBall ) );
		this.ironNugget = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.IronNugget ) );

		this.certusQuartzCrystal = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CertusQuartzCrystal ) );
		this.certusQuartzCrystalCharged = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.CertusQuartzCrystalCharged ) );
		this.fluixCrystal = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.FluixCrystal ) );
		this.fluixPearl = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.FluixPearl ) );

		this.woodenGear = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.WoodenGear ) );

		this.wireless = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Wireless ) );
		this.wirelessBooster = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.WirelessBooster ) );

		this.annihilationCore = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.AnnihilationCore ) );
		this.formationCore = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.FormationCore ) );

		this.singularity = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.Singularity ) );
		this.qESingularity = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.QESingularity ) );
		this.blankPattern = multiMaterial.transform( new MaterialTransformationFunction( itemMultiMaterial, MaterialType.BlankPattern ) );
	}

	private Optional<AEItemDefinition> getMaybeDefinition( IAEFeature feature )
	{
		final IFeatureHandler handler = feature.handler();

		if ( handler.isFeatureAvailable() )
		{
			this.handlers.addFeatureHandler( handler );
			this.features.addFeature( feature );

			return Optional.of( handler.getDefinition() );
		}
		else
		{
			return Optional.absent();
		}
	}

	@Override
	public Optional<AEItemDefinition> cell2SpatialPart()
	{
		return this.cell2SpatialPart;
	}

	@Override
	public Optional<AEItemDefinition> cell16SpatialPart()
	{
		return this.cell16SpatialPart;
	}

	@Override
	public Optional<AEItemDefinition> cell128SpatialPart()
	{
		return this.cell128SpatialPart;
	}

	@Override
	public Optional<AEItemDefinition> silicon()
	{
		return this.silicon;
	}

	@Override
	public Optional<AEItemDefinition> skyDust()
	{
		return this.skyDust;
	}

	@Override
	public Optional<AEItemDefinition> calcProcessorPress()
	{
		return this.calcProcessorPress;
	}

	@Override
	public Optional<AEItemDefinition> engProcessorPress()
	{
		return this.engProcessorPress;
	}

	@Override
	public Optional<AEItemDefinition> logicProcessorPress()
	{
		return this.logicProcessorPress;
	}

	@Override
	public Optional<AEItemDefinition> calcProcessorPrint()
	{
		return this.calcProcessorPrint;
	}

	@Override
	public Optional<AEItemDefinition> engProcessorPrint()
	{
		return this.engProcessorPrint;
	}

	@Override
	public Optional<AEItemDefinition> logicProcessorPrint()
	{
		return this.logicProcessorPrint;
	}

	@Override
	public Optional<AEItemDefinition> siliconPress()
	{
		return this.siliconPress;
	}

	@Override
	public Optional<AEItemDefinition> siliconPrint()
	{
		return this.siliconPrint;
	}

	@Override
	public Optional<AEItemDefinition> namePress()
	{
		return this.namePress;
	}

	@Override
	public Optional<AEItemDefinition> logicProcessor()
	{
		return this.logicProcessor;
	}

	@Override
	public Optional<AEItemDefinition> calcProcessor()
	{
		return this.calcProcessor;
	}

	@Override
	public Optional<AEItemDefinition> engProcessor()
	{
		return this.engProcessor;
	}

	@Override
	public Optional<AEItemDefinition> basicCard()
	{
		return this.basicCard;
	}

	@Override
	public Optional<AEItemDefinition> advCard()
	{
		return this.advCard;
	}

	@Override
	public Optional<AEItemDefinition> purifiedCertusQuartzCrystal()
	{
		return this.purifiedCertusQuartzCrystal;
	}

	@Override
	public Optional<AEItemDefinition> purifiedNetherQuartzCrystal()
	{
		return this.purifiedNetherQuartzCrystal;
	}

	@Override
	public Optional<AEItemDefinition> purifiedFluixCrystal()
	{
		return this.purifiedFluixCrystal;
	}

	@Override
	public Optional<AEItemDefinition> cell1kPart()
	{
		return this.cell1kPart;
	}

	@Override
	public Optional<AEItemDefinition> cell4kPart()
	{
		return this.cell4kPart;
	}

	@Override
	public Optional<AEItemDefinition> cell16kPart()
	{
		return this.cell16kPart;
	}

	@Override
	public Optional<AEItemDefinition> cell64kPart()
	{
		return this.cell64kPart;
	}

	@Override
	public Optional<AEItemDefinition> emptyStorageCell()
	{
		return this.emptyStorageCell;
	}

	@Override
	public Optional<AEItemDefinition> cardRedstone()
	{
		return this.cardRedstone;
	}

	@Override
	public Optional<AEItemDefinition> cardSpeed()
	{
		return this.cardSpeed;
	}

	@Override
	public Optional<AEItemDefinition> cardCapacity()
	{
		return this.cardCapacity;
	}

	@Override
	public Optional<AEItemDefinition> cardFuzzy()
	{
		return this.cardFuzzy;
	}

	@Override
	public Optional<AEItemDefinition> cardInverter()
	{
		return this.cardInverter;
	}

	@Override
	public Optional<AEItemDefinition> cardCrafting()
	{
		return this.cardCrafting;
	}

	@Override
	public Optional<AEItemDefinition> enderDust()
	{
		return this.enderDust;
	}

	@Override
	public Optional<AEItemDefinition> flour()
	{
		return this.flour;
	}

	@Override
	public Optional<AEItemDefinition> goldDust()
	{
		return this.goldDust;
	}

	@Override
	public Optional<AEItemDefinition> ironDust()
	{
		return this.ironDust;
	}

	@Override
	public Optional<AEItemDefinition> fluixDust()
	{
		return this.fluixDust;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzDust()
	{
		return this.certusQuartzDust;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzDust()
	{
		return this.netherQuartzDust;
	}

	@Override
	public Optional<AEItemDefinition> matterBall()
	{
		return this.matterBall;
	}

	@Override
	public Optional<AEItemDefinition> ironNugget()
	{
		return this.ironNugget;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzCrystal()
	{
		return this.certusQuartzCrystal;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzCrystalCharged()
	{
		return this.certusQuartzCrystalCharged;
	}

	@Override
	public Optional<AEItemDefinition> fluixCrystal()
	{
		return this.fluixCrystal;
	}

	@Override
	public Optional<AEItemDefinition> fluixPearl()
	{
		return this.fluixPearl;
	}

	@Override
	public Optional<AEItemDefinition> woodenGear()
	{
		return this.woodenGear;
	}

	@Override
	public Optional<AEItemDefinition> wireless()
	{
		return this.wireless;
	}

	@Override
	public Optional<AEItemDefinition> wirelessBooster()
	{
		return this.wirelessBooster;
	}

	@Override
	public Optional<AEItemDefinition> annihilationCore()
	{
		return this.annihilationCore;
	}

	@Override
	public Optional<AEItemDefinition> formationCore()
	{
		return this.formationCore;
	}

	@Override
	public Optional<AEItemDefinition> singularity()
	{
		return this.singularity;
	}

	@Override
	public Optional<AEItemDefinition> qESingularity()
	{
		return this.qESingularity;
	}

	@Override
	public Optional<AEItemDefinition> blankPattern()
	{
		return this.blankPattern;
	}

	private static final class MaterialTransformationFunction implements Function<AEItemDefinition, AEItemDefinition>
	{
		private final ItemMultiMaterial itemMultiMaterial;
		private final MaterialType type;

		public MaterialTransformationFunction( ItemMultiMaterial itemMultiMaterial, MaterialType type )
		{
			this.itemMultiMaterial = itemMultiMaterial;
			this.type = type;
		}

		@Nullable
		@Override
		public AEItemDefinition apply( AEItemDefinition input )
		{
			final IStackSrc stackSource = this.itemMultiMaterial.createMaterial( this.type );
			final Optional<IStackSrc> maybeSource = Optional.fromNullable( stackSource );

			if ( maybeSource.isPresent() )
			{
				return new DamagedItemDefinition( stackSource );
			}
			else
			{
				return new NullItemDefinition();
			}
		}
	}
}
