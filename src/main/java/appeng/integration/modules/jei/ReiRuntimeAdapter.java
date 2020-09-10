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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.gui.widget.TextFieldWidget;

import appeng.integration.abstraction.IRei;

class ReiRuntimeAdapter implements IRei {

    private final REIHelper runtime;

    ReiRuntimeAdapter() {
        this.runtime = Preconditions.checkNotNull(REIHelper.getInstance(), "REI helper was null");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getSearchText() {
        TextFieldWidget searchField = this.runtime.getSearchTextField();
        if (searchField == null) {
            return "";
        }
        return Strings.nullToEmpty(searchField.getText());
    }

    @Override
    public void setSearchText(String searchText) {
        TextFieldWidget searchField = this.runtime.getSearchTextField();
        if (searchField != null) {
            searchField.setText(Strings.nullToEmpty(searchText));
        }
    }
}
