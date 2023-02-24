package appeng.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.core.localization.GuiText;
import appeng.items.storage.StorageCellTooltipComponent;

public class StorageCellClientTooltipComponent implements ClientTooltipComponent {
    private final StorageCellTooltipComponent tooltipComponent;
    private final Component upgradesLabel;

    public StorageCellClientTooltipComponent(StorageCellTooltipComponent tooltipComponent) {
        this.tooltipComponent = tooltipComponent;
        this.upgradesLabel = GuiText.StorageCellTooltipUpgrades.text();
    }

    @Override
    public int getHeight() {
        var height = 0;
        var upgrades = tooltipComponent.upgrades();
        if (!upgrades.isEmpty()) {
            height += 17;
        }

        var content = tooltipComponent.content();
        if (!content.isEmpty()) {
            height += 17;
        }

        return height;
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;

        var content = tooltipComponent.content();
        if (!content.isEmpty()) {
            var filterWidth = content.size() * 17;
            if (tooltipComponent.hasMoreContent()) {
                filterWidth += 10; // ellipsis
            }

            width = Math.max(width, filterWidth);
        }

        var upgrades = tooltipComponent.upgrades();
        if (!upgrades.isEmpty()) {
            var upgradesWidth = font.width(upgradesLabel) + 2 + 17 * upgrades.size();
            width = Math.max(width, upgradesWidth);
        }

        return width;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        var yoff = (16 - font.lineHeight) / 2;

        var content = tooltipComponent.content();
        if (!content.isEmpty()) {
            var xoff = content.size() * 17;
            if (tooltipComponent.hasMoreContent()) {
                font.drawInBatch("\u2026", x + xoff + 2, y + 2, -1, false, matrix4f, bufferSource,
                        Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            }
            y += 17;
        }

        var upgrades = tooltipComponent.upgrades();
        if (!upgrades.isEmpty()) {
            font.drawInBatch(upgradesLabel, x, y + yoff, 0x7E7E7E, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
        poseStack.pushPose();
        poseStack.translate(0, 0, blitOffset);

        var content = tooltipComponent.content();
        if (!content.isEmpty()) {
            var xoff = 0;
            for (var stack : content) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), poseStack, x + xoff, y, stack.what());
                xoff += 17;
            }

            // Now render the amounts on top of the items
            xoff = 0;
            for (var stack : content) {
                var amtText = stack.what().formatAmount(stack.amount(), AmountFormat.SLOT);
                StackSizeRenderer.renderSizeLabel(poseStack, font, x + xoff, y, amtText, false);
                xoff += 17;
            }
            y += 17;
        }

        var upgrades = tooltipComponent.upgrades();
        if (!upgrades.isEmpty()) {
            var xoff = font.width(upgradesLabel) + 2;
            for (ItemStack upgrade : upgrades) {
                itemRenderer.renderGuiItem(poseStack, upgrade, x + xoff, y);
                xoff += 17;
            }
        }

        poseStack.popPose();
    }
}
