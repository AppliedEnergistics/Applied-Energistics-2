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

import java.util.function.LongConsumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;

public class NumberBox extends TextFieldWidget {

    private final LongConsumer changeListener;

    private long lastValue;

    private long minValue = 0;

    private long maxValue;

    public NumberBox(final TextRenderer fontRenderer, final int x, final int y, final int width, final int height,
                     final Class<?> type, LongConsumer changeListener) {
        super(fontRenderer, x, y, width, height, new LiteralText("0"));
        this.setText("0");
        setResponder(this::onTextChanged);
        this.lastValue = 0;
        this.changeListener = changeListener;
        if (type == int.class || type == Integer.class) {
            maxValue = Integer.MAX_VALUE;
        } else {
            maxValue = Long.MAX_VALUE;
        }
    }

    private void onTextChanged(String text) {
        if (text.isEmpty()) {
            setText("0"); // Will call onTextChanged recursively
            return;
        }

        boolean canBeNegative = canBeNegative();
        if (canBeNegative && text.equals("-")) {
            // Allow this as a special case to make typing in a negative number easier
            return;
        }

        StringBuilder sanitized = new StringBuilder(text);
        boolean encounteredNonZero = false;
        for (int i = 0; i < sanitized.length(); i++) {
            char ch = sanitized.charAt(i);
            if (canBeNegative && i == 0 && ch == '-') {
                continue; // Allow leading minus sign
            }
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
        if (getValue() < minValue) {
            setText(String.valueOf(minValue)); // Will call onTextChanged recursively
            return;
        }
        if (getValue() > maxValue) {
            setText(String.valueOf(maxValue)); // Will call onTextChanged recursively
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

    public void setValue(long value, boolean skipNotify) {
        // This check avoid changing the cursor position needlessly
        if (value == this.getValue()) {
            return;
        }

        if (skipNotify) {
            lastValue = value;
        }
        setText(String.valueOf(value));
    }

    public long getValue() {
        if (getText().equals("-") && canBeNegative()) {
            return lastValue; // Allow this as a special case to type in a negative number more easily
        }

        return Long.parseLong(getText());
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        }

        // Swallow key presses for numbers because they would otherwise trigger the
        // hotbar swapping unintentionally
        return isFocused() && p_keyPressed_1_ >= '0' && p_keyPressed_1_ <= '9';
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public long getMinValue() {
        return minValue;
    }

    private boolean canBeNegative() {
        return minValue < 0;
    }

}
