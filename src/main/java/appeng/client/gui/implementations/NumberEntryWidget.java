package appeng.client.gui.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.widgets.ConfirmableTextField;
import appeng.client.gui.widgets.ValidationIcon;
import appeng.core.AEConfig;

/**
 * A utility widget that consists of a text-field to enter a number with attached buttons to increment/decrement the
 * number in fixed intervals.
 */
public class NumberEntryWidget extends AbstractGui {

    private static final ITextComponent INVALID_NUMBER = new TranslationTextComponent(
            "gui.appliedenergistics2.validation.InvalidNumber");
    private static final String NUMBER_LESS_THAN_MIN_VALUE = "gui.appliedenergistics2.validation.NumberLessThanMinValue";
    private static final ITextComponent PLUS = new StringTextComponent("+");
    private static final ITextComponent MINUS = new StringTextComponent("-");
    private static final int TEXT_COLOR_ERROR = 0xFF1900;
    private static final int TEXT_COLOR_NORMAL = 0xFFFFFF;

    private final AEBaseScreen<?> parent;

    private final int x;
    private final int y;

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

    public NumberEntryWidget(AEBaseScreen<?> parent, int x, int y, int width, int height, NumberEntryType type) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.type = type;

        FontRenderer font = parent.getMinecraft().font;
        int inputX = parent.getGuiLeft() + x;
        int inputY = parent.getGuiTop() + y;
        this.textField = new ConfirmableTextField(font, inputX, inputY, width, font.lineHeight,
                StringTextComponent.EMPTY);
        this.textField.setBordered(false);
        this.textField.setMaxLength(16);
        this.textField.setTextColor(TEXT_COLOR_NORMAL);
        this.textField.setVisible(true);
        this.textField.setFocus(true);
        parent.setInitialFocus(this.textField);
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

    public void setTextFieldBounds(int x, int y, int width) {
        this.textField.x = parent.getGuiLeft() + x;
        this.textField.y = parent.getGuiTop() + y;
        this.textField.setWidth(width);
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
        validate();
    }

    public void addButtons(Consumer<IGuiEventListener> addChildren, Consumer<Button> addButton) {
        final int[] steps = AEConfig.instance().getNumberEntrySteps(type);
        int a = steps[0];
        int b = steps[1];
        int c = steps[2];
        int d = steps[3];

        int left = parent.getGuiLeft() + x;
        int top = parent.getGuiTop() + y;

        List<Button> buttons = new ArrayList<>(9);

        buttons.add(new Button(left, top, 22, 20, makeLabel(PLUS, a), btn -> addQty(a)));
        buttons.add(new Button(left + 28, top, 28, 20, makeLabel(PLUS, b), btn -> addQty(b)));
        buttons.add(new Button(left + 62, top, 32, 20, makeLabel(PLUS, c), btn -> addQty(c)));
        buttons.add(new Button(left + 100, top, 38, 20, makeLabel(PLUS, d), btn -> addQty(d)));

        // Need to add these now for sensible tab-order
        buttons.forEach(addButton);

        // Placing this here will give a sensible tab order
        addChildren.accept(this.textField);

        buttons.add(new Button(left, top + 42, 22, 20, makeLabel(MINUS, a), btn -> addQty(-a)));
        buttons.add(new Button(left + 28, top + 42, 28, 20, makeLabel(MINUS, b), btn -> addQty(-b)));
        buttons.add(new Button(left + 62, top + 42, 32, 20, makeLabel(MINUS, c), btn -> addQty(-c)));
        buttons.add(new Button(left + 100, top + 42, 38, 20, makeLabel(MINUS, d), btn -> addQty(-d)));

        // This element is not focusable
        if (!hideValidationIcon) {
            this.validationIcon = new ValidationIcon(left + 104, top + 27);
            buttons.add(this.validationIcon);
        }

        // Add the rest to the tab order
        buttons.subList(4, buttons.size()).forEach(addButton);

        this.buttons = buttons;

        // we need to re-validate because the icon may now be present and needs it's
        // initial state
        this.validate();
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

    private void addQty(final long i) {
        getLongValue().ifPresent(currentValue -> setValue(currentValue + i));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.textField.render(matrices, mouseX, mouseY, partialTicks);
    }

    private void validate() {
        List<ITextComponent> validationErrors = new ArrayList<>();

        String text = textField.getValue().trim();
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
        return prefix.plainCopy().append(String.valueOf(amount));
    }

    public void setHideValidationIcon(boolean hideValidationIcon) {
        this.hideValidationIcon = hideValidationIcon;
    }

}
