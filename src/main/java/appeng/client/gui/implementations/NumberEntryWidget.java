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

package appeng.client.gui.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.widgets.ConfirmableTextField;
import appeng.client.gui.widgets.ValidationIcon;
import appeng.core.AEConfig;

/**
 * A utility widget that consists of a text-field to enter a number with attached buttons to increment/decrement the
 * number in fixed intervals.
 */
public class NumberEntryWidget extends AbstractGui implements ICompositeWidget {

    private static final ITextComponent INVALID_NUMBER = new TranslationTextComponent(
            "gui.appliedenergistics2.validation.InvalidNumber");
    private static final String NUMBER_LESS_THAN_MIN_VALUE = "gui.appliedenergistics2.validation.NumberLessThanMinValue";
    private static final ITextComponent PLUS = new StringTextComponent("+");
    private static final ITextComponent MINUS = new StringTextComponent("-");
    private static final int TEXT_COLOR_ERROR = 0xFF1900;
    private static final int TEXT_COLOR_NORMAL = 0xFFFFFF;

    private final ConfirmableTextField textField;
    private final NumberEntryType type;
    private List<Button> buttons;
    private long minValue;
    private ValidationIcon validationIcon;

    // Called when the value changes
    private Runnable onChange;

    // Called when the user presses enter while there's a valid number in the field
    private Runnable onConfirm;

    private boolean hideValidationIcon;

    private Rectangle2d bounds = new Rectangle2d(0, 0, 0, 0);

    private Point textFieldOrigin = Point.ZERO;

    public NumberEntryWidget(NumberEntryType type) {
        this.type = type;

        FontRenderer font = Minecraft.getInstance().fontRenderer;

        this.textField = new ConfirmableTextField(font, 0, 0, 0, font.FONT_HEIGHT,
                StringTextComponent.EMPTY);
        this.textField.setEnableBackgroundDrawing(false);
        this.textField.setMaxStringLength(16);
        this.textField.setTextColor(TEXT_COLOR_NORMAL);
        this.textField.setVisible(true);
        this.textField.setFocused2(true);
        this.textField.setResponder(text -> {
            validate();
            if (onChange != null) {
                this.onChange.run();
            }
        });
        this.textField.setOnConfirm(() -> {
            // Only confirm if it's actually valid
            if (this.onConfirm != null && getLongValue().isPresent()) {
                this.onConfirm.run();
            }
        });
        validate();
    }

    public void setOnConfirm(Runnable callback) {
        this.onConfirm = callback;
    }

    public void setOnChange(Runnable callback) {
        this.onChange = callback;
    }

    public void setActive(boolean active) {
        this.textField.setEnabled(active);
        this.buttons.forEach(b -> b.active = active);
    }

    /**
     * Sets the bounds of the text field on the screen. This may seem insane, but the text-field background is actually
     * baked into the screens background image, which necessitates setting it precisely.
     */
    public void setTextFieldBounds(int x, int y, int width) {
        textFieldOrigin = new Point(x, y);
        this.textField.setWidth(width);
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
        validate();
    }

