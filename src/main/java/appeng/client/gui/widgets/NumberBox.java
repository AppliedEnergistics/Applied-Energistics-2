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

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;

// FIXME: Fix this piece of crap (i.e. onChange listener)
public class NumberBox extends TextFieldWidget {

    private final Class type;

    public NumberBox(final TextRenderer fontRenderer, final int x, final int y, final int width, final int height,
                     final Class type) {
        super(fontRenderer, x, y, width, height, new LiteralText("0"));
        this.type = type;
    }

    @Override
    public void write(final String selectedText) {
        final String original = this.getText();
        super.write(selectedText);

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
