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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;

public enum MaterialType {
    CERTUS_QUARTZ_CRYSTAL("certus_quartz_crystal"),
    CERTUS_QUARTZ_CRYSTAL_CHARGED("charged_certus_quartz_crystal"),
    CERTUS_QUARTZ_DUST("certus_quartz_dust"),
    NETHER_QUARTZ_DUST("nether_quartz_dust"),
    FLOUR("flour"),
    GOLD_DUST("gold_dust"),
    IRON_DUST("iron_dust"),
    SILICON("silicon"),
    MATTER_BALL("matter_ball"),
    FLUIX_CRYSTAL("fluix_crystal"),
    FLUIX_DUST("fluix_dust"),
    FLUIX_PEARL("fluix_pearl"),
    PURIFIED_CERTUS_QUARTZ_CRYSTAL("purified_certus_quartz_crystal"),
    PURIFIED_NETHER_QUARTZ_CRYSTAL("purified_nether_quartz_crystal"),
    PURIFIED_FLUIX_CRYSTAL("purified_fluix_crystal"),
    CALCULATION_PROCESSOR_PRESS("calculation_processor_press"),
    ENGINEERING_PROCESSOR_PRESS("engineering_processor_press"),
    LOGIC_PROCESSOR_PRESS("logic_processor_press"),
    CALCULATION_PROCESSOR_PRINT("printed_calculation_processor"),
    ENGINEERING_PROCESSOR_PRINT("printed_engineering_processor"),
    LOGIC_PROCESSOR_PRINT("printed_logic_processor"),
    SILICON_PRESS("silicon_press"),
    SILICON_PRINT("printed_silicon"),
    NAME_PRESS("name_press"),
    LOGIC_PROCESSOR("logic_processor"),
    CALCULATION_PROCESSOR("calculation_processor"),
    ENGINEERING_PROCESSOR("engineering_processor"),
    BASIC_CARD("basic_card"),
    CARD_REDSTONE("redstone_card"),
    CARD_CAPACITY("capacity_card"),
    ADVANCED_CARD("advanced_card"),
    CARD_FUZZY("fuzzy_card"),
    CARD_SPEED("speed_card"),
    CARD_INVERTER("inverter_card"),
    SPATIAL_2_CELL_COMPONENT("2_cubed_spatial_cell_component"),
    SPATIAL_16_CELL_COMPONENT("16_cubed_spatial_cell_component"),
    SPATIAL_128_CELL_COMPONENT("128_cubed_spatial_cell_component"),
    ITEM_1K_CELL_COMPONENT("1k_cell_component"),
    ITEM_4K_CELL_COMPONENT("4k_cell_component"),
    ITEM_16K_CELL_COMPONENT("16k_cell_component"),
    ITEM_64K_CELL_COMPONENT("64k_cell_component"),
    EMPTY_STORAGE_CELL("empty_storage_cell"),
    WOODEN_GEAR("wooden_gear"),
    WIRELESS_RECEIVER("wireless_receiver"),
    WIRELESS_BOOSTER("wireless_booster"),
    FORMATION_CORE("formation_core"),
    ANNIHILATION_CORE("annihilation_core"),
    SKY_DUST("sky_dust"),
    ENDER_DUST("ender_dust"),
    SINGULARITY("singularity"),
    QUANTUM_ENTANGLED_SINGULARITY("quantum_entangled_singularity"),
    BLANK_PATTERN("blank_pattern"),
    CARD_CRAFTING("crafting_card"),
    FLUID_1K_CELL_COMPONENT("1k_fluid_cell_component"),
    FLUID_4K_CELL_COMPONENT("4k_fluid_cell_component"),
    FLUID_16K_CELL_COMPONENT("16k_fluid_cell_component"),
    FLUID_64K_CELL_COMPONENT("64k_fluid_cell_component");

    private final ResourceLocation registryName;
    private Item itemInstance;
    private boolean isRegistered = false;

    MaterialType(String id) {
        this.registryName = AppEng.makeId(id);
    }

    public ItemStack stack(final int size) {
        return new ItemStack(this.getItemInstance(), size);
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