    @Override
    public void setPosition(Point position) {
        bounds = new Rectangle2d(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        bounds = new Rectangle2d(bounds.getX(), bounds.getY(), width, height);
    }

    @Override
    public Rectangle2d getBounds() {
        return bounds;
    }

    @Override
    public void populateScreen(Consumer<Widget> addWidget, Rectangle2d bounds, AEBaseScreen<?> screen) {
        final int[] steps = AEConfig.instance().getNumberEntrySteps(type);
        int a = steps[0];
        int b = steps[1];
        int c = steps[2];
        int d = steps[3];

        int left = bounds.getX() + this.bounds.getX();
        int top = bounds.getY() + this.bounds.getY();

        List<Button> buttons = new ArrayList<>(9);

        buttons.add(new Button(left, top, 22, 20, makeLabel(PLUS, a), btn -> addQty(a)));
        buttons.add(new Button(left + 28, top, 28, 20, makeLabel(PLUS, b), btn -> addQty(b)));
        buttons.add(new Button(left + 62, top, 32, 20, makeLabel(PLUS, c), btn -> addQty(c)));
        buttons.add(new Button(left + 100, top, 38, 20, makeLabel(PLUS, d), btn -> addQty(d)));

        // Need to add these now for sensible tab-order
        buttons.forEach(addWidget);

        // Placing this here will give a sensible tab order
        this.textField.x = bounds.getX() + textFieldOrigin.getX();
        this.textField.y = bounds.getY() + textFieldOrigin.getY();
        screen.setFocusedDefault(this.textField);
        addWidget.accept(this.textField);

        buttons.add(new Button(left, top + 42, 22, 20, makeLabel(MINUS, a), btn -> addQty(-a)));
        buttons.add(new Button(left + 28, top + 42, 28, 20, makeLabel(MINUS, b), btn -> addQty(-b)));
        buttons.add(new Button(left + 62, top + 42, 32, 20, makeLabel(MINUS, c), btn -> addQty(-c)));
        buttons.add(new Button(left + 100, top + 42, 38, 20, makeLabel(MINUS, d), btn -> addQty(-d)));

        // This element is not focusable
        if (!hideValidationIcon) {
            this.validationIcon = new ValidationIcon();
            this.validationIcon.x = left + 104;
            this.validationIcon.y = top + 27;
            buttons.add(this.validationIcon);
        }

        // Add the rest to the tab order
        buttons.subList(4, buttons.size()).forEach(addWidget);

        this.buttons = buttons;

        // we need to re-validate because the icon may now be present and needs it's
        // initial state
        this.validate();

        screen.changeFocus(true);
    }

    /**
     * Returns the integer value currently in the text-field, if it is a valid number and is within the allowed min/max
     * value.
     */
    public OptionalInt getIntValue() {
        String text = textField.getText().trim();
        try {
            int value = Integer.parseInt(text, 10);
            if (value < minValue) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(value);
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    /**
     * Returns the long value currently in the text-field, if it is a valid number and is within the allowed min/max
     * value.
     */
    public OptionalLong getLongValue() {
        String text = textField.getText().trim();
        try {
            long value = Long.parseLong(text, 10);
            if (value < minValue) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(value);
        } catch (NumberFormatException ignored) {
            return OptionalLong.empty();
        }
    }

    public void setValue(long value) {
        this.textField.setText(String.valueOf(Math.max(minValue, value)));
        this.textField.setCursorPositionEnd();
        this.textField.setSelectionPos(0);
        validate();
    }

    private void addQty(final long i) {
        getLongValue().ifPresent(currentValue -> setValue(currentValue + i));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.textField.render(matrices, mouseX, mouseY, partialTicks);
    }

    private void validate() {
        List<ITextComponent> validationErrors = new ArrayList<>();

        String text = textField.getText().trim();
        try {
            long value = Long.parseLong(text, 10);
            if (value < minValue) {
                validationErrors.add(new TranslationTextComponent(NUMBER_LESS_THAN_MIN_VALUE, minValue));
            }
        } catch (NumberFormatException ignored) {
            validationErrors.add(INVALID_NUMBER);
        }

        boolean valid = validationErrors.isEmpty();
        this.textField.setTextColor(valid ? TEXT_COLOR_NORMAL : TEXT_COLOR_ERROR);
        if (this.validationIcon != null) {
            this.validationIcon.setValid(valid);
            this.validationIcon.setTooltip(validationErrors);
        }
    }

    private ITextComponent makeLabel(ITextComponent prefix, int amount) {
        return prefix.copyRaw().appendString(String.valueOf(amount));
    }

    public void setHideValidationIcon(boolean hideValidationIcon) {
        this.hideValidationIcon = hideValidationIcon;
    }

}
