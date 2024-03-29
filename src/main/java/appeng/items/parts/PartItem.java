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

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.items.AEBaseItem;

public class PartItem<T extends IPart> extends AEBaseItem implements IPartItem<T> {

    private final Class<T> partClass;
    private final Function<IPartItem<T>, T> factory;

    public PartItem(Properties properties, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        super(properties);
        this.partClass = partClass;
        this.factory = factory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PartHelper.usePartItem(context);
    }

    @Override
    public Class<T> getPartClass() {
        return partClass;
    }

    @Override
    public T createPart() {
        return factory.apply(this);
    }

}
