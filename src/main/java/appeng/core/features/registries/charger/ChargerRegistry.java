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

package appeng.core.features.registries.charger;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;

import appeng.api.features.IChargerRegistry;

public class ChargerRegistry implements IChargerRegistry {
    private static final double DEFAULT_CHARGE_RATE = 160d;
    private static final double CAPPED_CHARGE_RATE = 16000d;

    private final Map<Item, Double> chargeRates;

    public ChargerRegistry() {
        this.chargeRates = new IdentityHashMap<>();
    }

    @Override
    @Nonnegative
    public double getChargeRate(@Nonnull IItemProvider item) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.asItem());

        return this.chargeRates.getOrDefault(item.asItem(), DEFAULT_CHARGE_RATE);
    }

    @Override
    public void addChargeRate(@Nonnull IItemProvider item, @Nonnegative double value) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.asItem());
        Preconditions.checkArgument(value > 0d);

        final double cappedValue = Math.min(value, CAPPED_CHARGE_RATE);

        this.chargeRates.put(item.asItem(), cappedValue);
    }

    @Override
    public void removeChargeRate(@Nonnull IItemProvider item) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.asItem());

        this.chargeRates.remove(item.asItem());
    }

}
