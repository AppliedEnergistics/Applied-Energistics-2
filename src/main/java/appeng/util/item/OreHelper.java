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

package appeng.util.item;


import appeng.api.storage.data.IAEItemStack;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;


public class OreHelper {

    public static final OreHelper INSTANCE = new OreHelper();

    /**
     * A local cache to speed up OreDictionary lookups.
     */
    private final LoadingCache<String, List<ItemStack>> oreDictCache = CacheBuilder.newBuilder().build(new CacheLoader<String, List<ItemStack>>() {
        @Override
        public List<ItemStack> load(final String oreName) {
            return OreDictionary.getOres(oreName);
        }
    });

    private final Map<ItemRef, OreReference> references = new HashMap<>();

    /**
     * Test if the passed {@link ItemStack} is an ore.
     *
     * @param itemStack the itemstack to test
     * @return true if an ore entry exists, false otherwise
     */
    public Optional<OreReference> getOre(final ItemStack itemStack) {
        final ItemRef ir = new ItemRef(itemStack);

        if (!this.references.containsKey(ir)) {
            final OreReference ref = new OreReference();
            final Collection<Integer> ores = ref.getOres();
            final Collection<String> set = ref.getEquivalents();

            final Set<String> toAdd = new HashSet<>();

            for (final String ore : OreDictionary.getOreNames()) {
                // skip ore if it is a match already or null.
                if (ore == null || toAdd.contains(ore)) {
                    continue;
                }

                for (final ItemStack oreItem : this.oreDictCache.getUnchecked(ore)) {
                    if (OreDictionary.itemMatches(oreItem, itemStack, false)) {
                        toAdd.add(ore);
                        break;
                    }
                }
            }

            for (final String ore : toAdd) {
                set.add(ore);
                ores.add(OreDictionary.getOreID(ore));
            }

            if (!set.isEmpty()) {
                this.references.put(ir, ref);
            } else {
                this.references.put(ir, null);
            }
        }

        return Optional.ofNullable(this.references.get(ir));
    }

    boolean sameOre(final AEItemStack aeItemStack, final IAEItemStack is) {
        final OreReference a = aeItemStack.getOre().orElse(null);
        final OreReference b = ((AEItemStack) is).getOre().orElse(null);

        return this.sameOre(a, b);
    }

    public boolean sameOre(final OreReference a, final OreReference b) {
        if (a == null || b == null) {
            return false;
        }

        if (a == b) {
            return true;
        }

        final Collection<Integer> bOres = b.getOres();
        for (final Integer ore : a.getOres()) {
            if (bOres.contains(ore)) {
                return true;
            }
        }

        return false;
    }

    boolean sameOre(final AEItemStack aeItemStack, final ItemStack o) {
        return aeItemStack.getOre().map(a -> {
            for (final String oreName : a.getEquivalents()) {
                for (final ItemStack oreItem : this.oreDictCache.getUnchecked(oreName)) {
                    if (OreDictionary.itemMatches(oreItem, o, false)) {
                        return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

    public Set<Integer> getMatchingOre(String oreExp) {
        Set<Integer> matchingIds = new HashSet<>();

        List<OreDictFilterMatcher.MatchRule> rulesList = OreDictFilterMatcher.parseExpression(oreExp);
        for (String ore : OreDictionary.getOreNames()) {
            if (ore == null) {
                continue;
            }
            if (OreDictFilterMatcher.matches(rulesList, ore)) {
                matchingIds.add(OreDictionary.getOreID(ore));
            }
        }
        return matchingIds;
    }

    public List<ItemStack> getCachedOres(final String oreName) {
        return this.oreDictCache.getUnchecked(oreName);
    }

    private static class ItemRef {

        private final Item ref;
        private final int damage;
        private final int hash;

        ItemRef(final ItemStack stack) {
            this.ref = stack.getItem();

            if (stack.getItem().isDamageable()) {
                this.damage = 0; // IGNORED
            } else {
                this.damage = stack.getItemDamage(); // might be important...
            }

            this.hash = this.ref.hashCode() ^ this.damage;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final ItemRef other = (ItemRef) obj;
            return this.damage == other.damage && this.ref == other.ref;
        }

        @Override
        public String toString() {
            return "ItemRef [ref=" + this.ref.getUnlocalizedName() + ", damage=" + this.damage + ", hash=" + this.hash + ']';
        }
    }
}