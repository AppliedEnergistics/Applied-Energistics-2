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
import appeng.api.features.AEFeature;
import appeng.bootstrap.FeatureFactory;
import appeng.core.AEConfig;
import appeng.core.features.MaterialStackSrc;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntitySingularity;
import appeng.items.materials.ItemMaterial;
import appeng.items.materials.MaterialType;
import com.google.common.base.Preconditions;
import net.minecraft.entity.EntityClassification;


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

	private final IItemDefinition certusQuartzCrystal;
	private final IItemDefinition certusQuartzCrystalCharged;
	private final IItemDefinition fluixCrystal;
	private final IItemDefinition fluixPearl;

	private final IItemDefinition woodenGear;

	private final IItemDefinition wirelessReceiver;
	private final IItemDefinition wirelessBooster;

	private final IItemDefinition annihilationCore;
	private final IItemDefinition formationCore;

	private final IItemDefinition singularity;
	private final IItemDefinition qESingularity;
	private final IItemDefinition blankPattern;

	private final IItemDefinition fluidCell1kPart;
	private final IItemDefinition fluidCell4kPart;
	private final IItemDefinition fluidCell16kPart;
	private final IItemDefinition fluidCell64kPart;

	private final FeatureFactory registry;

	public ApiMaterials( FeatureFactory registry )
	{
		this.registry = registry;

		registry.<EntitySingularity>entity("singularity", EntitySingularity::new, EntityClassification.MISC)
				.customize(builder -> builder.setTrackingRange(16).setUpdateInterval(4).setShouldReceiveVelocityUpdates(true))
				.build();

		registry.<EntityChargedQuartz>entity("charged_quartz", EntityChargedQuartz::new, EntityClassification.MISC)
				.customize(builder -> builder.setTrackingRange(16).setUpdateInterval(4).setShouldReceiveVelocityUpdates(true))
				.build();

		this.cell2SpatialPart = createMaterial( "material_cell2_spatial_part", MaterialType.CELL2_SPATIAL_PART );
		this.cell16SpatialPart = createMaterial( "material_cell16_spatial_part", MaterialType.CELL16_SPATIAL_PART );
		this.cell128SpatialPart = createMaterial( "material_cell128_spatial_part", MaterialType.CELL128_SPATIAL_PART );
		this.silicon = createMaterial( "material_silicon", MaterialType.SILICON );
		this.skyDust = createMaterial( "material_sky_dust", MaterialType.SKY_DUST );
		this.calcProcessorPress = createMaterial( "material_calculation_processor_press", MaterialType.CALCULATION_PROCESSOR_PRESS );
		this.engProcessorPress = createMaterial( "material_engineering_processor_press", MaterialType.ENGINEERING_PROCESSOR_PRESS );
		this.logicProcessorPress = createMaterial( "material_logic_processor_press", MaterialType.LOGIC_PROCESSOR_PRESS );
		this.siliconPress = createMaterial( "material_silicon_press", MaterialType.SILICON_PRESS );
		this.namePress = createMaterial( "material_name_press", MaterialType.NAME_PRESS );
		this.calcProcessorPrint = createMaterial( "material_calculation_processor_print", MaterialType.CALCULATION_PROCESSOR_PRINT );
		this.engProcessorPrint = createMaterial( "material_engineering_processor_print", MaterialType.ENGINEERING_PROCESSOR_PRINT );
		this.logicProcessorPrint = createMaterial( "material_logic_processor_print", MaterialType.LOGIC_PROCESSOR_PRINT );
		this.siliconPrint = createMaterial( "material_silicon_print", MaterialType.SILICON_PRINT );
		this.logicProcessor = createMaterial( "material_logic_processor", MaterialType.LOGIC_PROCESSOR );
		this.calcProcessor = createMaterial( "material_calculation_processor", MaterialType.CALCULATION_PROCESSOR );
		this.engProcessor = createMaterial( "material_engineering_processor", MaterialType.ENGINEERING_PROCESSOR );
		this.basicCard = createMaterial( "material_basic_card", MaterialType.BASIC_CARD );
		this.advCard = createMaterial( "material_advanced_card", MaterialType.ADVANCED_CARD );
		this.purifiedCertusQuartzCrystal = createMaterial( "material_purified_certus_quartz_crystal", MaterialType.PURIFIED_CERTUS_QUARTZ_CRYSTAL );
		this.purifiedNetherQuartzCrystal = createMaterial( "material_purified_nether_quartz_crystal", MaterialType.PURIFIED_NETHER_QUARTZ_CRYSTAL );
		this.purifiedFluixCrystal = createMaterial( "material_purified_fluix_crystal", MaterialType.PURIFIED_FLUIX_CRYSTAL );
		this.cell1kPart = createMaterial( "material_cell1k_part", MaterialType.CELL1K_PART );
		this.cell4kPart = createMaterial( "material_cell4k_part", MaterialType.CELL4K_PART );
		this.cell16kPart = createMaterial( "material_cell16k_part", MaterialType.CELL16K_PART );
		this.cell64kPart = createMaterial( "material_cell64k_part", MaterialType.CELL64K_PART );
		this.emptyStorageCell = createMaterial( "material_empty_storage_cell", MaterialType.EMPTY_STORAGE_CELL );
		this.cardRedstone = createMaterial( "material_card_redstone", MaterialType.CARD_REDSTONE );
		this.cardSpeed = createMaterial( "material_card_speed", MaterialType.CARD_SPEED );
		this.cardCapacity = createMaterial( "material_card_capacity", MaterialType.CARD_CAPACITY );
		this.cardFuzzy = createMaterial( "material_card_fuzzy", MaterialType.CARD_FUZZY );
		this.cardInverter = createMaterial( "material_card_inverter", MaterialType.CARD_INVERTER );
		this.cardCrafting = createMaterial( "material_card_crafting", MaterialType.CARD_CRAFTING );
		this.enderDust = createMaterial( "material_ender_dust", MaterialType.ENDER_DUST );
		this.flour = createMaterial( "material_flour", MaterialType.FLOUR );
		this.goldDust = createMaterial( "material_gold_dust", MaterialType.GOLD_DUST );
		this.ironDust = createMaterial( "material_iron_dust", MaterialType.IRON_DUST );
		this.fluixDust = createMaterial( "material_fluix_dust", MaterialType.FLUIX_DUST );
		this.certusQuartzDust = createMaterial( "material_certus_quartz_dust", MaterialType.CERTUS_QUARTZ_DUST );
		this.netherQuartzDust = createMaterial( "material_nether_quartz_dust", MaterialType.NETHER_QUARTZ_DUST );
		this.matterBall = createMaterial( "material_matter_ball", MaterialType.MATTER_BALL );
		this.certusQuartzCrystal = createMaterial( "material_certus_quartz_crystal", MaterialType.CERTUS_QUARTZ_CRYSTAL );
		this.certusQuartzCrystalCharged = createMaterial( "material_certus_quartz_crystal_charged", MaterialType.CERTUS_QUARTZ_CRYSTAL_CHARGED );
		this.fluixCrystal = createMaterial( "material_fluix_crystal", MaterialType.FLUIX_CRYSTAL );
		this.fluixPearl = createMaterial( "material_fluix_pearl", MaterialType.FLUIX_PEARL );
		this.woodenGear = createMaterial( "material_wooden_gear", MaterialType.WOODEN_GEAR );
		this.wirelessReceiver = createMaterial( "material_wireless_receiver", MaterialType.WIRELESS );
		this.wirelessBooster = createMaterial( "material_wireless_booster", MaterialType.WIRELESS_BOOSTER );
		this.annihilationCore = createMaterial( "material_annihilation_core", MaterialType.ANNIHILATION_CORE );
		this.formationCore = createMaterial( "material_formation_core", MaterialType.FORMATION_CORE );
		this.singularity = createMaterial( "material_singularity", MaterialType.SINGULARITY );
		this.qESingularity = createMaterial( "material_quantum_entangled_singularity", MaterialType.QUANTUM_ENTANGLED_SINGULARITY );
		this.blankPattern = createMaterial( "material_blank_pattern", MaterialType.BLANK_PATTERN );
		this.fluidCell1kPart = createMaterial( "material_fluid_cell1k_part", MaterialType.FLUID_CELL1K_PART );
		this.fluidCell4kPart = createMaterial( "material_fluid_cell4k_part", MaterialType.FLUID_CELL4K_PART );
		this.fluidCell16kPart = createMaterial( "material_fluid_cell16k_part", MaterialType.FLUID_CELL16K_PART );
		this.fluidCell64kPart = createMaterial( "material_fluid_cell64k_part", MaterialType.FLUID_CELL64K_PART );
	}

	private IItemDefinition createMaterial(String id, final MaterialType mat)
	{
		Preconditions.checkState( !mat.isRegistered(), "Cannot create the same material twice." );

		IItemDefinition def = registry.item(id, props -> new ItemMaterial(props, mat))
				.features(mat.getFeature().toArray(new AEFeature[0]))
				.build();

		boolean enabled = true;

		for( final AEFeature f : mat.getFeature() )
		{
			enabled = enabled && AEConfig.instance().isFeatureEnabled( f );
		}

		mat.setStackSrc( new MaterialStackSrc( mat, enabled ) );
		mat.setItemInstance( def.item() );
		mat.markReady();
		return def;
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
	public IItemDefinition wirelessReceiver()
	{
		return this.wirelessReceiver;
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

	@Override
	public IItemDefinition fluidCell1kPart()
	{
		return this.fluidCell1kPart;
	}

	@Override
	public IItemDefinition fluidCell4kPart()
	{
		return this.fluidCell4kPart;
	}

	@Override
	public IItemDefinition fluidCell16kPart()
	{
		return this.fluidCell16kPart;
	}

	@Override
	public IItemDefinition fluidCell64kPart()
	{
		return this.fluidCell64kPart;
	}
}
