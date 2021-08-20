/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.init.client;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.items.misc.FluidDummyItem;
import appeng.items.misc.PaintBallItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.tools.powered.ColorApplicatorItem;

public final class InitItemColors {
    private InitItemColors() {
    }

    public static void init(Registry itemColors) {
        itemColors.register(new StaticItemColor(AEColor.TRANSPARENT), AEBlocks.SECURITY_STATION.asItem());
        // I checked, the ME chest doesn't keep its color in item form
        itemColors.register(new StaticItemColor(AEColor.TRANSPARENT), AEBlocks.CHEST.asItem());

        itemColors.register(InitItemColors::getColorApplicatorColor, AEItems.COLOR_APPLICATOR);

        itemColors.register(InitItemColors::getDummyFluidItemColor, AEItems.DUMMY_FLUID_ITEM);

        // Automatically register colors for certain items we register
        for (ItemDefinition<?> definition : AEItems.getItems()) {
            Item item = definition.asItem();
            if (item instanceof PartItem) {
                AEColor color = AEColor.TRANSPARENT;
                if (item instanceof ColoredPartItem) {
                    color = ((ColoredPartItem<?>) item).getColor();
                }
                itemColors.register(new StaticItemColor(color), item);
            } else if (item instanceof PaintBallItem) {
                registerPaintBall(itemColors, (PaintBallItem) item);
            }
        }
    }

    /**
     * We use a white base item icon for paint balls. This applies the correct color to it.
     */
    private static void registerPaintBall(Registry colors, PaintBallItem item) {
        AEColor color = item.getColor();
        final int colorValue = item.isLumen() ? color.mediumVariant : color.mediumVariant;
        final int r = colorValue >> 16 & 0xff;
        final int g = colorValue >> 8 & 0xff;
        final int b = colorValue & 0xff;

        int renderColor;
        if (item.isLumen()) {
            final float fail = 0.7f;
            final int full = (int) (255 * 0.3);
            renderColor = (int) (full + r * fail) << 16 | (int) (full + g * fail) << 8 | (int) (full + b * fail)
                    | 0xff << 24;
        } else {
            renderColor = r << 16 | g << 8 | b | 0xff << 24;
        }

        colors.register((is, tintIndex) -> renderColor, item);
    }

    private static int getColorApplicatorColor(ItemStack itemStack, int idx) {
        if (idx == 0) {
            return -1;
        }

        final AEColor col = ((ColorApplicatorItem) itemStack.getItem()).getActiveColor(itemStack);

        if (col == null) {
            return -1;
        }

        return switch (idx) {
            case 1 -> col.blackVariant;
            case 2 -> col.mediumVariant;
            case 3 -> col.whiteVariant;
            default -> -1;
        };
    }

    private static int getDummyFluidItemColor(ItemStack stack, int tintIndex) {

        Item item = stack.getItem();
        if (!(item instanceof FluidDummyItem fluidItem)) {
            return -1;
        }

        return FluidVariantRendering.getColor(fluidItem.getFluid(stack));
    }

    @FunctionalInterface
    public interface Registry {
        void register(ItemColor itemColor, ItemLike... itemLikes);
    }

}
