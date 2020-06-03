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


import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.bootstrap.FeatureFactory;
import appeng.core.CreativeTab;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntitySingularity;
import appeng.items.materials.ItemCustomEntity;
import appeng.items.materials.ItemStorageComponent;
import appeng.items.materials.ItemUpgrade;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


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

	public ApiMaterials( FeatureFactory registry )
	{
		Item.Properties properties = new Item.Properties()
				.group( CreativeTab.instance );

		this.cell2SpatialPart = registry.item( "material_cell2_spatial_part", () -> new Item( properties ) )
				.addFeatures( AEFeature.SPATIAL_IO )
				.build();
		this.cell16SpatialPart = registry.item( "material_cell16_spatial_part",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.SPATIAL_IO )
				.build();
		this.cell128SpatialPart = registry.item( "material_cell128_spatial_part",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.SPATIAL_IO )
				.build();

		this.silicon = registry.item( "material_silicon", () -> new Item( properties ) )
				.addFeatures( AEFeature.SILICON )
				.build();
		this.skyDust = registry.item( "material_sky_dust", () -> new Item( properties ) )
				.addFeatures( AEFeature.DUSTS )
				.build();

		this.calcProcessorPress = registry.item( "material_calculation_processor_press",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PRESSES )
				.build();
		this.engProcessorPress = registry.item( "material_engineering_processor_press",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PRESSES )
				.build();
		this.logicProcessorPress = registry.item( "material_logic_processor_press",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PRESSES )
				.build();
		this.siliconPress = registry.item( "material_silicon_press", () -> new Item( properties ) )
				.addFeatures( AEFeature.PRESSES )
				.build();

		this.namePress = registry.item( "material_name_press", () -> new Item( properties )
		{
			@Override
			public void addInformation( @Nonnull ItemStack is, @Nullable World world, @Nonnull List<ITextComponent> ttp, @Nonnull ITooltipFlag flags )
			{
				super.addInformation( is, world, ttp, flags );

				final CompoundNBT c = is.getOrCreateTag();
				ttp.add( new StringTextComponent( c.getString( "InscribeName" ) ) );
			}
		} )
				.addFeatures( AEFeature.PRESSES )
				.build();

		this.calcProcessorPrint = registry.item( "material_calculation_processor_print",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PRINTED_CIRCUITS )
				.build();
		this.engProcessorPrint = registry.item( "material_engineering_processor_print",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PRINTED_CIRCUITS )
				.build();
		this.logicProcessorPrint = registry.item( "material_logic_processor_print",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PRINTED_CIRCUITS )
				.build();
		this.siliconPrint = registry.item( "material_silicon_print", () -> new Item( properties ) )
				.addFeatures( AEFeature.PRINTED_CIRCUITS )
				.build();

		this.logicProcessor = registry.item( "material_logic_processor", () -> new Item( properties ) )
				.addFeatures( AEFeature.PROCESSORS )
				.build();
		this.calcProcessor = registry.item( "material_calculation_processor",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PROCESSORS )
				.build();
		this.engProcessor = registry.item( "material_engineering_processor",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PROCESSORS )
				.build();

		this.basicCard = registry.item( "material_basic_card", () -> new Item( properties ) )
				.addFeatures( AEFeature.BASIC_CARDS )
				.build();
		this.advCard = registry.item( "material_advanced_card", () -> new Item( properties ) )
				.addFeatures( AEFeature.ADVANCED_CARDS )
				.build();

		this.purifiedCertusQuartzCrystal = registry.item( "material_purified_certus_quartz_crystal",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.CERTUS, AEFeature.PURE_CRYSTALS )
				.build();
		this.purifiedNetherQuartzCrystal = registry.item( "material_purified_nether_quartz_crystal",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.PURE_CRYSTALS )
				.build();
		this.purifiedFluixCrystal = registry.item( "material_purified_fluix_crystal",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.FLUIX, AEFeature.PURE_CRYSTALS )
				.build();

		this.cell1kPart = registry.item( "material_cell1k_part", () -> new ItemStorageComponent( ItemStorageComponent.ComponentType.CELL1K_PART, properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();
		this.cell4kPart = registry.item( "material_cell4k_part", () -> new ItemStorageComponent( ItemStorageComponent.ComponentType.CELL4K_PART, properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();
		this.cell16kPart = registry.item( "material_cell16k_part",
				() -> new ItemStorageComponent( ItemStorageComponent.ComponentType.CELL16K_PART, properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();
		this.cell64kPart = registry.item( "material_cell64k_part",
				() -> new ItemStorageComponent( ItemStorageComponent.ComponentType.CELL64K_PART, properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();

		this.emptyStorageCell = registry.item( "material_empty_storage_cell", () -> new Item( properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();

		this.cardRedstone = registry.item( "material_card_redstone", () -> new ItemUpgrade( Upgrades.REDSTONE, properties ) )
				.addFeatures( AEFeature.BASIC_CARDS )
				.build();
		this.cardCapacity = registry.item( "material_card_capacity", () -> new ItemUpgrade( Upgrades.CAPACITY, properties ) )
				.addFeatures( AEFeature.BASIC_CARDS )
				.build();

		this.cardFuzzy = registry.item( "material_card_fuzzy", () -> new ItemUpgrade( Upgrades.FUZZY, properties ) )
				.addFeatures( AEFeature.ADVANCED_CARDS )
				.build();
		this.cardSpeed = registry.item( "material_card_speed", () -> new ItemUpgrade( Upgrades.SPEED, properties ) )
				.addFeatures( AEFeature.ADVANCED_CARDS )
				.build();
		this.cardInverter = registry.item( "material_card_inverter", () -> new ItemUpgrade( Upgrades.INVERTER, properties ) )
				.addFeatures( AEFeature.ADVANCED_CARDS )
				.build();

		this.cardCrafting = registry.item( "material_card_crafting", () -> new ItemUpgrade( Upgrades.CRAFTING, properties ) )
				.addFeatures( AEFeature.ADVANCED_CARDS, AEFeature.CRAFTING_CPU )
				.build();

		this.enderDust = registry.item( "material_ender_dust",
				() -> new ItemCustomEntity( EntitySingularity::new, properties ) )
				.addFeatures( AEFeature.QUANTUM_NETWORK_BRIDGE )
				.build();
		this.flour = registry.item( "material_flour", () -> new Item( properties ) )
				.addFeatures( AEFeature.FLOUR )
				.build();
		this.goldDust = registry.item( "material_gold_dust", () -> new Item( properties ) )
				.addFeatures( AEFeature.DUSTS )
				.build();
		this.ironDust = registry.item( "material_iron_dust", () -> new Item( properties ) )
				.addFeatures( AEFeature.DUSTS )
				.build();
		this.fluixDust = registry.item( "material_fluix_dust", () -> new Item( properties ) )
				.addFeatures( AEFeature.FLUIX, AEFeature.DUSTS )
				.build();
		this.netherQuartzDust = registry.item( "material_nether_quartz_dust", () -> new Item( properties ) )
				.addFeatures( AEFeature.DUSTS )
				.build();
		this.certusQuartzDust = registry.item( "material_certus_quartz_dust", () -> new Item( properties ) )
				.addFeatures( AEFeature.DUSTS, AEFeature.CERTUS )
				.build();

		this.matterBall = registry.item( "material_matter_ball", () -> new Item( properties ) )
				.addFeatures( AEFeature.MATTER_BALL )
				.build();

		this.certusQuartzCrystal = registry.item( "material_certus_quartz_crystal",
				() -> new Item( properties ) )
				.addFeatures( AEFeature.CERTUS )
				.build();
		this.certusQuartzCrystalCharged = registry.item( "material_certus_quartz_crystal_charged",
				() -> new ItemCustomEntity( EntityChargedQuartz::new, properties ) )
				.addFeatures( AEFeature.CERTUS )
				.build();
		this.fluixCrystal = registry.item( "material_fluix_crystal", () -> new Item( properties ) )
				.addFeatures( AEFeature.FLUIX )
				.build();
		this.fluixPearl = registry.item( "material_fluix_pearl", () -> new Item( properties ) )
				.addFeatures( AEFeature.FLUIX )
				.build();

		this.woodenGear = registry.item( "material_wooden_gear", () -> new Item( properties ) )
				.addFeatures( AEFeature.GRIND_STONE )
				.build();

		this.wirelessReceiver = registry.item( "material_wireless", () -> new Item( properties ) )
				.addFeatures( AEFeature.WIRELESS_ACCESS_TERMINAL )
				.build();
		this.wirelessBooster = registry.item( "material_wireless_booster", () -> new Item( properties ) )
				.addFeatures( AEFeature.WIRELESS_ACCESS_TERMINAL )
				.build();

		this.annihilationCore = registry.item( "material_annihilation_core", () -> new Item( properties ) )
				.addFeatures( AEFeature.CORES )
				.build();
		this.formationCore = registry.item( "material_formation_core", () -> new Item( properties ) )
				.addFeatures( AEFeature.CORES )
				.build();

		this.singularity = registry.item( "material_singularity", () -> new ItemCustomEntity( EntitySingularity::new, properties ) )
				.addFeatures( AEFeature.QUANTUM_NETWORK_BRIDGE )
				.build();
		this.qESingularity = registry.item( "material_quantum_entangled_singularity", () -> new ItemCustomEntity( EntitySingularity::new, properties ) )
				.addFeatures( AEFeature.QUANTUM_NETWORK_BRIDGE )
				.build();

		this.blankPattern = registry.item( "material_blank_pattern", () -> new Item( properties ) )
				.addFeatures( AEFeature.PATTERNS )
				.build();

		this.fluidCell1kPart = registry.item( "material_fluid_cell1k_part", () -> new Item( properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();
		this.fluidCell4kPart = registry.item( "material_fluid_cell4k_part", () -> new Item( properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();
		this.fluidCell16kPart = registry.item( "material_fluid_cell16k_part", () -> new Item( properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();
		this.fluidCell64kPart = registry.item( "material_fluid_cell64k_part", () -> new Item( properties ) )
				.addFeatures( AEFeature.STORAGE_CELLS )
				.build();
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
