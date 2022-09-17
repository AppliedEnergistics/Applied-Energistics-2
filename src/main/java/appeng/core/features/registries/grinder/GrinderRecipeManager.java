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

package appeng.core.features.registries.grinder;


import appeng.api.features.*;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.recipes.ores.IOreListener;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.util.Platform;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;


public final class GrinderRecipeManager implements IGrinderRegistry, IOreListener {
    private final Map<CacheKey, IGrinderRecipe> recipes;
    private final Map<ItemStack, String> ores;
    private final Map<ItemStack, String> ingots;
    private final Map<String, ItemStack> dusts;
    private final Map<String, Integer> dustToOreRatio;

    public GrinderRecipeManager() {
        this.recipes = Maps.newHashMap();
        this.ores = Maps.newHashMap();
        this.ingots = Maps.newHashMap();
        this.dusts = Maps.newHashMap();
        this.dustToOreRatio = Maps.newHashMap();

        this.addDustRatio("Obsidian", 1);
        this.addDustRatio("Charcoal", 1);
        this.addDustRatio("Coal", 1);

        this.addOre("Coal", new ItemStack(Items.COAL));
        this.addOre("Charcoal", new ItemStack(Items.COAL, 1, 1));

        this.addOre("NetherQuartz", new ItemStack(Blocks.QUARTZ_ORE));
        this.addIngot("NetherQuartz", new ItemStack(Items.QUARTZ));

        this.addOre("Gold", new ItemStack(Blocks.GOLD_ORE));
        this.addIngot("Gold", new ItemStack(Items.GOLD_INGOT));

        this.addOre("Iron", new ItemStack(Blocks.IRON_ORE));
        this.addIngot("Iron", new ItemStack(Items.IRON_INGOT));

        this.addOre("Obsidian", new ItemStack(Blocks.OBSIDIAN));

        this.addIngot("Ender", new ItemStack(Items.ENDER_PEARL));
        this.addIngot("EnderPearl", new ItemStack(Items.ENDER_PEARL));

        this.addIngot("Wheat", new ItemStack(Items.WHEAT));

        OreDictionaryHandler.INSTANCE.observe(this);
    }

    @Override
    public IGrinderRecipeBuilder builder() {
        return new Builder();
    }

    @Override
    public boolean addRecipe(IGrinderRecipe recipe) {
        Preconditions.checkNotNull(recipe, "Cannot add null as recipe.");

        return this.injectRecipe(recipe);
    }

    @Override
    public Collection<IGrinderRecipe> getRecipes() {
        return Collections.unmodifiableCollection(this.recipes.values());
    }

    @Override
    public boolean removeRecipe(IGrinderRecipe recipe) {
        Preconditions.checkNotNull(recipe, "Cannot remove null as recipe.");

        final CacheKey key = new CacheKey(recipe.getInput());
        final IGrinderRecipe removedRecipe = this.recipes.remove(key);

        this.log("Removed Grinding of '%1%s'", Platform.getItemDisplayName(recipe.getInput()));

        return removedRecipe != null;
    }

    @Override
    public IGrinderRecipe getRecipeForInput(final ItemStack input) {
        this.log("Looking up recipe for '%1$s'", Platform.getItemDisplayName(input));

        if (input == null) {
            return null;
        }

        final IGrinderRecipe recipe = this.recipes.get(new CacheKey(input));

        if (recipe == null) {
            return null;
        }

        this.log("Recipe for '%1$s' found '%2$s'", input.getUnlocalizedName(), Platform.getItemDisplayName(recipe.getOutput()));
        return recipe;
    }

    @Override
    public void addDustRatio(String oredictName, int ratio) {
        Preconditions.checkNotNull(oredictName);
        Preconditions.checkArgument(ratio > 0);

        this.log("Added ratio for '%1$s' of %2$d", oredictName, ratio);

        this.dustToOreRatio.put(oredictName, ratio);
    }

    @Override
    public boolean removeDustRatio(String oredictName) {
        Preconditions.checkNotNull(oredictName);

        this.log("Removed ratio for '%1$s'", oredictName);

        return this.dustToOreRatio.remove(oredictName) != null;
    }

