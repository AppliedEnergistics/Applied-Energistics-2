package appeng.client.gui.implementations;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.NumberBox;
import appeng.core.AEConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * A utility widget that consists of a text-field to enter a number with attached buttons to
 * increment/decrement the number in fixed intervals.
 */
public class NumberEntryWidget {

    private final AEBaseScreen<?> parent;

    private final int x;
    private final int y;

    private final NumberBox level;
    private Button plus1;
    private Button plus10;
    private Button plus100;
    private Button plus1000;
    private Button minus1;
    private Button minus10;
    private Button minus100;
    private Button minus1000;

    public NumberEntryWidget(AEBaseScreen<?> parent, int x, int y, int width, LongConsumer changeListener) {
        this.parent = parent;
        this.x = x;
        this.y = y;

        FontRenderer font = parent.getMinecraft().fontRenderer;
        this.level = new NumberBox(font, parent.getGuiLeft() + x + 5, parent.getGuiTop() + y + 27, width, font.FONT_HEIGHT,
                Long.class, changeListener);
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

    public void addButtons(Consumer<IGuiEventListener> addChildren, Consumer<Button> addButton) {
        final int a = AEConfig.instance().levelByStackAmounts(0);
        final int b = AEConfig.instance().levelByStackAmounts(1);
        final int c = AEConfig.instance().levelByStackAmounts(2);
        final int d = AEConfig.instance().levelByStackAmounts(3);

        int left = parent.getGuiLeft() + x;
        int top = parent.getGuiTop() + y;

        addButton.accept(this.plus1 = new Button(left, top, 22, 20, "+" + a, btn -> addQty(a)));
        addButton.accept(
                this.plus10 = new Button(left + 28, top, 28, 20, "+" + b, btn -> addQty(b)));
        addButton.accept(
                this.plus100 = new Button(left + 62, top, 32, 20, "+" + c, btn -> addQty(c)));
        addButton.accept(
                this.plus1000 = new Button(left + 100, top, 38, 20, "+" + d, btn -> addQty(d)));

        // Placing this here will give a sensible tab order
        addChildren.accept(this.level);

        addButton.accept(
                this.minus1 = new Button(left, top + 42, 22, 20, "-" + a, btn -> addQty(-a)));
        addButton.accept(
                this.minus10 = new Button(left + 28, top + 42, 28, 20, "-" + b, btn -> addQty(-b)));
        addButton.accept(
                this.minus100 = new Button(left + 62, top + 42, 32, 20, "-" + c, btn -> addQty(-c)));
        addButton.accept(
                this.minus1000 = new Button(left + 100, top + 42, 38, 20, "-" + d, btn -> addQty(-d)));
    }

    private void addQty(final long i) {
        long currentValue = this.level.getValue();
        this.level.setText(String.valueOf(Math.max(0, currentValue + i)));
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        this.level.render(mouseX, mouseY, partialTicks);
    }

    public void setValue(long value) {
        // This check avoid changing the cursor position needlessly
        if (value != this.level.getValue()) {
            this.level.setText(String.valueOf(value));
        }
    }

    public void tick() {
        this.level.tick();
    }
}
