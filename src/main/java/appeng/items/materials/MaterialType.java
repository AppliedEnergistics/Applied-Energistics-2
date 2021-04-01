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

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import appeng.api.features.AEFeature;
import appeng.core.AppEng;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.SingularityEntity;

public enum MaterialType {
    CERTUS_QUARTZ_CRYSTAL("certus_quartz_crystal", EnumSet.of(AEFeature.CERTUS), "crystalCertusQuartz"),
    CERTUS_QUARTZ_CRYSTAL_CHARGED("charged_certus_quartz_crystal", EnumSet.of(AEFeature.CERTUS),
            ChargedQuartzEntity.class),

    CERTUS_QUARTZ_DUST("certus_quartz_dust", EnumSet.of(AEFeature.DUSTS, AEFeature.CERTUS), "dustCertusQuartz"),
    NETHER_QUARTZ_DUST("nether_quartz_dust", EnumSet.of(AEFeature.DUSTS), "dustNetherQuartz,dustQuartz"),
    FLOUR("flour", EnumSet.of(AEFeature.FLOUR), "dustWheat"),
    GOLD_DUST("gold_dust", EnumSet.of(AEFeature.DUSTS), "dustGold"),
    IRON_DUST("iron_dust", EnumSet.of(AEFeature.DUSTS), "dustIron"),

    SILICON("silicon", EnumSet.of(AEFeature.SILICON), "itemSilicon"),
    MATTER_BALL("matter_ball", EnumSet.of(AEFeature.MATTER_BALL)),

    FLUIX_CRYSTAL("fluix_crystal", EnumSet.of(AEFeature.FLUIX), "crystalFluix"),
    FLUIX_DUST("fluix_dust", EnumSet.of(AEFeature.FLUIX, AEFeature.DUSTS), "dustFluix"),
    FLUIX_PEARL("fluix_pearl", EnumSet.of(AEFeature.FLUIX), "pearlFluix"),

    PURIFIED_CERTUS_QUARTZ_CRYSTAL("purified_certus_quartz_crystal",
            EnumSet.of(AEFeature.CERTUS, AEFeature.PURE_CRYSTALS), "crystalPureCertusQuartz"),
    PURIFIED_NETHER_QUARTZ_CRYSTAL("purified_nether_quartz_crystal", EnumSet.of(AEFeature.PURE_CRYSTALS),
            "crystalPureNetherQuartz"),
    PURIFIED_FLUIX_CRYSTAL("purified_fluix_crystal", EnumSet.of(AEFeature.FLUIX, AEFeature.PURE_CRYSTALS),
            "crystalPureFluix"),

    CALCULATION_PROCESSOR_PRESS("calculation_processor_press", EnumSet.of(AEFeature.PRESSES)),
    ENGINEERING_PROCESSOR_PRESS("engineering_processor_press", EnumSet.of(AEFeature.PRESSES)),
    LOGIC_PROCESSOR_PRESS("logic_processor_press", EnumSet.of(AEFeature.PRESSES)),

    CALCULATION_PROCESSOR_PRINT("printed_calculation_processor", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),
    ENGINEERING_PROCESSOR_PRINT("printed_engineering_processor", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),
    LOGIC_PROCESSOR_PRINT("printed_logic_processor", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),

    SILICON_PRESS("silicon_press", EnumSet.of(AEFeature.PRESSES)),
    SILICON_PRINT("printed_silicon", EnumSet.of(AEFeature.PRINTED_CIRCUITS)),

    NAME_PRESS("name_press", EnumSet.of(AEFeature.PRESSES)),

    LOGIC_PROCESSOR("logic_processor", EnumSet.of(AEFeature.PROCESSORS)),
    CALCULATION_PROCESSOR("calculation_processor", EnumSet.of(AEFeature.PROCESSORS)),
    ENGINEERING_PROCESSOR("engineering_processor", EnumSet.of(AEFeature.PROCESSORS)),

    // Basic Cards
    BASIC_CARD("basic_card", EnumSet.of(AEFeature.BASIC_CARDS)),
    CARD_REDSTONE("redstone_card", EnumSet.of(AEFeature.BASIC_CARDS)),
    CARD_CAPACITY("capacity_card", EnumSet.of(AEFeature.BASIC_CARDS)),

