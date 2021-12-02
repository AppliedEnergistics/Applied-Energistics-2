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

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.NumberEntryType;
import appeng.core.AEConfig;

/**
 * A utility widget that consists of a text-field to enter a number with attached buttons to increment/decrement the
 * number in fixed intervals.
 */
public class NumberEntryWidget extends GuiComponent implements ICompositeWidget {

    private static final Component INVALID_NUMBER = new TranslatableComponent(
            "gui.ae2.validation.InvalidNumber");
    private static final String NUMBER_LESS_THAN_MIN_VALUE = "gui.ae2.validation.NumberLessThanMinValue";
    private static final String NUMBER_GREATER_THAN_MAX_VALUE = "gui.ae2.validation.NumberGreaterThanMaxValue";
    private static final Component PLUS = new TextComponent("+");
    private static final Component MINUS = new TextComponent("-");
    private static final int TEXT_COLOR_ERROR = 0xFF1900;
    private static final int TEXT_COLOR_NORMAL = 0xFFFFFF;

    private final ConfirmableTextField textField;
    private NumberEntryType type;
    private Button[] minusButtons;
    private Button[] plusButtons;
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

    private Point textFieldOrigin = Point.ZERO;

    public NumberEntryWidget(NumberEntryType type) {
        this.type = type;

        Font font = Minecraft.getInstance().font;

        this.textField = new ConfirmableTextField(font, 0, 0, 0, font.lineHeight,
                TextComponent.EMPTY);
        this.textField.setBordered(false);
        this.textField.setMaxLength(16);
        this.textField.setTextColor(TEXT_COLOR_NORMAL);
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
    public void setTextFieldBounds(int x, int y, int width) {
        textFieldOrigin = new Point(x, y);
        this.textField.setWidth(width);
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
        final int[] steps = AEConfig.instance().getNumberEntrySteps(type);
        int a = steps[0];
        int b = steps[1];
        int c = steps[2];
        int d = steps[3];

        int left = bounds.getX() + this.bounds.getX();
        int top = bounds.getY() + this.bounds.getY();

        List<Button> buttons = new ArrayList<>(9);
        this.plusButtons = new Button[4];
        this.minusButtons = new Button[4];

        buttons.add(plusButtons[0] = new Button(left, top, 22, 20, makeLabel(PLUS, a), btn -> addQty(1)));
        buttons.add(plusButtons[1] = new Button(left + 28, top, 28, 20, makeLabel(PLUS, b), btn -> addQty(2)));
        buttons.add(plusButtons[2] = new Button(left + 62, top, 32, 20, makeLabel(PLUS, c), btn -> addQty(3)));
        buttons.add(plusButtons[3] = new Button(left + 100, top, 38, 20, makeLabel(PLUS, d), btn -> addQty(4)));

        // Need to add these now for sensible tab-order
        buttons.forEach(addWidget);

        // Placing this here will give a sensible tab order
        this.textField.x = bounds.getX() + textFieldOrigin.getX();
        this.textField.y = bounds.getY() + textFieldOrigin.getY();
        screen.setInitialFocus(this.textField);
        addWidget.accept(this.textField);

        buttons.add(minusButtons[0] = new Button(left, top + 42, 22, 20, makeLabel(MINUS, a), btn -> addQty(-1)));
        buttons.add(minusButtons[1] = new Button(left + 28, top + 42, 28, 20, makeLabel(MINUS, b), btn -> addQty(-2)));
        buttons.add(minusButtons[2] = new Button(left + 62, top + 42, 32, 20, makeLabel(MINUS, c), btn -> addQty(-3)));
        buttons.add(minusButtons[3] = new Button(left + 100, top + 42, 38, 20, makeLabel(MINUS, d), btn -> addQty(-4)));

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
        String text = textField.getValue().trim();
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
        String text = textField.getValue().trim();
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
        this.textField.setValue(String.valueOf(Math.max(minValue, value)));
        this.textField.moveCursorToEnd();
        this.textField.setHighlightPos(0);
        validate();
    }

    private void addQty(int i) {
        Preconditions.checkArgument(Math.abs(i) >= 1 && Math.abs(i) <= 4);

        var steps = AEConfig.instance().getNumberEntrySteps(type);
        var step = steps[Math.absExact(i) - 1];
        var delta = i < 0 ? -step : step;
        getLongValue().ifPresent(currentValue -> setValue(currentValue + delta));
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.textField.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void validate() {
        List<Component> validationErrors = new ArrayList<>();

        String text = textField.getValue().trim();
        try {
            long value = Long.parseLong(text, 10);
            if (value < minValue) {
                validationErrors.add(new TranslatableComponent(NUMBER_LESS_THAN_MIN_VALUE, minValue));
            } else if (value > maxValue) {
                validationErrors.add(new TranslatableComponent(NUMBER_GREATER_THAN_MAX_VALUE, maxValue));
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

    private Component makeLabel(Component prefix, int amount) {
        return prefix.plainCopy().append(String.valueOf(amount));
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

        var steps = AEConfig.instance().getNumberEntrySteps(type);
        for (int i = 0; i < steps.length; i++) {
            minusButtons[i].setMessage(makeLabel(MINUS, steps[i]));
            plusButtons[i].setMessage(makeLabel(PLUS, steps[i]));
        }
    }
}
