/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.items.parts;

import java.util.function.Function;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.Api;
import appeng.items.AEBaseItem;

import net.minecraft.world.item.Item.Properties;

public class PartItem<T extends IPart> extends AEBaseItem implements IPartItem<T> {

    private final Function<ItemStack, T> factory;

    public PartItem(net.minecraft.world.item.Item.Properties properties, Function<ItemStack, T> factory) {
        super(properties);
        this.factory = factory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack held = player.getItemInHand(context.getHand());
        if (held.getItem() != this) {
            return InteractionResult.PASS;
        }

        return Api.instance().partHelper().placeBus(held, context.getClickedPos(), context.getClickedFace(), player,
                context.getHand(), context.getLevel());
    }

    @Override
    public T createPart(ItemStack is) {
        return factory.apply(is);
    }

}
