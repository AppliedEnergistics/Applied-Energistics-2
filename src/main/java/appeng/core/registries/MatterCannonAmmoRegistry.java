/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.core.registries;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import appeng.api.features.IMatterCannonAmmoRegistry;

public class MatterCannonAmmoRegistry implements IMatterCannonAmmoRegistry {

    /**
     * Contains a mapping from
     */
    private final Map<ResourceLocation, Double> tagDamageModifiers = new HashMap<>();

    private final Map<Item, Double> itemDamageModifiers = new IdentityHashMap<>();

    public MatterCannonAmmoRegistry() {
        this.addTagWeight("forge:nuggets/meatraw", 32);
        this.addTagWeight("forge:nuggets/meatcooked", 32);
        this.addTagWeight("forge:nuggets/meat", 32);
        this.addTagWeight("forge:nuggets/chicken", 32);
        this.addTagWeight("forge:nuggets/beef", 32);
        this.addTagWeight("forge:nuggets/sheep", 32);
        this.addTagWeight("forge:nuggets/fish", 32);

        // real world...
        this.addTagWeight("forge:nuggets/lithium", 6.941);
        this.addTagWeight("forge:nuggets/beryllium", 9.0122);
        this.addTagWeight("forge:nuggets/boron", 10.811);
        this.addTagWeight("forge:nuggets/carbon", 12.0107);
        this.addTagWeight("forge:nuggets/coal", 12.0107);
        this.addTagWeight("forge:nuggets/charcoal", 12.0107);
        this.addTagWeight("forge:nuggets/sodium", 22.9897);
        this.addTagWeight("forge:nuggets/magnesium", 24.305);
        this.addTagWeight("forge:nuggets/aluminum", 26.9815);
        this.addTagWeight("forge:nuggets/silicon", 28.0855);
        this.addTagWeight("forge:nuggets/phosphorus", 30.9738);
        this.addTagWeight("forge:nuggets/sulfur", 32.065);
        this.addTagWeight("forge:nuggets/potassium", 39.0983);
        this.addTagWeight("forge:nuggets/calcium", 40.078);
        this.addTagWeight("forge:nuggets/scandium", 44.9559);
        this.addTagWeight("forge:nuggets/titanium", 47.867);
        this.addTagWeight("forge:nuggets/vanadium", 50.9415);
        this.addTagWeight("forge:nuggets/manganese", 54.938);
        this.addTagWeight("forge:nuggets/iron", 55.845);
        this.addTagWeight("forge:nuggets/gold", 196.96655);
        this.addTagWeight("forge:nuggets/nickel", 58.6934);
        this.addTagWeight("forge:nuggets/cobalt", 58.9332);
        this.addTagWeight("forge:nuggets/copper", 63.546);
        this.addTagWeight("forge:nuggets/zinc", 65.39);
        this.addTagWeight("forge:nuggets/gallium", 69.723);
        this.addTagWeight("forge:nuggets/germanium", 72.64);
        this.addTagWeight("forge:nuggets/bromine", 79.904);
        this.addTagWeight("forge:nuggets/krypton", 83.8);
        this.addTagWeight("forge:nuggets/rubidium", 85.4678);
        this.addTagWeight("forge:nuggets/strontium", 87.62);
        this.addTagWeight("forge:nuggets/yttrium", 88.9059);
        this.addTagWeight("forge:nuggets/zirconium", 91.224);
        this.addTagWeight("forge:nuggets/niobium", 92.9064);
        this.addTagWeight("forge:nuggets/technetium", 98);
        this.addTagWeight("forge:nuggets/ruthenium", 101.07);
        this.addTagWeight("forge:nuggets/rhodium", 102.9055);
        this.addTagWeight("forge:nuggets/palladium", 106.42);
        this.addTagWeight("forge:nuggets/silver", 107.8682);
        this.addTagWeight("forge:nuggets/cadmium", 112.411);
        this.addTagWeight("forge:nuggets/indium", 114.818);
        this.addTagWeight("forge:nuggets/tin", 118.71);
        this.addTagWeight("forge:nuggets/antimony", 121.76);
        this.addTagWeight("forge:nuggets/iodine", 126.9045);
        this.addTagWeight("forge:nuggets/tellurium", 127.6);
        this.addTagWeight("forge:nuggets/xenon", 131.293);
        this.addTagWeight("forge:nuggets/cesium", 132.9055);
        this.addTagWeight("forge:nuggets/barium", 137.327);
        this.addTagWeight("forge:nuggets/lanthanum", 138.9055);
        this.addTagWeight("forge:nuggets/cerium", 140.116);
        this.addTagWeight("forge:nuggets/tantalum", 180.9479);
        this.addTagWeight("forge:nuggets/tungsten", 183.84);
        this.addTagWeight("forge:nuggets/osmium", 190.23);
        this.addTagWeight("forge:nuggets/iridium", 192.217);
        this.addTagWeight("forge:nuggets/platinum", 195.078);
        this.addTagWeight("forge:nuggets/lead", 207.2);
        this.addTagWeight("forge:nuggets/bismuth", 208.9804);
        this.addTagWeight("forge:nuggets/uranium", 238.0289);
        this.addTagWeight("forge:nuggets/plutonium", 244);

        // TE stuff...
        this.addTagWeight("forge:nuggets/invar", (58.6934 + 55.845 + 55.845) / 3.0);
        this.addTagWeight("forge:nuggets/electrum", (107.8682 + 196.96655) / 2.0);
    }

    @Override
    public void registerAmmoItem(final Item ammo, final double weight) {
        this.itemDamageModifiers.put(ammo, weight);
    }

    @Override
    public void registerAmmoTag(final ResourceLocation ammoTag, final double weight) {
        this.tagDamageModifiers.put(ammoTag, weight);
    }

    @Override
    public float getPenetration(final ItemStack is) {
        // Check for an exact item match first
        Item item = is.getItem();
        Double weight = itemDamageModifiers.get(item);
        if (weight != null) {
            return weight.floatValue();
        }

        // Next, check each item tag
        for (ResourceLocation tag : item.getTags()) {
            weight = tagDamageModifiers.get(tag);
            if (weight != null) {
                return weight.floatValue();
            }
        }

        return 0;
    }

    private void addTagWeight(String name, final double weight) {
        this.registerAmmoTag(new ResourceLocation(name), weight);
    }
}
