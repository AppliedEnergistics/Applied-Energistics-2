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

package appeng.api.implementations.blockentities;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

/**
 * Allows access to specific segments of a machines inventory.
 */
@FunctionalInterface
public interface ISegmentedInventory {
    /**
     * Identifies an inventory that contains fake items for the purpose of configuring a filter or interface
     * auto-stocking.
     */
    ResourceLocation CONFIG = new ResourceLocation("appliedenergistics2:config");

    /**
     * Identifies the sub-inventory that contains installed upgrades. See
     * {@link appeng.api.implementations.IUpgradeInventory}.
     */
    ResourceLocation UPGRADES = new ResourceLocation("appliedenergistics2:upgrades");

    /**
     * Identifies the sub-inventory used locally by the machine to store items.
     */
    ResourceLocation STORAGE = new ResourceLocation("appliedenergistics2:storage");

    /**
     * Identifies the sub-inventory used by interfaces or molecular assemblers to store crafting patterns.
     */
    ResourceLocation PATTERNS = new ResourceLocation("appliedenergistics2:patterns");

    /**
     * Identifies the sub-inventory used to store storage cells in machines such as the cell workbench, drive, ME chest.
     */
    ResourceLocation CELLS = new ResourceLocation("appliedenergistics2:cells");

    /**
     * Access an internal inventory, note, not all inventories contain real items, some may be ghost items, and treating
     * them a real inventories will result in duplication.
     *
     * @param id Identifier for the inventory segment.
     * @return Null if the machine has no sub-inventory with the given id.
     */
    @Nullable
    IItemHandler getSubInventory(ResourceLocation id);
}