    @Override
    public void oreRegistered(final String name, final ItemStack item) {
        if (!AEConfig.instance().getGrinderBlackList().contains(name) && (name.startsWith("ore") || name.startsWith("crystal") || name
                .startsWith("gem") || name.startsWith("ingot") || name.startsWith("dust"))) {
            for (final String ore : AEConfig.instance().getGrinderOres()) {
                if (name.equals("ore" + ore)) {
                    this.addOre(ore, item);
                } else if (name.equals("crystal" + ore) || name.equals("ingot" + ore) || name.equals("gem" + ore)) {
                    this.addIngot(ore, item);
                } else if (name.equals("dust" + ore)) {
                    this.addDust(ore, item);
                }
            }
        }
    }

    private boolean injectRecipe(final IGrinderRecipe grinderRecipe) {
        final CacheKey cacheKey = new CacheKey(grinderRecipe.getInput());

        if (this.recipes.containsKey(cacheKey)) {
            this.log("Tried to add duplicate recipe for '%1$s'", Platform.getItemDisplayName(grinderRecipe.getInput()));
            return false;
        }

        this.recipes.put(cacheKey, grinderRecipe);

        return true;
    }

    private int getDustToOreRatio(final String name) {
        return this.dustToOreRatio.getOrDefault(name, 2);
    }

    private void addOre(final String name, final ItemStack item) {
        if (item == null) {
            return;
        }
        this.log("Adding Ore: '%1$s'", Platform.getItemDisplayName(item));

        this.ores.put(item, name);

        if (this.dusts.containsKey(name)) {
            final ItemStack is = this.dusts.get(name).copy();
            final int ratio = this.getDustToOreRatio(name);
            if (ratio > 1) {
                final ItemStack extra = is.copy();
                extra.setCount(ratio - 1);

                final IGrinderRecipeBuilder builder = this.builder();
                IGrinderRecipe grinderRecipe = builder.withInput(item)
                        .withOutput(is)
                        .withFirstOptional(extra, (float) (AEConfig.instance().getOreDoublePercentage() / 100.0))
                        .withTurns(8)
                        .build();

                this.addRecipe(grinderRecipe);
            } else {
                final IGrinderRecipeBuilder builder = this.builder();
                IGrinderRecipe grinderRecipe = builder.withInput(item)
                        .withOutput(is)
                        .withTurns(8)
                        .build();

                this.addRecipe(grinderRecipe);
            }
        }
    }

    private void addIngot(final String name, final ItemStack item) {
        if (item == null) {
            return;
        }
        this.log("Adding Ingot: '%1$s'", Platform.getItemDisplayName(item));

        this.ingots.put(item, name);

        if (this.dusts.containsKey(name)) {
            final IGrinderRecipeBuilder builder = this.builder();
            IGrinderRecipe grinderRecipe = builder.withInput(item)
                    .withOutput(this.dusts.get(name))
                    .withTurns(4)
                    .build();

            this.addRecipe(grinderRecipe);
        }
    }

    private void addDust(final String name, final ItemStack item) {
        if (item == null) {
            return;
        }

        if (this.dusts.containsKey(name)) {
            this.log("Rejecting Dust: '%1$s'", Platform.getItemDisplayName(item));
            return;
        }

        this.log("Adding Dust: '%1$s'", Platform.getItemDisplayName(item));

        this.dusts.put(name, item);

        for (final Entry<ItemStack, String> d : this.ores.entrySet()) {
            if (name.equals(d.getValue())) {
                final ItemStack is = item.copy();
                is.setCount(1);
                final int ratio = this.getDustToOreRatio(name);
                if (ratio > 1) {
                    final ItemStack extra = is.copy();
                    extra.setCount(ratio - 1);

                    final IGrinderRecipeBuilder builder = this.builder();
                    final IGrinderRecipe grinderRecipe = builder.withInput(d.getKey())
                            .withOutput(is)
                            .withFirstOptional(extra, (float) (AEConfig.instance().getOreDoublePercentage() / 100.0))
                            .withTurns(8)
                            .build();

                    this.addRecipe(grinderRecipe);
                } else {
                    final IGrinderRecipeBuilder builder = this.builder();
                    final IGrinderRecipe grinderRecipe = builder.withInput(d.getKey())
                            .withOutput(is)
                            .withTurns(8)
                            .build();

                    this.addRecipe(grinderRecipe);
                }
            }
        }

        for (final Entry<ItemStack, String> d : this.ingots.entrySet()) {
            if (name.equals(d.getValue())) {
                final IGrinderRecipeBuilder builder = this.builder();
                final IGrinderRecipe grinderRecipe = builder.withInput(d.getKey())
                        .withOutput(item)
                        .withTurns(4)
                        .build();

                this.addRecipe(grinderRecipe);
            }
        }
    }

