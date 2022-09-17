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

package appeng.recipes;


import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
import appeng.api.recipes.ISubItemResolver;
import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.AppEng;
import appeng.items.materials.ItemMaterial;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;
import net.minecraft.item.ItemStack;


public class AEItemResolver implements ISubItemResolver {

    @Override
    public Object resolveItemByName(final String nameSpace, final String itemName) {

        if (nameSpace.equals(AppEng.MOD_ID)) {
            final IDefinitions definitions = AEApi.instance().definitions();
            final IItems items = definitions.items();
            final IParts parts = definitions.parts();

            if (itemName.startsWith("paint_ball.")) {
                return this.paintBall(items.coloredPaintBall(), itemName.substring(itemName.indexOf('.') + 1), false);
            }

            if (itemName.startsWith("lumen_paint_ball.")) {
                return this.paintBall(items.coloredPaintBall(), itemName.substring(itemName.indexOf('.') + 1), true);
            }

            if (itemName.equals("cable_glass")) {
                return new ResolverResultSet("cable_glass", parts.cableGlass().allStacks(1));
            }

            if (itemName.startsWith("cable_glass.")) {
                return this.cableItem(parts.cableGlass(), itemName.substring(itemName.indexOf('.') + 1));
            }

            if (itemName.equals("cable_covered")) {
                return new ResolverResultSet("cable_covered", parts.cableCovered().allStacks(1));
            }

            if (itemName.startsWith("cable_covered.")) {
                return this.cableItem(parts.cableCovered(), itemName.substring(itemName.indexOf('.') + 1));
            }

            if (itemName.equals("cable_smart")) {
                return new ResolverResultSet("cable_smart", parts.cableSmart().allStacks(1));
            }

            if (itemName.startsWith("cable_smart.")) {
                return this.cableItem(parts.cableSmart(), itemName.substring(itemName.indexOf('.') + 1));
            }

            if (itemName.equals("cable_dense_covered")) {
                return new ResolverResultSet("cable_dense_covered", parts.cableDenseCovered().allStacks(1));
            }

            if (itemName.startsWith("cable_dense_covered.")) {
                return this.cableItem(parts.cableDenseCovered(), itemName.substring(itemName.indexOf('.') + 1));
            }

            if (itemName.equals("cable_dense_smart")) {
                return new ResolverResultSet("cable_dense_smart", parts.cableDenseSmart().allStacks(1));
            }

            if (itemName.startsWith("cable_dense_smart.")) {
                return this.cableItem(parts.cableDenseSmart(), itemName.substring(itemName.indexOf('.') + 1));
            }

            if (itemName.startsWith("crystal_seed.")) {
                if (itemName.equalsIgnoreCase("crystal_seed.certus")) {
                    return ItemCrystalSeed.getResolver(ItemCrystalSeed.CERTUS);
                }
                if (itemName.equalsIgnoreCase("crystal_seed.nether")) {
                    return ItemCrystalSeed.getResolver(ItemCrystalSeed.NETHER);
                }
                if (itemName.equalsIgnoreCase("crystal_seed.fluix")) {
                    return ItemCrystalSeed.getResolver(ItemCrystalSeed.FLUIX);
                }

                return null;
            }

            if (itemName.startsWith("material.")) {
                final String materialName = itemName.substring(itemName.indexOf('.') + 1);
                final MaterialType mt = MaterialType.valueOf(materialName.toUpperCase());
                // itemName = itemName.substring( 0, itemName.indexOf( "." ) );
                if (mt.getItemInstance() == ItemMaterial.instance && mt.getDamageValue() >= 0 && mt.isRegistered()) {
                    return new ResolverResult("material", mt.getDamageValue());
                }
            }

            if (itemName.startsWith("part.")) {
                final String partName = itemName.substring(itemName.indexOf('.') + 1);
                final PartType pt = PartType.valueOf(partName.toUpperCase());
                // itemName = itemName.substring( 0, itemName.indexOf( "." ) );
                final int dVal = ItemPart.instance.getDamageByType(pt);
                if (dVal >= 0) {
                    return new ResolverResult("part", dVal);
                }
            }
        }

        return null;
    }

    private Object paintBall(final AEColoredItemDefinition partType, final String substring, final boolean lumen) {
        AEColor col;

        try {
            col = AEColor.valueOf(substring.toUpperCase());
        } catch (final Throwable t) {
            col = AEColor.TRANSPARENT;
        }

        if (col == AEColor.TRANSPARENT) {
            return null;
        }

        final ItemStack is = partType.stack(col, 1);
        return new ResolverResult("paint_ball", (lumen ? 20 : 0) + is.getItemDamage());
    }

    private Object cableItem(final AEColoredItemDefinition partType, final String substring) {
        AEColor col;

        try {
            col = AEColor.valueOf(substring.toUpperCase());
        } catch (final Throwable t) {
            col = AEColor.TRANSPARENT;
        }

        final ItemStack is = partType.stack(col, 1);
        return new ResolverResult("part", is.getItemDamage());
    }
}
