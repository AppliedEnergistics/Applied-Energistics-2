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

package appeng.client.gui;

import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;

/**
 * Properties of {@link appeng.client.gui.style.Text} can be overridden by their ID. This class stores those overrides.
 */
public class TextOverride {

    /**
     * If this is not-null, this overrides the content to be displayed.
     */
    @Nullable
    private ITextComponent content;

    /**
     * If true, the text will not be drawn.
     */
    private boolean hidden;

    @Nullable
    public ITextComponent getContent() {
        return content;
    }

    public void setContent(@Nullable ITextComponent content) {
        this.content = content;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

}
