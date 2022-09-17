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

package appeng.core.features.registries.inscriber;


import appeng.api.features.InscriberProcessType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;


/**
 * inscribe recipes do not use up the provided optional upon craft
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public class InscriberInscribeRecipe extends InscriberRecipe {
    InscriberInscribeRecipe(@Nonnull final Collection<ItemStack> inputs, @Nonnull final ItemStack output, @Nullable final ItemStack top, @Nullable final ItemStack bot) {
        super(inputs, output, top, bot, InscriberProcessType.INSCRIBE);
    }
}
