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

package appeng.crafting.pattern;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.storage.data.AEItemKey;

public class AEPatternDecoder implements IPatternDetailsDecoder {
    public static final AEPatternDecoder INSTANCE = new AEPatternDecoder();

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        return stack.getItem() instanceof EncodedPatternItem;
    }

    @Nullable
    @Override
    public IAEPatternDetails decodePattern(AEItemKey what, Level level) {
        if (level == null || !(what.getItem() instanceof EncodedPatternItem encodedPatternItem)) {
            return null;
        }

        return encodedPatternItem.decode(what, level);
    }

    @Nullable
    @Override
    public IAEPatternDetails decodePattern(ItemStack what, Level level, boolean tryRecovery) {
        if (level == null || !(what.getItem() instanceof EncodedPatternItem encodedPatternItem)) {
            return null;
        }

        return encodedPatternItem.decode(what, level, tryRecovery);
    }
}
