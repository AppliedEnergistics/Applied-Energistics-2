/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public enum Upgrades {
    /**
     * Gold Tier Upgrades.
     */
    CAPACITY("item.appliedenergistics2.capacity_card", 0),
    REDSTONE("item.appliedenergistics2.redstone_card", 0),
    CRAFTING("item.appliedenergistics2.crafting_card", 0),

    /**
     * Diamond Tier Upgrades.
     */
    FUZZY("item.appliedenergistics2.fuzzy_card", 1),
    SPEED("item.appliedenergistics2.speed_card", 1),
    INVERTER("item.appliedenergistics2.inverter_card", 1);

    private final int tier;
    private final Component displayName;
    private final List<Supported> supported = new ArrayList<>();
    private List<Component> supportedTooltipLines;

    Upgrades(final String translationKey, final int tier) {
        this.tier = tier;
        this.displayName = new TranslatableComponent(translationKey);
    }

    public Component getDisplayName() {
        return displayName;
    }

    /**
     * @return list of Items/Blocks that support this upgrade, and how many it supports.
     */
    public List<Supported> getSupported() {
        return this.supported;
    }

    public void registerItem(final ItemLike item, final int maxSupported) {
        this.registerItem(item, maxSupported, null);
    }

    /**
     * Registers a specific amount of this upgrade into a specific machine
     *
     * @param item         machine in which this upgrade can be installed
     * @param maxSupported amount how many upgrades can be installed
     * @param tooltipGroup If more than one item of the same group are supported, the tooltip will show this translation
     *                     key instead. If the items have different maxSupported values, the highest will be shown.
     */
    public void registerItem(final ItemLike item, final int maxSupported, @Nullable String tooltipGroup) {
        Preconditions.checkNotNull(item);
        this.supported.add(new Supported(item.asItem(), maxSupported, tooltipGroup));
        supportedTooltipLines = null; // Reset tooltip
    }

    public List<Component> getTooltipLines() {
        if (supportedTooltipLines == null) {
            supported.sort(Comparator.comparingInt(o -> o.maxCount));
            supportedTooltipLines = new ArrayList<>(supported.size());

            // Use a separate set because the final text will include numbers
            Set<Component> namesAdded = new HashSet<>();

            for (int i = 0; i < supported.size(); i++) {
                Supported supported = this.supported.get(i);
                Component name = supported.item.getDescription();

                // If the group was already added by a previous item, skip this
                if (supported.getTooltipGroup() != null && namesAdded.contains(supported.getTooltipGroup())) {
                    continue;
                }

                // If any of the following items would be of the same group, use the group name
                // instead
                if (supported.getTooltipGroup() != null) {
                    for (int j = i + 1; j < this.supported.size(); j++) {
                        Component otherGroup = this.supported.get(j).getTooltipGroup();
                        if (supported.getTooltipGroup().equals(otherGroup)) {
                            name = supported.getTooltipGroup();
                            break;
                        }
                    }
                }

                if (namesAdded.add(name)) {
                    // append the supported count only if its > 1
                    if (supported.maxCount > 1) {
                        name = name.copy().append(" (" + supported.maxCount + ")");
                    }
                    supportedTooltipLines.add(name);
                }
            }
        }

        return supportedTooltipLines;
    }

    public int getTier() {
        return this.tier;
    }

    public static class Supported {
        private final Item item;
        private final Block block;
        private final int maxCount;
        // Translation key of the tooltip group
        @Nullable
        private final String tooltipGroup;

        public Supported(Item item, int maxCount, @Nullable String tooltipGroup) {
            this.item = item;
            if (item instanceof BlockItem blockItem) {
                this.block = blockItem.getBlock();
            } else {
                this.block = null;
            }
            this.maxCount = maxCount;
            this.tooltipGroup = tooltipGroup;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public boolean isSupported(Block block) {
            return block != null && this.block == block;
        }

        public boolean isSupported(Item item) {
            return item != null && this.item == item;
        }

        public Component getTooltipGroup() {
            return this.tooltipGroup != null ? new TranslatableComponent(this.tooltipGroup) : null;
        }

    }

}
