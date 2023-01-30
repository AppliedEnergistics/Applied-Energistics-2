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


import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.features.MaterialStackSrc;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntitySingularity;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.EnumSet;
import java.util.Set;


public enum MaterialType {
    INVALID_TYPE(-1, "material_invalid_type"),

    CERTUS_QUARTZ_CRYSTAL(0, "material_certus_quartz_crystal", EnumSet.of(AEFeature.CERTUS), "crystalCertusQuartz"),
    CERTUS_QUARTZ_CRYSTAL_CHARGED(1, "material_certus_quartz_crystal_charged", EnumSet.of(AEFeature.CERTUS), EntityChargedQuartz.class),

    CERTUS_QUARTZ_DUST(2, "material_certus_quartz_dust", EnumSet.of(AEFeature.DUSTS, AEFeature.CERTUS), "dustCertusQuartz"),
    NETHER_QUARTZ_DUST(3, "material_nether_quartz_dust", EnumSet.of(AEFeature.DUSTS), "dustNetherQuartz,dustQuartz"),
    FLOUR(4, "material_flour", EnumSet.of(AEFeature.FLOUR), "dustWheat"),
    GOLD_DUST(51, "material_gold_dust", EnumSet.of(AEFeature.DUSTS), "dustGold"),
    IRON_DUST(49, "material_iron_dust", EnumSet.of(AEFeature.DUSTS), "dustIron"),

    SILICON(5, "material_silicon", EnumSet.of(AEFeature.SILICON), "itemSilicon"),
    MATTER_BALL(6, "material_matter_ball", EnumSet.of(AEFeature.MATTER_BALL)),

    FLUIX_CRYSTAL(7, "material_fluix_crystal", EnumSet.of(AEFeature.FLUIX), "crystalFluix"),
    FLUIX_DUST(8, "material_fluix_dust", EnumSet.of(AEFeature.FLUIX, AEFeature.DUSTS), "dustFluix"),
    FLUIX_PEARL(9, "material_fluix_pearl", EnumSet.of(AEFeature.FLUIX), "pearlFluix"),

    PURIFIED_CERTUS_QUARTZ_CRYSTAL(10, "material_purified_certus_quartz_crystal", EnumSet.of(AEFeature.CERTUS,
            AEFeature.PURE_CRYSTALS), "crystalPureCertusQuartz"),
    PURIFIED_NETHER_QUARTZ_CRYSTAL(11, "material_purified_nether_quartz_crystal", EnumSet.of(AEFeature.PURE_CRYSTALS), "crystalPureNetherQuartz"),
    PURIFIED_FLUIX_CRYSTAL(12, "material_purified_fluix_crystal", EnumSet.of(AEFeature.FLUIX, AEFeature.PURE_CRYSTALS), "crystalPureFluix"),

    CALCULATION_PROCESSOR_PRESS(13, "material_calculation_processor_press", EnumSet.of(AEFeature.PRESSES)),
    ENGINEERING_PROCESSOR_PRESS(14, "material_engineering_processor_press", EnumSet.of(AEFeature.PRESSES)),
    LOGIC_PROCESSOR_PRESS(15, "material_logic_processor_press", EnumSet.of(AEFeature.PRESSES)),

    CALCULATION_PROCESSOR_PRINT(16, "material_calculation_processor_print", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),
    ENGINEERING_PROCESSOR_PRINT(17, "material_engineering_processor_print", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),
    LOGIC_PROCESSOR_PRINT(18, "material_logic_processor_print", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),

    SILICON_PRESS(19, "material_silicon_press", EnumSet.of(AEFeature.PRESSES)),
    SILICON_PRINT(20, "material_silicon_print", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),

    NAME_PRESS(21, "material_name_press", EnumSet.of(AEFeature.PRESSES)),

    LOGIC_PROCESSOR(22, "material_logic_processor", EnumSet.of(AEFeature.PROCESSORS)),
    CALCULATION_PROCESSOR(23, "material_calculation_processor", EnumSet.of(AEFeature.PROCESSORS)),
    ENGINEERING_PROCESSOR(24, "material_engineering_processor", EnumSet.of(AEFeature.PROCESSORS)),

    // Basic Cards
    BASIC_CARD(25, "material_basic_card", EnumSet.of(AEFeature.BASIC_CARDS)),
    CARD_REDSTONE(26, "material_card_redstone", EnumSet.of(AEFeature.BASIC_CARDS)),
    CARD_CAPACITY(27, "material_card_capacity", EnumSet.of(AEFeature.BASIC_CARDS)),

