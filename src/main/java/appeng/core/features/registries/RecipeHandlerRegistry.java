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

package appeng.core.features.registries;


import appeng.api.features.IRecipeHandlerRegistry;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.ISubItemResolver;
import appeng.core.AELog;
import appeng.recipes.RecipeHandler;

import javax.annotation.Nullable;
import java.util.*;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv3 - 10.08.2015
 * @since rv0
 */
public class RecipeHandlerRegistry implements IRecipeHandlerRegistry {
    private final Map<String, Class<? extends ICraftHandler>> handlers = new HashMap<>(20);
    private final Collection<ISubItemResolver> resolvers = new ArrayList<>();

    @Override
    public void addNewCraftHandler(final String name, final Class<? extends ICraftHandler> handler) {
        this.handlers.put(name.toLowerCase(Locale.ENGLISH), handler);
    }

    @Override
    public void addNewSubItemResolver(final ISubItemResolver sir) {
        this.resolvers.add(sir);
    }

    @Nullable
    @Override
    public ICraftHandler getCraftHandlerFor(final String name) {
        final Class<? extends ICraftHandler> clz = this.handlers.get(name);
        if (clz == null) {
            return null;
        }
        try {
            return clz.newInstance();
        } catch (final Throwable e) {
            AELog.error("Error Caused when trying to construct " + clz.getName());
            AELog.debug(e);

            this.handlers.put(name, null); // clear it..

            return null;
        }
    }

    @Override
    public IRecipeHandler createNewRecipehandler() {
        return new RecipeHandler();
    }

    @Nullable
    @Override
    public Object resolveItem(final String nameSpace, final String itemName) {
        for (final ISubItemResolver sir : this.resolvers) {
            Object rr = null;

            try {
                rr = sir.resolveItemByName(nameSpace, itemName);
            } catch (final Throwable t) {
                AELog.debug(t);
            }

            if (rr != null) {
                return rr;
            }
        }

        return null;
    }
}
