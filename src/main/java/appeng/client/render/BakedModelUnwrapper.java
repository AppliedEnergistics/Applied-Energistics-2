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

package appeng.client.render;

import net.minecraft.client.resources.model.BakedModel;

import appeng.client.render.model.AutoRotatingBakedModel;

/**
 * Helper to get a specific baked model class out of a chain of delegating baked models.
 */
public final class BakedModelUnwrapper {

    private BakedModelUnwrapper() {
    }

    public static <T> T unwrap(BakedModel model, Class<T> targetClass) {
        if (targetClass.isInstance(model)) {
            return targetClass.cast(model);
        }

        if (model instanceof AutoRotatingBakedModel) {
            model = ((AutoRotatingBakedModel) model).getWrapped();
            if (targetClass.isInstance(model)) {
                return targetClass.cast(model);
            }
        }

        return null;
    }

}
