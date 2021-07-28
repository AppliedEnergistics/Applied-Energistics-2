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

package appeng.items.materials;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.items.AEBaseItem;

public class NamePressItem extends AEBaseItem {
    /**
     * NBT property used by the name press to store the name to be inscribed.
     */
    public static final String TAG_INSCRIBE_NAME = "InscribeName";

    public NamePressItem(Item.Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level world, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, world, lines, advancedTooltips);

        final CompoundTag c = stack.getOrCreateTag();
        if (c.contains(TAG_INSCRIBE_NAME)) {
            lines.add(new TextComponent(c.getString(TAG_INSCRIBE_NAME)));
        }
    }
}
