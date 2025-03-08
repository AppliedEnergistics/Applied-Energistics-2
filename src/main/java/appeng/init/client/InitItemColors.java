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

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.items.misc.PaintBallItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.MemoryCardItem;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.items.tools.powered.PortableCellItem;

public final class InitItemColors {
    private InitItemColors() {
    }

    public static void init(RegisterColorHandlersEvent.ItemTintSources event) {
//        // Automatically make all registered itemcolors create opaque colors
// // TODO 1.21.4       init((itemColor, items) -> event.register(makeOpaque(itemColor), items));
  }
// // TODO 1.21.4
//    private static void init(ItemColorRegistrar registrar) {
//        // I checked, the ME chest doesn't keep its color in item form
//        registrar.register(new StaticItemColor(AEColor.TRANSPARENT), AEBlocks.ME_CHEST.asItem());
//
//        registrar.register(MemoryCardItem::getTintColor, AEItems.MEMORY_CARD);
//
//        registrar.register(InitItemColors::getColorApplicatorColor, AEItems.COLOR_APPLICATOR);
//
//        registrar.register(PortableCellItem::getColor, AEItems.PORTABLE_ITEM_CELL1K, AEItems.PORTABLE_FLUID_CELL1K,
//                AEItems.PORTABLE_ITEM_CELL4K, AEItems.PORTABLE_FLUID_CELL4K,
//                AEItems.PORTABLE_ITEM_CELL16K, AEItems.PORTABLE_FLUID_CELL16K,
//                AEItems.PORTABLE_ITEM_CELL64K, AEItems.PORTABLE_FLUID_CELL64K,
//                AEItems.PORTABLE_ITEM_CELL256K, AEItems.PORTABLE_FLUID_CELL256K);
//
//        registrar.register(BasicStorageCell::getColor, AEItems.ITEM_CELL_1K, AEItems.FLUID_CELL_1K,
//                AEItems.ITEM_CELL_4K, AEItems.FLUID_CELL_4K,
//                AEItems.ITEM_CELL_16K, AEItems.FLUID_CELL_16K,
//                AEItems.ITEM_CELL_64K, AEItems.FLUID_CELL_64K,
//                AEItems.ITEM_CELL_256K, AEItems.FLUID_CELL_256K);
//
//        // Automatically register colors for certain items we register
//        for (ItemDefinition<?> definition : AEItems.getItems()) {
//            Item item = definition.asItem();
//            if (item instanceof PartItem) {
//                AEColor color = AEColor.TRANSPARENT;
//                if (item instanceof ColoredPartItem) {
//                    color = ((ColoredPartItem<?>) item).getColor();
//                }
//                registrar.register(new StaticItemColor(color), item);
//            } else if (item instanceof PaintBallItem) {
//                registerPaintBall(registrar, (PaintBallItem) item);
//            }
//        }
//    }
//

//
//    private static int getColorApplicatorColor(ItemStack itemStack, int idx) {
//        if (idx == 0) {
//            return -1;
//        }
//
//        final AEColor col = ((ColorApplicatorItem) itemStack.getItem()).getActiveColor(itemStack);
//
//        if (col == null) {
//            return -1;
//        }
//
//        return switch (idx) {
//            case 1 -> col.blackVariant;
//            case 2 -> col.mediumVariant;
//            case 3 -> col.whiteVariant;
//            default -> -1;
//        };
//    }
//
//    private static ItemTintSource makeOpaque(ItemTintSource itemColor) {
//        return (stack, tintIndex) -> ARGB.opaque(itemColor.getColor(stack, tintIndex));
//    }
}
