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

package appeng.items.materials;


import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;
import appeng.api.features.AEFeature;
import appeng.core.features.MaterialStackSrc;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntitySingularity;


public enum MaterialType
{
	CERTUS_QUARTZ_CRYSTAL("material_certus_quartz_crystal", EnumSet.of( AEFeature.CERTUS ), "crystalCertusQuartz" ),
	CERTUS_QUARTZ_CRYSTAL_CHARGED("material_certus_quartz_crystal_charged", EnumSet.of( AEFeature.CERTUS ), EntityChargedQuartz.class ),

	CERTUS_QUARTZ_DUST("material_certus_quartz_dust", EnumSet.of( AEFeature.DUSTS, AEFeature.CERTUS ), "dustCertusQuartz" ),
	NETHER_QUARTZ_DUST("material_nether_quartz_dust", EnumSet.of( AEFeature.DUSTS ), "dustNetherQuartz,dustQuartz" ),
	FLOUR("material_flour", EnumSet.of( AEFeature.FLOUR ), "dustWheat" ),
	GOLD_DUST("material_gold_dust", EnumSet.of( AEFeature.DUSTS ), "dustGold" ),
	IRON_DUST("material_iron_dust", EnumSet.of( AEFeature.DUSTS ), "dustIron" ),

	SILICON("material_silicon", EnumSet.of( AEFeature.SILICON ), "itemSilicon" ),
	MATTER_BALL("material_matter_ball", EnumSet.of( AEFeature.MATTER_BALL ) ),

	FLUIX_CRYSTAL("material_fluix_crystal", EnumSet.of( AEFeature.FLUIX ), "crystalFluix" ),
	FLUIX_DUST("material_fluix_dust", EnumSet.of( AEFeature.FLUIX, AEFeature.DUSTS ), "dustFluix" ),
	FLUIX_PEARL("material_fluix_pearl", EnumSet.of( AEFeature.FLUIX ), "pearlFluix" ),

	PURIFIED_CERTUS_QUARTZ_CRYSTAL("material_purified_certus_quartz_crystal", EnumSet.of( AEFeature.CERTUS,
			AEFeature.PURE_CRYSTALS ), "crystalPureCertusQuartz" ),
	PURIFIED_NETHER_QUARTZ_CRYSTAL("material_purified_nether_quartz_crystal", EnumSet.of( AEFeature.PURE_CRYSTALS ), "crystalPureNetherQuartz" ),
	PURIFIED_FLUIX_CRYSTAL("material_purified_fluix_crystal", EnumSet.of( AEFeature.FLUIX, AEFeature.PURE_CRYSTALS ), "crystalPureFluix" ),

	CALCULATION_PROCESSOR_PRESS("material_calculation_processor_press", EnumSet.of( AEFeature.PRESSES ) ),
	ENGINEERING_PROCESSOR_PRESS("material_engineering_processor_press", EnumSet.of( AEFeature.PRESSES ) ),
	LOGIC_PROCESSOR_PRESS("material_logic_processor_press", EnumSet.of( AEFeature.PRESSES ) ),