    private void log(final String o, Object... params) {
        AELog.grinder(o, params);
    }

    private static class CacheKey {
        private final Item item;
        private final int damage;

        CacheKey(ItemStack input) {
            Preconditions.checkNotNull(input);
            Preconditions.checkNotNull(input.getItem());

            this.item = input.getItem();
            this.damage = input.getItemDamage();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.damage;
            result = prime * result + ((this.item == null) ? 0 : this.item.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }

            CacheKey other = (CacheKey) obj;

            if (this.damage != other.damage) {
                return false;
            }

            if (this.item == null) {
                return other.item == null;
            } else return this.item == other.item;
        }

    }

    /**
     * Internal {@link IInscriberRecipeBuilder} implementation.
     * Needs to be adapted to represent a correct {@link IInscriberRecipe}
     */
    private static final class Builder implements IGrinderRecipeBuilder {

        private ItemStack in;
        private ItemStack out;

        private float optionalChance;
        private ItemStack optionalOutput;

        private float optionalChance2;
        private ItemStack optionalOutput2;

        private int turns = 8;

        @Override
        public IGrinderRecipeBuilder withInput(ItemStack input) {
            Preconditions.checkNotNull(input);
            Preconditions.checkArgument(!input.isEmpty(), "Input cannot be empty.");

            this.in = this.copy(input);

            return this;
        }

        @Override
        public IGrinderRecipeBuilder withOutput(ItemStack output) {
            Preconditions.checkNotNull(output);
            Preconditions.checkArgument(!output.isEmpty(), "Output cannot be empty.");

            this.out = this.copy(output);

            return this;
        }

        @Override
        public IGrinderRecipeBuilder withFirstOptional(ItemStack optional, float chance) {
            Preconditions.checkNotNull(optional);
            Preconditions.checkArgument(!optional.isEmpty(), "Optional cannot be empty.");
            Preconditions.checkArgument(chance >= 0 && chance <= 1.0);

            this.optionalOutput = this.copy(optional);
            this.optionalChance = chance;

            return this;
        }

        @Override
        public IGrinderRecipeBuilder withSecondOptional(ItemStack optional, float chance) {
            Preconditions.checkNotNull(optional);
            Preconditions.checkArgument(!optional.isEmpty(), "Optional cannot be empty.");
            Preconditions.checkArgument(chance >= 0 && chance <= 1.0);

            this.optionalOutput2 = this.copy(optional);
            this.optionalChance2 = chance;

            return this;
        }

        @Override
        public IGrinderRecipeBuilder withTurns(int turns) {
            Preconditions.checkArgument(turns > 0);

            this.turns = turns;

            return this;
        }

        @Nonnull
        @Override
        public IGrinderRecipe build() {
            Preconditions.checkState(this.in != null, "Input itemstack must be defined.");
            Preconditions.checkState(this.out != null, "Output itemstack must be defined.");

            return new AppEngGrinderRecipe(this.in, this.out, this.optionalOutput, this.optionalOutput2, this.optionalChance, this.optionalChance2, this.turns);
        }

        private ItemStack copy(final ItemStack is) {
            if (is != null) {
                return is.copy();
            }
            return null;
        }
    }
}
