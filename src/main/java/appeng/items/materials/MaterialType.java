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

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.features.MaterialStackSrc;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntityIds;
import appeng.entity.EntitySingularity;


public enum MaterialType
{
	InvalidType( -1, "material_invalid_type" ),

	CertusQuartzCrystal( 0, "material_certus_quartz_crystal", EnumSet.of( AEFeature.CERTUS ), "crystalCertusQuartz" ),
	CertusQuartzCrystalCharged( 1, "material_certus_quartz_crystal_charged", EnumSet.of( AEFeature.CERTUS ), EntityChargedQuartz.class ),

	CertusQuartzDust( 2, "material_certus_quartz_dust", EnumSet.of( AEFeature.DUSTS, AEFeature.CERTUS ), "dustCertusQuartz" ),
	NetherQuartzDust( 3, "material_nether_quartz_dust", EnumSet.of( AEFeature.DUSTS ), "dustNetherQuartz" ),
	Flour( 4, "material_flour", EnumSet.of( AEFeature.FLOUR ), "dustWheat" ),
	GoldDust( 51, "material_gold_dust", EnumSet.of( AEFeature.DUSTS ), "dustGold" ),
	IronDust( 49, "material_iron_dust", EnumSet.of( AEFeature.DUSTS ), "dustIron" ),
	IronNugget( 50, "material_iron_nugget", EnumSet.of( AEFeature.NUGGETS ), "nuggetIron" ),

	Silicon( 5, "material_silicon", EnumSet.of( AEFeature.SILICON ), "itemSilicon" ),
	MatterBall( 6, "material_matter_ball", EnumSet.of( AEFeature.MATTER_BALL ) ),

	FluixCrystal( 7, "material_fluix_crystal", EnumSet.of( AEFeature.FLUIX ), "crystalFluix" ),
	FluixDust( 8, "material_fluix_dust", EnumSet.of( AEFeature.FLUIX, AEFeature.DUSTS ), "dustFluix" ),
	FluixPearl( 9, "material_fluix_pearl", EnumSet.of( AEFeature.FLUIX ), "pearlFluix" ),

	PurifiedCertusQuartzCrystal( 10, "material_purified_certus_quartz_crystal", EnumSet.of( AEFeature.CERTUS, AEFeature.PURE_CRYSTALS ) ),
	PurifiedNetherQuartzCrystal( 11, "material_purified_nether_quartz_crystal", EnumSet.of( AEFeature.PURE_CRYSTALS ) ),
	PurifiedFluixCrystal( 12, "material_purified_fluix_crystal", EnumSet.of( AEFeature.FLUIX, AEFeature.PURE_CRYSTALS ) ),

	CalcProcessorPress( 13, "material_calc_processor_press", EnumSet.of( AEFeature.PRESSES ) ),
	EngProcessorPress( 14, "material_eng_processor_press", EnumSet.of( AEFeature.PRESSES ) ),
	LogicProcessorPress( 15, "material_logic_processor_press", EnumSet.of( AEFeature.PRESSES ) ),

