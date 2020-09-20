package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

/**
 * Displays a small icon that shows validation errors for some input control.
 */
public class ValidationIcon extends IconButton {

    private final List<ITextComponent> tooltip = new ArrayList<>();

    private boolean valid;

    public ValidationIcon(int x, int y) {
        super(x, y, btn -> {
        });
        setDisableBackground(true);
        setDisableClickSound(true);
        setHalfSize(true);
    }

    public void setValid(boolean valid) {
        setVisibility(!valid);
        if (valid) {
            this.tooltip.clear();
        }
    }

    public void setTooltip(List<ITextComponent> lines) {
        this.tooltip.clear();
        this.tooltip.addAll(lines);
    }

    @Override
    protected int getIconIndex() {
        return 16 * 8;
    }

    @Override
    public boolean changeFocus(boolean flag) {
        return false; // Cannot focus this element
    }

    @Override
    public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
        if (this.tooltip.isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Screen screen = client.currentScreen;
        if (screen == null) {
            return;
        }

        screen.func_243308_b(matrices, this.tooltip, x, y);
    }
}
