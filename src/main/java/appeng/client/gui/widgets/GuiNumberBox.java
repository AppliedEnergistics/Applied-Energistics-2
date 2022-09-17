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

package appeng.client.gui.widgets;


import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;


public class GuiNumberBox extends GuiTextField {

    private final Class type;

    public GuiNumberBox(final FontRenderer fontRenderer, final int x, final int y, final int width, final int height, final Class type) {
        super(0, fontRenderer, x, y, width, height);
        this.type = type;
    }

    @Override
    public void writeText(final String selectedText) {
        final String original = this.getText();
        super.writeText(selectedText);

        try {
            if (this.type == int.class || this.type == Integer.class) {
                Integer.parseInt(this.getText());
            } else if (this.type == long.class || this.type == Long.class) {
                Long.parseLong(this.getText());
            } else if (this.type == double.class || this.type == Double.class) {
                Double.parseDouble(this.getText());
            }
        } catch (final NumberFormatException e) {
            this.setText(original);
        }
    }
}
