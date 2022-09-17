/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.stats;


import appeng.core.AppEng;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.advancements.critereon.ItemPredicates;


public class PartItemPredicate extends ItemPredicate {
    private final PartType partType;

    public PartItemPredicate(String partName) {
        this.partType = PartType.valueOf(partName.toUpperCase());
    }

    @Override
    public boolean test(ItemStack item) {
        if (ItemPart.instance != null && item.getItem() == ItemPart.instance) {
            return ItemPart.instance.getTypeByStack(item) == this.partType;
        }
        return false;
    }

    public static ItemPredicate deserialize(JsonObject jsonobject) {
        if (jsonobject.has("part")) {
            return new PartItemPredicate(JsonUtils.getString(jsonobject, "part"));
        } else {
            return ItemPredicate.ANY;
        }
    }

    public static void register() {
        ItemPredicates.register(new ResourceLocation(AppEng.MOD_ID, "part"), PartItemPredicate::deserialize);
    }
}
