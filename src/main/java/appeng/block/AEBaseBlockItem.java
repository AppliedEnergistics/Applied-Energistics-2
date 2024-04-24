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

package appeng.block;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AEBaseBlockItem extends BlockItem {

    private final AEBaseBlock blockType;

    public AEBaseBlockItem(Block id, Properties props) {
        super(id, props);
        this.blockType = (AEBaseBlock) id;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> toolTip,
            TooltipFlag advancedTooltips) {
        this.addCheckedInformation(itemStack, context, toolTip, advancedTooltips);
    }

    @OnlyIn(Dist.CLIENT)
    public void addCheckedInformation(ItemStack itemStack, TooltipContext context, List<Component> toolTip,
            TooltipFlag advancedTooltips) {
        this.blockType.appendHoverText(itemStack, context, toolTip, advancedTooltips);
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

    @Override
    public String getDescriptionId(ItemStack is) {
        return this.blockType.getDescriptionId();
    }

}
