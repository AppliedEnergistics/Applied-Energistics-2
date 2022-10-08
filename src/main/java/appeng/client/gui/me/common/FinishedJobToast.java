package appeng.client.gui.me.common;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FormattedCharSequence;

import appeng.api.client.AEStackRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.core.localization.GuiText;

/**
 * A Minecraft toast for a finished crafting job.
 */
public class FinishedJobToast implements Toast {
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

        var formattedAmount = what.formatAmount(amount, AmountFormat.PREVIEW_REGULAR);

        var text = GuiText.ToastCraftingJobFinishedText.text(formattedAmount, AEStackRendering.getDisplayName(what));
        lines = font.split(text, width() - 30 - 5);
        height = Toast.super.height() + (lines.size() - 1) * font.lineHeight;
    }

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long timeSinceLastVisible) {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // stretch the middle
        toastComponent.blit(poseStack, 0, 0, 0, 32, this.width(), 8);
        int middleHeight = height - 16;
        for (var middleY = 0; middleY < middleHeight; middleY += 16) {
            var tileHeight = Math.min(middleHeight - middleY, 16);
            toastComponent.blit(poseStack, 0, 8 + middleY, 0, 32 + 8, this.width(), tileHeight);
        }
        toastComponent.blit(poseStack, 0, height - 8, 0, 32 + 32 - 8, this.width(), 8);
        toastComponent.getMinecraft().font.draw(poseStack, GuiText.ToastCraftingJobFinishedTitle.text(), 30.0F, 7.0F,
                TITLE_COLOR);
        var lineY = 18;
        for (var line : lines) {
            toastComponent.getMinecraft().font.draw(poseStack, line, 30.0F, lineY, TEXT_COLOR);
            lineY += font.lineHeight;
        }
        AEStackRendering.drawInGui(minecraft, poseStack, 8, 8, 0, what);

        return timeSinceLastVisible >= TIME_VISIBLE ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public int height() {
        return height;
    }
}
