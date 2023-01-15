package appeng.client.gui.me.common;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.core.localization.GuiText;

/**
 * A Minecraft toast for a finished crafting job.
 */
public class FinishedJobToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("toast/recipe");

    private static final long TIME_VISIBLE = 2500;
    private static final int TITLE_COLOR = 0xFF500050;
    private static final int TEXT_COLOR = 0xFF000000;

    private final AEKey what;
    private final List<FormattedCharSequence> lines;
    private final int height;

    public FinishedJobToast(AEKey what, long amount) {
        this.what = what;

        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        var formattedAmount = what.formatAmount(amount, AmountFormat.SLOT);

        var text = GuiText.ToastCraftingJobFinishedText.text(formattedAmount, AEKeyRendering.getDisplayName(what));
        lines = font.split(text, width() - 30 - 5);
        height = Toast.super.height() + (lines.size() - 1) * font.lineHeight;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        // stretch the middle
        guiGraphics.blit(BACKGROUND_SPRITE, 0, 0, 0, 32, this.width(), 8);
        int middleHeight = height - 16;
        for (var middleY = 0; middleY < middleHeight; middleY += 16) {
            var tileHeight = Math.min(middleHeight - middleY, 16);
            guiGraphics.blit(BACKGROUND_SPRITE, 0, 8 + middleY, 0, 32 + 8, this.width(), tileHeight);
        }
        guiGraphics.blit(BACKGROUND_SPRITE, 0, height - 8, 0, 32 + 32 - 8, this.width(), 8);
        guiGraphics.drawString(toastComponent.getMinecraft().font, GuiText.ToastCraftingJobFinishedTitle.text(), 30, 7,
                TITLE_COLOR, false);
        var lineY = 18;
        for (var line : lines) {
            guiGraphics.drawString(toastComponent.getMinecraft().font, line, 30, lineY, TEXT_COLOR, false);
            lineY += font.lineHeight;
        }
        AEKeyRendering.drawInGui(minecraft, guiGraphics, 8, 8, what);

        return timeSinceLastVisible >= TIME_VISIBLE ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int height() {
        return height;
    }
}
