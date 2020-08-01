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

package appeng.core.features.registries;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import appeng.api.features.IMatterCannonAmmoRegistry;

public class MatterCannonAmmoRegistry implements IMatterCannonAmmoRegistry {

    /**
     * Contains a mapping from
     */
    private final Map<Identifier, Double> tagDamageModifiers = new HashMap<>();

    private final Map<Item, Double> itemDamageModifiers = new IdentityHashMap<>();

    public MatterCannonAmmoRegistry() {
        this.addTagWeight("c:raw_meat_nuggets", 32);
        this.addTagWeight("c:cooked_meat_nuggets", 32);
        this.addTagWeight("c:meat_nuggets", 32);
        this.addTagWeight("c:chicken_nuggets", 32);
        this.addTagWeight("c:beef_nuggets", 32);
        this.addTagWeight("c:sheep_nuggets", 32);
        this.addTagWeight("c:fish_nuggets", 32);

        // real world...
        this.addTagWeight("c:lithium_nuggets", 6.941);
        this.addTagWeight("c:beryllium_nuggets", 9.0122);
        this.addTagWeight("c:boron_nuggets", 10.811);
        this.addTagWeight("c:carbon_nuggets", 12.0107);
        this.addTagWeight("c:coal_nuggets", 12.0107);
        this.addTagWeight("c:charcoal_nuggets", 12.0107);
        this.addTagWeight("c:sodium_nuggets", 22.9897);
        this.addTagWeight("c:magnesium_nuggets", 24.305);
        this.addTagWeight("c:aluminum_nuggets", 26.9815);
        this.addTagWeight("c:silicon_nuggets", 28.0855);
        this.addTagWeight("c:phosphorus_nuggets", 30.9738);
        this.addTagWeight("c:sulfur_nuggets", 32.065);
        this.addTagWeight("c:potassium_nuggets", 39.0983);
        this.addTagWeight("c:calcium_nuggets", 40.078);
        this.addTagWeight("c:scandium_nuggets", 44.9559);
        this.addTagWeight("c:titanium_nuggets", 47.867);
        this.addTagWeight("c:vanadium_nuggets", 50.9415);
        this.addTagWeight("c:manganese_nuggets", 54.938);
        this.addTagWeight("c:iron_nuggets", 55.845);
        this.addTagWeight("c:gold_nuggets", 196.96655);
        this.addTagWeight("c:nickel_nuggets", 58.6934);
        this.addTagWeight("c:cobalt_nuggets", 58.9332);
        this.addTagWeight("c:copper_nuggets", 63.546);
        this.addTagWeight("c:zinc_nuggets", 65.39);
        this.addTagWeight("c:gallium_nuggets", 69.723);
        this.addTagWeight("c:germanium_nuggets", 72.64);
        this.addTagWeight("c:bromine_nuggets", 79.904);
        this.addTagWeight("c:krypton_nuggets", 83.8);
        this.addTagWeight("c:rubidium_nuggets", 85.4678);
        this.addTagWeight("c:strontium_nuggets", 87.62);
        this.addTagWeight("c:yttrium_nuggets", 88.9059);
        this.addTagWeight("c:zirconium_nuggets", 91.224);
        this.addTagWeight("c:niobium_nuggets", 92.9064);
        this.addTagWeight("c:technetium_nuggets", 98);
        this.addTagWeight("c:ruthenium_nuggets", 101.07);
        this.addTagWeight("c:rhodium_nuggets", 102.9055);
        this.addTagWeight("c:palladium_nuggets", 106.42);
        this.addTagWeight("c:silver_nuggets", 107.8682);
        this.addTagWeight("c:cadmium_nuggets", 112.411);
        this.addTagWeight("c:indium_nuggets", 114.818);
        this.addTagWeight("c:tin_nuggets", 118.71);
        this.addTagWeight("c:antimony_nuggets", 121.76);
        this.addTagWeight("c:iodine_nuggets", 126.9045);
        this.addTagWeight("c:tellurium_nuggets", 127.6);
        this.addTagWeight("c:xenon_nuggets", 131.293);
        this.addTagWeight("c:cesium_nuggets", 132.9055);
        this.addTagWeight("c:barium_nuggets", 137.327);
        this.addTagWeight("c:lanthanum_nuggets", 138.9055);
        this.addTagWeight("c:cerium_nuggets", 140.116);
        this.addTagWeight("c:tantalum_nuggets", 180.9479);
        this.addTagWeight("c:tungsten_nuggets", 183.84);
        this.addTagWeight("c:osmium_nuggets", 190.23);
        this.addTagWeight("c:iridium_nuggets", 192.217);
        this.addTagWeight("c:platinum_nuggets", 195.078);
        this.addTagWeight("c:lead_nuggets", 207.2);
        this.addTagWeight("c:bismuth_nuggets", 208.9804);
        this.addTagWeight("c:uranium_nuggets", 238.0289);
        this.addTagWeight("c:plutonium_nuggets", 244);

        // TE stuff...
        this.addTagWeight("c:invar_nuggets", (58.6934 + 55.845 + 55.845) / 3.0);
        this.addTagWeight("c:electrum_nuggets", (107.8682 + 196.96655) / 2.0);
    }

    @Override
    public void registerAmmoItem(final Item ammo, final double weight) {
        this.itemDamageModifiers.put(ammo, weight);
    }

    @Override
    public void registerAmmoTag(final Identifier ammoTag, final double weight) {
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
        for (Map.Entry<Identifier, Double> entry : tagDamageModifiers.entrySet()) {
            Tag<Item> itemTag = ItemTags.getTagGroup().getTag(entry.getKey());
            if (itemTag != null && itemTag.contains(item)) {
                return entry.getValue().floatValue();
            }
        }

        return 0;
    }

    private void addTagWeight(String name, final double weight) {
        this.registerAmmoTag(new Identifier(name), weight);
    }
}
