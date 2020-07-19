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
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.function.LongConsumer;
import java.util.regex.Pattern;

public class NumberBox extends TextFieldWidget {

    private final Class<?> type;

    private final LongConsumer changeListener;

    private long lastValue;

    public NumberBox(final FontRenderer fontRenderer, final int x, final int y, final int width, final int height,
                     final Class<?> type, LongConsumer changeListener) {
        super(fontRenderer, x, y, width, height, "0");
        this.type = type;
        this.setText("0");
        setResponder(this::onTextChanged);
        this.lastValue = 0;
        this.changeListener = changeListener;
    }

    private void onTextChanged(String text) {
        if (text.isEmpty() || !isValidNumber()) {
            setText("0"); // Will call onTextChanged recursively
            return;
        }

        StringBuilder sanitized = new StringBuilder(text);
        boolean encounteredNonZero = false;
        for (int i = 0; i < sanitized.length(); i++) {
            char ch = sanitized.charAt(i);
            if (ch >= '1' && ch <= '9') {
                encounteredNonZero = true;
                continue;
            }
            if (ch != '0' || !encounteredNonZero) {
                sanitized.deleteCharAt(i--);
            }
        }
        if (sanitized.length() == 0) {
            sanitized.append('0');
        }
        String sanitizedStr = sanitized.toString();
        if (!sanitizedStr.equals(text)) {
            setText(sanitizedStr); // Will call onTextChanged recursively
            return;
        }

        reportChange();
    }

    private void reportChange() {
        long value = getValue();
        if (value != lastValue) {
            lastValue = value;
            changeListener.accept(value);
        }
    }

    private boolean isValidNumber() {
        try {
            if (this.type == int.class || this.type == Integer.class) {
                Integer.parseInt(this.getText());
            } else if (this.type == long.class || this.type == Long.class) {
                Long.parseLong(this.getText());
            }
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    public long getValue() {
        return Long.parseLong(getText());
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        }

        // Swallow key presses for numbers because they would otherwise trigger the hotbar swapping unintentionally
        return isFocused() && p_keyPressed_1_ >= '0' && p_keyPressed_1_ <= '9';
    }
}
