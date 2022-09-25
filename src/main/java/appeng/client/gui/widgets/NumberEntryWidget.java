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

package appeng.client.gui.widgets;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.MathExpressionParser;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.Rects;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.core.localization.GuiText;

/**
 * A utility widget that consists of a text-field to enter a number with attached buttons to increment/decrement the
 * number in fixed intervals.
 */
public class NumberEntryWidget extends GuiComponent implements ICompositeWidget {

    private static final long[] STEPS = new long[] { 1, 10, 100, 1000 };
    private static final Component PLUS = Component.literal("+");
    private static final Component MINUS = Component.literal("-");
    private final int errorTextColor;
    private final int normalTextColor;

    private final ConfirmableTextField textField;
    private final DecimalFormat decimalFormat;
    private NumberEntryType type;
    private List<Button> buttons;
    private long minValue;
    private long maxValue = Long.MAX_VALUE;
    private ValidationIcon validationIcon;

    // Called when the value changes
    private Runnable onChange;

    // Called when the user presses enter while there's a valid number in the field
    private Runnable onConfirm;

    private boolean hideValidationIcon;

    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    private Rect2i textFieldBounds = Rects.ZERO;

    public NumberEntryWidget(ScreenStyle style, NumberEntryType type) {
        this.errorTextColor = style.getColor(PaletteColor.TEXTFIELD_ERROR).toARGB();
        this.normalTextColor = style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB();

        this.type = Objects.requireNonNull(type, "type");
        this.decimalFormat = new DecimalFormat("#.######", new DecimalFormatSymbols());
        this.decimalFormat.setParseBigDecimal(true);
        this.decimalFormat.setNegativePrefix("-");

        Font font = Minecraft.getInstance().font;

        this.textField = new ConfirmableTextField(style, font, 0, 0, 0, font.lineHeight);
        this.textField.setBordered(false);
        this.textField.setMaxLength(16);
        this.textField.setTextColor(normalTextColor);
        this.textField.setVisible(true);
        this.textField.setFocus(true);
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
        this.textField.setEditable(active);
        this.buttons.forEach(b -> b.active = active);
    }

    /**
     * Sets the bounds of the text field on the screen. This may seem insane, but the text-field background is actually
     * baked into the screens background image, which necessitates setting it precisely.
     */
    public void setTextFieldBounds(Rect2i bounds) {
        this.textFieldBounds = bounds;
        this.textField.move(Point.fromTopLeft(bounds));
        this.textField.resize(bounds.getWidth(), bounds.getHeight());
    }

    public void setTextFieldStyle(WidgetStyle style) {
        int left = 0;
        if (style.getLeft() != null) {
            left = style.getLeft();
        }
        int top = 0;
        if (style.getTop() != null) {
            top = style.getTop();
        }
        setTextFieldBounds(new Rect2i(
                left,
                top,
                style.getWidth(),
                style.getHeight()));
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
        validate();
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
        validate();
    }

    @Override
    public void setPosition(Point position) {
        bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return bounds;
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        int left = bounds.getX() + this.bounds.getX();
        int top = bounds.getY() + this.bounds.getY();

        List<Button> buttons = new ArrayList<>(9);

        buttons.add(new Button(left, top, 22, 20, makeLabel(PLUS, STEPS[0]), btn -> addQty(STEPS[0])));
        buttons.add(new Button(left + 28, top, 28, 20, makeLabel(PLUS, STEPS[1]), btn -> addQty(STEPS[1])));
        buttons.add(new Button(left + 62, top, 32, 20, makeLabel(PLUS, STEPS[2]), btn -> addQty(STEPS[2])));
        buttons.add(new Button(left + 100, top, 38, 20, makeLabel(PLUS, STEPS[3]), btn -> addQty(STEPS[3])));

        // Need to add these now for sensible tab-order
        buttons.forEach(addWidget);

        // Placing this here will give a sensible tab order
        var textFieldBounds = Rects.move(this.textFieldBounds, bounds.getX(), bounds.getY());
        this.textField.move(Point.fromTopLeft(textFieldBounds));
        this.textField.resize(textFieldBounds.getWidth(), textFieldBounds.getHeight());
        screen.setInitialFocus(this.textField);
        addWidget.accept(this.textField);

        buttons.add(new Button(left, top + 42, 22, 20, makeLabel(MINUS, STEPS[0]), btn -> addQty(-STEPS[0])));
        buttons.add(new Button(left + 28, top + 42, 28, 20, makeLabel(MINUS, STEPS[1]), btn -> addQty(-STEPS[1])));
        buttons.add(new Button(left + 62, top + 42, 32, 20, makeLabel(MINUS, STEPS[2]), btn -> addQty(-STEPS[2])));
        buttons.add(new Button(left + 100, top + 42, 38, 20, makeLabel(MINUS, STEPS[3]), btn -> addQty(-STEPS[3])));

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
        var value = getLongValue();
        if (value.isPresent()) {
            var longValue = value.getAsLong();
            if (longValue > Integer.MAX_VALUE) {
                return OptionalInt.empty();
            }
            return OptionalInt.of((int) longValue);
        }
        return OptionalInt.empty();
    }

