package appeng.client.gui.implementations;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;

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
    private Button plus1;
    private Button plus10;
    private Button plus100;
    private Button plus1000;
    private Button minus1;
    private Button minus10;
    private Button minus100;
    private Button minus1000;

    public NumberEntryWidget(AEBaseScreen<?> parent, int x, int y, int width, int height, NumberEntryType type,
            LongConsumer changeListener) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.type = type;

        FontRenderer font = parent.getMinecraft().fontRenderer;
        int inputX = parent.getGuiLeft() + x;
        int inputY = parent.getGuiTop() + y;
        this.level = new NumberBox(font, inputX, inputY, width, font.FONT_HEIGHT, type.getInputType(), changeListener);
        this.level.setEnableBackgroundDrawing(false);
        this.level.setMaxStringLength(16);
        this.level.setTextColor(0xFFFFFF);
        this.level.setVisible(true);
        this.level.setFocused2(true);
        parent.setFocusedDefault(this.level);
    }

    public void setActive(boolean active) {
        this.level.setEnabled(active);
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
        this.level.x = parent.getGuiLeft() + x;
        this.level.y = parent.getGuiTop() + y;
        this.level.setWidth(width);
    }

    public void setMinValue(long minValue) {
        this.level.setMinValue(minValue);
    }

    public void addButtons(Consumer<IGuiEventListener> addChildren, Consumer<Button> addButton) {
        final int[] steps = AEConfig.instance().getNumberEntrySteps(type);
        int a = steps[0];
        int b = steps[1];
        int c = steps[2];
        int d = steps[3];

        int left = parent.getGuiLeft() + x;
        int top = parent.getGuiTop() + y;

        addButton.accept(this.plus1 = new Button(left, top, 22, 20, "+" + a, btn -> addQty(a)));
        addButton.accept(this.plus10 = new Button(left + 28, top, 28, 20, "+" + b, btn -> addQty(b)));
        addButton.accept(this.plus100 = new Button(left + 62, top, 32, 20, "+" + c, btn -> addQty(c)));
        addButton.accept(this.plus1000 = new Button(left + 100, top, 38, 20, "+" + d, btn -> addQty(d)));

        // Placing this here will give a sensible tab order
        addChildren.accept(this.level);

        addButton.accept(this.minus1 = new Button(left, top + 42, 22, 20, "-" + a, btn -> addQty(-a)));
        addButton.accept(this.minus10 = new Button(left + 28, top + 42, 28, 20, "-" + b, btn -> addQty(-b)));
        addButton.accept(this.minus100 = new Button(left + 62, top + 42, 32, 20, "-" + c, btn -> addQty(-c)));
        addButton.accept(this.minus1000 = new Button(left + 100, top + 42, 38, 20, "-" + d, btn -> addQty(-d)));
    }

    private void addQty(final long i) {
        long currentValue = this.level.getValue();
        long minValue = this.level.getMinValue();
        this.level.setText(String.valueOf(Math.max(minValue, currentValue + i)));
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        this.level.render(mouseX, mouseY, partialTicks);
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