    // Adv Cards
    ADVANCED_CARD(28, "material_advanced_card", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_FUZZY(29, "material_card_fuzzy", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_SPEED(30, "material_card_speed", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_INVERTER(31, "material_card_inverter", EnumSet.of(AEFeature.ADVANCED_CARDS)),

    CELL2_SPATIAL_PART(32, "material_cell2_spatial_part", EnumSet.of(AEFeature.SPATIAL_IO)),
    CELL16_SPATIAL_PART(33, "material_cell16_spatial_part", EnumSet.of(AEFeature.SPATIAL_IO)),
    CELL128_SPATIAL_PART(34, "material_cell128_spatial_part", EnumSet.of(AEFeature.SPATIAL_IO)),

    CELL1K_PART(35, "material_cell1k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    CELL4K_PART(36, "material_cell4k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    CELL16K_PART(37, "material_cell16k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    CELL64K_PART(38, "material_cell64k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    EMPTY_STORAGE_CELL(39, "material_empty_storage_cell", EnumSet.of(AEFeature.STORAGE_CELLS)),

    WOODEN_GEAR(40, "material_wooden_gear", EnumSet.of(AEFeature.GRIND_STONE), "gearWood"),

    WIRELESS(41, "material_wireless", EnumSet.of(AEFeature.WIRELESS_ACCESS_TERMINAL)),
    WIRELESS_BOOSTER(42, "material_wireless_booster", EnumSet.of(AEFeature.WIRELESS_ACCESS_TERMINAL)),

    FORMATION_CORE(43, "material_formation_core", EnumSet.of(AEFeature.CORES)),
    ANNIHILATION_CORE(44, "material_annihilation_core", EnumSet.of(AEFeature.CORES)),

    SKY_DUST(45, "material_sky_dust", EnumSet.of(AEFeature.DUSTS)),

    ENDER_DUST(46, "material_ender_dust", EnumSet.of(AEFeature.QUANTUM_NETWORK_BRIDGE), "dustEnder,dustEnderPearl", EntitySingularity.class),
    SINGULARITY(47, "material_singularity", EnumSet.of(AEFeature.QUANTUM_NETWORK_BRIDGE), EntitySingularity.class),
    QUANTUM_ENTANGLED_SINGULARITY(48, "material_quantum_entangled_singularity", EnumSet.of(AEFeature.QUANTUM_NETWORK_BRIDGE), EntitySingularity.class),

    BLANK_PATTERN(52, "material_blank_pattern", EnumSet.of(AEFeature.PATTERNS)),
    CARD_CRAFTING(53, "material_card_crafting", EnumSet.of(AEFeature.ADVANCED_CARDS, AEFeature.CRAFTING_CPU)),

    FLUID_CELL1K_PART(54, "material_fluid_cell1k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_CELL4K_PART(55, "material_fluid_cell4k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_CELL16K_PART(56, "material_fluid_cell16k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_CELL64K_PART(57, "material_fluid_cell64k_part", EnumSet.of(AEFeature.STORAGE_CELLS)),

    CARD_PATTERN_EXPANSION(58, "material_card_pattern_expansion", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_QUANTUM_LINK(59, "material_card_quantum_link", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_MAGNET(60, "material_card_quantum_link", EnumSet.of(AEFeature.BASIC_CARDS));


    private final Set<AEFeature> features;
    private final ModelResourceLocation model;
    private Item itemInstance;
    private int damageValue;
    // stack!
    private MaterialStackSrc stackSrc;
    private String oreName;
    private Class<? extends Entity> droppedEntity;
    private boolean isRegistered = false;

    MaterialType(final int metaValue, String modelName) {
        this(metaValue, modelName, EnumSet.of(AEFeature.CORE));
    }

    MaterialType(final int metaValue, String modelName, final Set<AEFeature> features) {
        this.setDamageValue(metaValue);
        this.features = features;
        this.model = new ModelResourceLocation(new ResourceLocation(AppEng.MOD_ID, modelName), "inventory");
    }

    MaterialType(final int metaValue, String modelName, final Set<AEFeature> features, final Class<? extends Entity> c) {
        this(metaValue, modelName, features);
        this.droppedEntity = c;
    }

    MaterialType(final int metaValue, String modelName, final Set<AEFeature> features, final String oreDictionary, final Class<? extends Entity> c) {
        this(metaValue, modelName, features);
        this.oreName = oreDictionary;
        this.droppedEntity = c;
    }

    MaterialType(final int metaValue, String modelName, final Set<AEFeature> features, final String oreDictionary) {
        this(metaValue, modelName, features);
        this.oreName = oreDictionary;
    }

    public ItemStack stack(final int size) {
        return new ItemStack(this.getItemInstance(), size, this.getDamageValue());
    }

    Set<AEFeature> getFeature() {
        return this.features;
    }

    public String getOreName() {
        return this.oreName;
    }

    boolean hasCustomEntity() {
        return this.droppedEntity != null;
    }

    Class<? extends Entity> getCustomEntityClass() {
        return this.droppedEntity;
    }

    public boolean isRegistered() {
        return this.isRegistered;
    }

    void markReady() {
        this.isRegistered = true;
    }

    public int getDamageValue() {
        return this.damageValue;
    }

    void setDamageValue(final int damageValue) {
        this.damageValue = damageValue;
    }

    public Item getItemInstance() {
        return this.itemInstance;
    }

    void setItemInstance(final Item itemInstance) {
        this.itemInstance = itemInstance;
    }

    MaterialStackSrc getStackSrc() {
        return this.stackSrc;
    }

    void setStackSrc(final MaterialStackSrc stackSrc) {
        this.stackSrc = stackSrc;
    }

    public ModelResourceLocation getModel() {
        return this.model;
    }

}
