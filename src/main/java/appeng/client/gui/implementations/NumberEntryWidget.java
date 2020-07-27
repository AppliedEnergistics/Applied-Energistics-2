package appeng.client.gui.implementations;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.widgets.ITickingWidget;
import appeng.client.gui.widgets.NumberBox;
import appeng.core.AEConfig;

/**
 * A utility widget that consists of a text-field to enter a number with
 * attached buttons to increment/decrement the number in fixed intervals.
 */
public class NumberEntryWidget implements ITickingWidget {

    private final AEBaseScreen<?> parent;

    private final int x;
    private final int y;

    private final NumberBox level;
    private final NumberEntryType type;
    private ButtonWidget plus1;
    private ButtonWidget plus10;
    private ButtonWidget plus100;
    private ButtonWidget plus1000;
    private ButtonWidget minus1;
    private ButtonWidget minus10;
    private ButtonWidget minus100;
    private ButtonWidget minus1000;

    public NumberEntryWidget(AEBaseScreen<?> parent, int x, int y, int width, int height, NumberEntryType type,
            LongConsumer changeListener) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.type = type;

        TextRenderer font = parent.getClient().textRenderer;
        int inputX = parent.getX() + x;
        int inputY = parent.getY() + y;
        this.level = new NumberBox(font, inputX, inputY, width, font.fontHeight, type.getInputType(), changeListener);
        this.level.setHasBorder(false);
        this.level.setMaxLength(16);
        this.level.setEditableColor(0xFFFFFF);
        this.level.setVisible(true);
        parent.setInitialFocus(this.level);
    }

    public void setActive(boolean active) {
        this.level.active = active;
        this.plus1.active = active;
        this.plus10.active = active;
        this.plus100.active = active;
        this.plus1000.active = active;
        this.minus1.active = active;
        this.minus10.active = active;
        this.minus100.active = active;
        this.minus1000.active = active;
    }

    public void setTextFieldBounds(int x, int y, int width) {
        this.level.x = parent.getX() + x;
        this.level.y = parent.getY() + y;
        this.level.setWidth(width);
    }

    public void setMinValue(long minValue) {
        this.level.setMinValue(minValue);
    }

    public void addButtons(Consumer<Element> addChildren, Consumer<ButtonWidget> addButton) {
        final int[] steps = AEConfig.instance().getNumberEntrySteps(type);
        int a = steps[0];
        int b = steps[1];
        int c = steps[2];
        int d = steps[3];

        int left = parent.getX() + x;
        int top = parent.getY() + y;

        addButton.accept(this.plus1 = new ButtonWidget(left, top, 22, 20, new LiteralText("+" + a), btn -> addQty(a)));
        addButton.accept(
                this.plus10 = new ButtonWidget(left + 28, top, 28, 20, new LiteralText("+" + b), btn -> addQty(b)));
        addButton.accept(
                this.plus100 = new ButtonWidget(left + 62, top, 32, 20, new LiteralText("+" + c), btn -> addQty(c)));
        addButton.accept(
                this.plus1000 = new ButtonWidget(left + 100, top, 38, 20, new LiteralText("+" + d), btn -> addQty(d)));

        // Placing this here will give a sensible tab order
        addChildren.accept(this.level);

        addButton.accept(
                this.minus1 = new ButtonWidget(left, top + 42, 22, 20, new LiteralText("-" + a), btn -> addQty(-a)));
        addButton.accept(this.minus10 = new ButtonWidget(left + 28, top + 42, 28, 20, new LiteralText("-" + b),
                btn -> addQty(-b)));
        addButton.accept(this.minus100 = new ButtonWidget(left + 62, top + 42, 32, 20, new LiteralText("-" + c),
                btn -> addQty(-c)));
        addButton.accept(this.minus1000 = new ButtonWidget(left + 100, top + 42, 38, 20, new LiteralText("-" + d),
                btn -> addQty(-d)));
    }

    private void addQty(final long i) {
        long currentValue = this.level.getValue();
        long minValue = this.level.getMinValue();
        this.level.setText(String.valueOf(Math.max(minValue, currentValue + i)));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.level.render(matrices, mouseX, mouseY, partialTicks);
    }

    public void setValue(long value, boolean skipNotify) {
        this.level.setValue(value, skipNotify);
    }

    public void setValue(long value) {
        setValue(value, false);
    }

    public long getValue() {
        return level.getValue();
    }

    @Override
    public void tick() {
        this.level.tick();
    }

}