	CALCULATION_PROCESSOR_PRINT("material_calculation_processor_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),
	ENGINEERING_PROCESSOR_PRINT("material_engineering_processor_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),
	LOGIC_PROCESSOR_PRINT("material_logic_processor_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),

	SILICON_PRESS("material_silicon_press", EnumSet.of( AEFeature.PRESSES ) ),
	SILICON_PRINT("material_silicon_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),

	NAME_PRESS("material_name_press", EnumSet.of( AEFeature.PRESSES ) ),

	LOGIC_PROCESSOR("material_logic_processor", EnumSet.of( AEFeature.PROCESSORS ) ),
	CALCULATION_PROCESSOR("material_calculation_processor", EnumSet.of( AEFeature.PROCESSORS ) ),
	ENGINEERING_PROCESSOR("material_engineering_processor", EnumSet.of( AEFeature.PROCESSORS ) ),

	// Basic Cards
	BASIC_CARD("material_basic_card", EnumSet.of( AEFeature.BASIC_CARDS ) ),
	CARD_REDSTONE("material_card_redstone", EnumSet.of( AEFeature.BASIC_CARDS ) ),
	CARD_CAPACITY("material_card_capacity", EnumSet.of( AEFeature.BASIC_CARDS ) ),

	// Adv Cards
	ADVANCED_CARD("material_advanced_card", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),
	CARD_FUZZY("material_card_fuzzy", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),
	CARD_SPEED("material_card_speed", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),
	CARD_INVERTER("material_card_inverter", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),

	CELL2_SPATIAL_PART("material_cell2_spatial_part", EnumSet.of( AEFeature.SPATIAL_IO ) ),
	CELL16_SPATIAL_PART("material_cell16_spatial_part", EnumSet.of( AEFeature.SPATIAL_IO ) ),
	CELL128_SPATIAL_PART("material_cell128_spatial_part", EnumSet.of( AEFeature.SPATIAL_IO ) ),

	CELL1K_PART("material_cell1k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	CELL4K_PART("material_cell4k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	CELL16K_PART("material_cell16k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	CELL64K_PART("material_cell64k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	EMPTY_STORAGE_CELL("material_empty_storage_cell", EnumSet.of( AEFeature.STORAGE_CELLS ) ),

	WOODEN_GEAR("material_wooden_gear", EnumSet.of( AEFeature.GRIND_STONE ), "gearWood" ),

	WIRELESS("material_wireless", EnumSet.of( AEFeature.WIRELESS_ACCESS_TERMINAL ) ),
	WIRELESS_BOOSTER("material_wireless_booster", EnumSet.of( AEFeature.WIRELESS_ACCESS_TERMINAL ) ),

	FORMATION_CORE("material_formation_core", EnumSet.of( AEFeature.CORES ) ),
	ANNIHILATION_CORE("material_annihilation_core", EnumSet.of( AEFeature.CORES ) ),

	SKY_DUST("material_sky_dust", EnumSet.of( AEFeature.DUSTS ) ),

	ENDER_DUST("material_ender_dust", EnumSet.of( AEFeature.QUANTUM_NETWORK_BRIDGE ), "dustEnder,dustEnderPearl", EntitySingularity.class ),
	SINGULARITY("material_singularity", EnumSet.of( AEFeature.QUANTUM_NETWORK_BRIDGE ), EntitySingularity.class ),
	QUANTUM_ENTANGLED_SINGULARITY("material_quantum_entangled_singularity", EnumSet.of( AEFeature.QUANTUM_NETWORK_BRIDGE ), EntitySingularity.class ),

	BLANK_PATTERN("material_blank_pattern", EnumSet.of( AEFeature.PATTERNS ) ),
	CARD_CRAFTING("material_card_crafting", EnumSet.of( AEFeature.ADVANCED_CARDS, AEFeature.CRAFTING_CPU ) ),

	FLUID_CELL1K_PART("material_fluid_cell1k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	FLUID_CELL4K_PART("material_fluid_cell4k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	FLUID_CELL16K_PART("material_fluid_cell16k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	FLUID_CELL64K_PART("material_fluid_cell64k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) );

	private final Set<AEFeature> features;
	private final ModelResourceLocation model;
	private Item itemInstance;
	// stack!
	private MaterialStackSrc stackSrc;
	private String oreName;
	private Class<? extends Entity> droppedEntity;
	private boolean isRegistered = false;

	MaterialType(String modelName)
	{
		this(modelName, EnumSet.of( AEFeature.CORE ) );
	}

	MaterialType(String modelName, final Set<AEFeature> features)
	{
		this.features = features;
		this.model = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, modelName ), "inventory" );
	}

	MaterialType(String modelName, final Set<AEFeature> features, final Class<? extends Entity> c)
	{
		this(modelName, features );
		this.droppedEntity = c;
	}

	MaterialType(String modelName, final Set<AEFeature> features, final String oreDictionary, final Class<? extends Entity> c)
	{
		this(modelName, features );
		this.oreName = oreDictionary;
		this.droppedEntity = c;
	}

	MaterialType(String modelName, final Set<AEFeature> features, final String oreDictionary)
	{
		this(modelName, features );
		this.oreName = oreDictionary;
	}

	public ItemStack stack( final int size )
	{
		return new ItemStack( this.getItemInstance(), size );
	}

	public Set<AEFeature> getFeature()
	{
		return this.features;
	}

	public String getOreName()
	{
		return this.oreName;
	}

	boolean hasCustomEntity()
	{
		return this.droppedEntity != null;
	}

	Class<? extends Entity> getCustomEntityClass()
	{
		return this.droppedEntity;
	}

	public boolean isRegistered()
	{
		return this.isRegistered;
	}

	public void markReady()
	{
		this.isRegistered = true;
	}

	public Item getItemInstance()
	{
		return this.itemInstance;
	}

	public void setItemInstance( final Item itemInstance )
	{
		this.itemInstance = itemInstance;
	}

	public MaterialStackSrc getStackSrc()
	{
		return this.stackSrc;
	}

	public void setStackSrc( final MaterialStackSrc stackSrc )
	{
		this.stackSrc = stackSrc;
	}

	public ModelResourceLocation getModel()
	{
		return this.model;
	}

}
