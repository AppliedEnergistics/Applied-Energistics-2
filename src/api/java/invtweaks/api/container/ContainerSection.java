/*
 * Copyright (c) 2013 Andrew Crocker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package invtweaks.api.container;

/**
 * Names for specific parts of containers. For unknown container types (such as mod containers), only INVENTORY and
 * CHEST sections are available.
 */
public enum ContainerSection {
    /**
     * The player's inventory
     */
    INVENTORY,
    /**
     * The player's inventory (only the hotbar)
     */
    INVENTORY_HOTBAR,
    /**
     * The player's inventory (all except the hotbar)
     */
    INVENTORY_NOT_HOTBAR,
    /**
     * The chest or dispenser contents. Also used for unknown container contents.
     */
    CHEST,
    /**
     * The crafting input
     */
    CRAFTING_IN,
    /**
     * The crafting input, for containters that store it internally
     */
    CRAFTING_IN_PERSISTENT,
    /**
     * The crafting output
     */
    CRAFTING_OUT,
    /**
     * The armor slots
     */
    ARMOR,
    /**
     * The furnace input
     */
    FURNACE_IN,
    /**
     * The furnace output
     */
    FURNACE_OUT,
    /**
     * The furnace fuel
     */
    FURNACE_FUEL,
    /**
     * The enchantment table slot
     */
    ENCHANTMENT,
    /**
     * The three bottles slots in brewing tables
     */
    BREWING_BOTTLES,
    /**
     * The top slot in brewing tables
     */
    BREWING_INGREDIENT
}