    /**
     * Returns the long value currently in the text-field, if it is a valid number and is within the allowed min/max
     * value.
     */
    public OptionalLong getLongValue() {
        var internalValue = getValueInternal();
        if (internalValue.isEmpty()) {
            return OptionalLong.empty();
        }

        var externalValue = convertToExternalValue(internalValue.get());
        if (externalValue < minValue) {
            return OptionalLong.empty();
        } else if (externalValue > maxValue) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(externalValue);
    }

    public void setLongValue(long value) {
        var internalValue = convertToInternalValue(Mth.clamp(value, minValue, maxValue));
        this.textField.setValue(decimalFormat.format(internalValue));
        this.textField.moveCursorToEnd();
        this.textField.setHighlightPos(0);
        validate();
    }

    private void addQty(long delta) {
        var currentValue = getValueInternal().orElse(BigDecimal.ZERO);
        var newValue = currentValue.add(BigDecimal.valueOf(delta));
        var minimum = BigDecimal.valueOf(this.minValue);
        if (newValue.compareTo(minimum) < 0) {
            newValue = minimum;
        } else if (currentValue.compareTo(BigDecimal.ONE) == 0 && delta > 0 && delta % 10 == 0) {
            newValue = newValue.subtract(BigDecimal.ONE);
        }
        setValueInternal(newValue);
    }

    /**
     * Retrieves the numeric representation of the value entered by the user, if it is convertible.
     */
    private Optional<BigDecimal> getValueInternal() {
        return MathExpressionParser.parse(textField.getValue(), decimalFormat);
    }

    /*
     * Return true if the value entered by the user is a single numeric number and not a mathematical expression
     */
    private boolean isNumber() {
        var position = new ParsePosition(0);
        var textValue = textField.getValue().trim();
        decimalFormat.parse(textValue, position);
        return position.getErrorIndex() == -1 && position.getIndex() == textValue.length();
    }

    /**
     * Changes the value displayed to the user.
     */
    private void setValueInternal(BigDecimal value) {
        textField.setValue(decimalFormat.format(value));
    }

    private void validate() {
        List<Component> validationErrors = new ArrayList<>();
        List<Component> infoMessages = new ArrayList<>();

        var possibleValue = getValueInternal();
        if (possibleValue.isPresent()) {
            // Reject decimal values if the unit is integral
            if (type.amountPerUnit() == 1 && possibleValue.get().scale() > 0) {
                validationErrors.add(Component.literal("Must be whole number"));
            } else {
                var value = convertToExternalValue(possibleValue.get());
                if (value < minValue) {
                    var formatted = decimalFormat.format(convertToInternalValue(minValue));
                    validationErrors.add(GuiText.NumberLessThanMinValue.text(formatted));
                } else if (value > maxValue) {
                    var formatted = decimalFormat.format(convertToInternalValue(maxValue));
                    validationErrors.add(GuiText.NumberGreaterThanMaxValue.text(formatted));
                } else if (!isNumber()) { // is a mathematical expression
                    // displaying the evaluation of the expression
                    infoMessages.add(Component.literal("= " + decimalFormat.format(possibleValue.get())));
                }
            }
        } else {
            validationErrors.add(GuiText.InvalidNumber.text());
        }

        boolean valid = validationErrors.isEmpty();
        var tooltip = valid ? infoMessages : validationErrors;
        this.textField.setTextColor(valid ? normalTextColor : errorTextColor);
        this.textField.setTooltipMessage(tooltip);

        if (this.validationIcon != null) {
            this.validationIcon.setValid(valid);
            this.validationIcon.setTooltip(tooltip);
        }
    }

    private Component makeLabel(Component prefix, long amount) {
        return prefix.plainCopy().append(decimalFormat.format(amount));
    }

    public void setHideValidationIcon(boolean hideValidationIcon) {
        this.hideValidationIcon = hideValidationIcon;
    }

    public NumberEntryType getType() {
        return type;
    }

    public void setType(NumberEntryType type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        // Update the external with the now changed scaling
        if (onChange != null) {
            onChange.run();
        }

        validate();
    }

    private long convertToExternalValue(BigDecimal internalValue) {
        var multiplicand = BigDecimal.valueOf(type.amountPerUnit());
        var value = internalValue.multiply(multiplicand, MathContext.DECIMAL128);
        value = value.setScale(0, RoundingMode.UP);
        return value.longValue();
    }

    private BigDecimal convertToInternalValue(long externalValue) {
        var divisor = BigDecimal.valueOf(type.amountPerUnit());
        return BigDecimal.valueOf(externalValue).divide(divisor, MathContext.DECIMAL128);
    }

    @Override
    public void drawBackgroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        if (type.unit() != null) {
            var font = Minecraft.getInstance().font;
            font.draw(
                    poseStack,
                    type.unit(),
                    bounds.getX() + textFieldBounds.getX() + textFieldBounds.getWidth() + 3,
                    bounds.getY() + textFieldBounds.getY() + (textFieldBounds.getHeight() - font.lineHeight) / 2f + 1,
                    ChatFormatting.DARK_GRAY.getColor());
        }
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        if (textFieldBounds.contains(mousePos.getX(), mousePos.getY())) {
            if (getValueInternal().isPresent()) {
                if (delta < 0) {
                    addQty(-1);
                } else if (delta > 0) {
                    addQty(1);
                }

                return true;
            }
        }
        return false;
    }
}
