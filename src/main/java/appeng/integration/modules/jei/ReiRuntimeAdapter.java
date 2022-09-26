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

package appeng.integration.modules.jei;

import java.util.Objects;

import com.google.common.base.Strings;

import me.shedaniel.rei.api.client.REIRuntime;

import appeng.integration.abstraction.IREI;

class ReiRuntimeAdapter implements IREI {

    private final REIRuntime runtime;

    ReiRuntimeAdapter() {
        this.runtime = Objects.requireNonNull(REIRuntime.getInstance(), "REI helper was null");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getSearchText() {
        var searchField = this.runtime.getSearchTextField();
        if (searchField == null) {
            return "";
        }
        return Strings.nullToEmpty(searchField.getText());
    }

    @Override
    public void setSearchText(String text) {
        var searchField = this.runtime.getSearchTextField();
        if (searchField != null) {
            searchField.setText(text);
        }
    }

    @Override
    public boolean hasSearchFocus() {
        var searchField = this.runtime.getSearchTextField();
        return searchField != null && searchField.isFocused();
    }
}