	CalcProcessorPrint( 16, "material_calc_processor_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),
	EngProcessorPrint( 17, "material_eng_processor_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),
	LogicProcessorPrint( 18, "material_logic_processor_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),

	SiliconPress( 19, "material_silicon_press", EnumSet.of( AEFeature.PRESSES ) ),
	SiliconPrint( 20, "material_silicon_print", EnumSet.of( AEFeature.PRINTED_CIRCUITS ) ),

	NamePress( 21, "material_name_press", EnumSet.of( AEFeature.PRESSES ) ),

	LogicProcessor( 22, "material_logic_processor", EnumSet.of( AEFeature.PROCESSORS ) ),
	CalcProcessor( 23, "material_calc_processor", EnumSet.of( AEFeature.PROCESSORS ) ),
	EngProcessor( 24, "material_eng_processor", EnumSet.of( AEFeature.PROCESSORS ) ),

	// Basic Cards
	BasicCard( 25, "material_basic_card", EnumSet.of( AEFeature.BASIC_CARDS ) ),
	CardRedstone( 26, "material_card_redstone", EnumSet.of( AEFeature.BASIC_CARDS ) ),
	CardCapacity( 27, "material_card_capacity", EnumSet.of( AEFeature.BASIC_CARDS ) ),

	// Adv Cards
	AdvCard( 28, "material_adv_card", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),
	CardFuzzy( 29, "material_card_fuzzy", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),
	CardSpeed( 30, "material_card_speed", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),
	CardInverter( 31, "material_card_inverter", EnumSet.of( AEFeature.ADVANCED_CARDS ) ),

	Cell2SpatialPart( 32, "material_cell2_spatial_part", EnumSet.of( AEFeature.SPATIAL_IO ) ),
	Cell16SpatialPart( 33, "material_cell16_spatial_part", EnumSet.of( AEFeature.SPATIAL_IO ) ),
	Cell128SpatialPart( 34, "material_cell128_spatial_part", EnumSet.of( AEFeature.SPATIAL_IO ) ),

	Cell1kPart( 35, "material_cell1k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	Cell4kPart( 36, "material_cell4k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	Cell16kPart( 37, "material_cell16k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	Cell64kPart( 38, "material_cell64k_part", EnumSet.of( AEFeature.STORAGE_CELLS ) ),
	EmptyStorageCell( 39, "material_empty_storage_cell", EnumSet.of( AEFeature.STORAGE_CELLS ) ),

	WoodenGear( 40, "material_wooden_gear", EnumSet.of( AEFeature.GRIND_STONE ), "gearWood" ),

	Wireless( 41, "material_wireless", EnumSet.of( AEFeature.WIRELESS_ACCESS_TERMINAL ) ),
	WirelessBooster( 42, "material_wireless_booster", EnumSet.of( AEFeature.WIRELESS_ACCESS_TERMINAL ) ),

	FormationCore( 43, "material_formation_core", EnumSet.of( AEFeature.CORES ) ),
	AnnihilationCore( 44, "material_annihilation_core", EnumSet.of( AEFeature.CORES ) ),

	SkyDust( 45, "material_sky_dust", EnumSet.of( AEFeature.DUSTS ) ),

	EnderDust( 46, "material_ender_dust", EnumSet.of( AEFeature.QUANTUM_NETWORK_BRIDGE ), "dustEnder,dustEnderPearl", EntitySingularity.class ),
	Singularity( 47, "material_singularity", EnumSet.of( AEFeature.QUANTUM_NETWORK_BRIDGE ), EntitySingularity.class ),
	QESingularity( 48, "material_qesingularity", EnumSet.of( AEFeature.QUANTUM_NETWORK_BRIDGE ), EntitySingularity.class ),

	BlankPattern( 52, "material_blank_pattern", EnumSet.of( AEFeature.PATTERNS ) ),
	CardCrafting( 53, "material_card_crafting", EnumSet.of( AEFeature.ADVANCED_CARDS, AEFeature.CRAFTING_CPU ) );

	private final Set<AEFeature> features;
	private final ModelResourceLocation model;
	private Item itemInstance;
	private int damageValue;
	// stack!
	private MaterialStackSrc stackSrc;
	private String oreName;
	private Class<? extends Entity> droppedEntity;
	private boolean isRegistered = false;

	MaterialType( final int metaValue, String modelName )
	{
		this( metaValue, modelName, EnumSet.of( AEFeature.CORE ) );
	}

	MaterialType( final int metaValue, String modelName, final Set<AEFeature> features )
	{
		this.setDamageValue( metaValue );
		this.features = features;
		this.model = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, modelName ), "inventory" );
	}

	MaterialType( final int metaValue, String modelName, final Set<AEFeature> features, final Class<? extends Entity> c )
	{
		this( metaValue, modelName, features );
		this.droppedEntity = c;

		EntityRegistry.registerModEntity( new ResourceLocation( this.droppedEntity.getName() ), this.droppedEntity, this.droppedEntity.getSimpleName(),
				EntityIds.get( this.droppedEntity ), AppEng.instance(), 16, 4,
				true );
	}

	MaterialType( final int metaValue, String modelName, final Set<AEFeature> features, final String oreDictionary, final Class<? extends Entity> c )
	{
		this( metaValue, modelName, features );
		this.oreName = oreDictionary;
		this.droppedEntity = c;
		EntityRegistry.registerModEntity( new ResourceLocation( this.droppedEntity.getName() ), this.droppedEntity, this.droppedEntity.getSimpleName(),
				EntityIds.get( this.droppedEntity ), AppEng.instance(), 16, 4,
				true );
	}

	MaterialType( final int metaValue, String modelName, final Set<AEFeature> features, final String oreDictionary )
	{
		this( metaValue, modelName, features );
		this.oreName = oreDictionary;
	}

	public ItemStack stack( final int size )
	{
		return new ItemStack( this.getItemInstance(), size, this.getDamageValue() );
	}

	Set<AEFeature> getFeature()
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

	void markReady()
	{
		this.isRegistered = true;
	}

	public int getDamageValue()
	{
		return this.damageValue;
	}

	void setDamageValue( final int damageValue )
	{
		this.damageValue = damageValue;
	}

	public Item getItemInstance()
	{
		return this.itemInstance;
	}

	void setItemInstance( final Item itemInstance )
	{
		this.itemInstance = itemInstance;
	}

	MaterialStackSrc getStackSrc()
	{
		return this.stackSrc;
	}

	void setStackSrc( final MaterialStackSrc stackSrc )
	{
		this.stackSrc = stackSrc;
	}

	public ModelResourceLocation getModel()
	{
		return model;
	}

}