    // Adv Cards
    ADVANCED_CARD("advanced_card", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_FUZZY("fuzzy_card", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_SPEED("speed_card", EnumSet.of(AEFeature.ADVANCED_CARDS)),
    CARD_INVERTER("inverter_card", EnumSet.of(AEFeature.ADVANCED_CARDS)),

    SPATIAL_2_CELL_COMPONENT("2_cubed_spatial_cell_component", EnumSet.of(AEFeature.SPATIAL_IO)),
    SPATIAL_16_CELL_COMPONENT("16_cubed_spatial_cell_component", EnumSet.of(AEFeature.SPATIAL_IO)),
    SPATIAL_128_CELL_COMPONENT("128_cubed_spatial_cell_component", EnumSet.of(AEFeature.SPATIAL_IO)),

    ITEM_1K_CELL_COMPONENT("1k_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS)),
    ITEM_4K_CELL_COMPONENT("4k_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS)),
    ITEM_16K_CELL_COMPONENT("16k_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS)),
    ITEM_64K_CELL_COMPONENT("64k_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS)),
    EMPTY_STORAGE_CELL("empty_storage_cell", EnumSet.of(AEFeature.STORAGE_CELLS)),

    WOODEN_GEAR("wooden_gear", EnumSet.of(AEFeature.GRIND_STONE), "gearWood"),

    WIRELESS_RECEIVER("wireless_receiver", EnumSet.of(AEFeature.WIRELESS_ACCESS_TERMINAL)),
    WIRELESS_BOOSTER("wireless_booster", EnumSet.of(AEFeature.WIRELESS_ACCESS_TERMINAL)),

    FORMATION_CORE("formation_core", EnumSet.of(AEFeature.CORES)),
    ANNIHILATION_CORE("annihilation_core", EnumSet.of(AEFeature.CORES)),

    SKY_DUST("sky_dust", EnumSet.of(AEFeature.DUSTS)),

    ENDER_DUST("ender_dust", EnumSet.of(AEFeature.QUANTUM_NETWORK_BRIDGE), "dustEnder,dustEnderPearl",
            SingularityEntity.class),
    SINGULARITY("singularity", EnumSet.of(AEFeature.QUANTUM_NETWORK_BRIDGE), SingularityEntity.class),
    QUANTUM_ENTANGLED_SINGULARITY("quantum_entangled_singularity", EnumSet.of(AEFeature.QUANTUM_NETWORK_BRIDGE),
            SingularityEntity.class),

    BLANK_PATTERN("blank_pattern", EnumSet.of(AEFeature.PATTERNS)),
    CARD_CRAFTING("crafting_card", EnumSet.of(AEFeature.ADVANCED_CARDS, AEFeature.CRAFTING_CPU)),

    FLUID_1K_CELL_COMPONENT("1k_fluid_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_4K_CELL_COMPONENT("4k_fluid_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_16K_CELL_COMPONENT("16k_fluid_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS)),
    FLUID_64K_CELL_COMPONENT("64k_fluid_cell_component", EnumSet.of(AEFeature.STORAGE_CELLS));

    private final Set<AEFeature> features;
    private final ResourceLocation registryName;
    private Item itemInstance;
    private String oreName;
    private Class<? extends Entity> droppedEntity;
    private boolean isRegistered = false;

    MaterialType(String id, final Set<AEFeature> features) {
        this.features = features;
        this.registryName = new ResourceLocation(AppEng.MOD_ID, id);
    }

    MaterialType(String id, final Set<AEFeature> features, final Class<? extends Entity> c) {
        this(id, features);
        this.droppedEntity = c;
    }

    MaterialType(String id, final Set<AEFeature> features, final String oreDictionary,
            final Class<? extends Entity> c) {
        this(id, features);
        this.oreName = oreDictionary;
        this.droppedEntity = c;
    }

    MaterialType(String id, final Set<AEFeature> features, final String oreDictionary) {
        this(id, features);
        this.oreName = oreDictionary;
    }

    public ItemStack stack(final int size) {
        return new ItemStack(this.getItemInstance(), size);
    }

    public Set<AEFeature> getFeature() {
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

    public void markReady() {
        this.isRegistered = true;
    }

    public Item getItemInstance() {
        return this.itemInstance;
    }

    public void setItemInstance(final Item itemInstance) {
        this.itemInstance = itemInstance;
    }

    public String getId() {
        return registryName.getPath();
    }

    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

}
