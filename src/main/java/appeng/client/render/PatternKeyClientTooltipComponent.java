package appeng.client.render;

import org.joml.Matrix4f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.core.localization.GuiText;
import appeng.crafting.pattern.PatternKeyTooltipComponent;

public class PatternKeyClientTooltipComponent implements ClientTooltipComponent {
    private final PatternKeyTooltipComponent tooltipComponent;
    private static final int TEXT_HEIGHT = 10;
    private static final int LINE_HEIGHT = 16;
    private static final int TEXT_PADDING = 20;
    private static final int TEXT_VERTICAL_OFFSET = 4;
    private static final int SECTION_SPACING = 12;
    private static final int AUTHOR_VERTICAL_OFFSET = 6;
    private static final int TOOLTIP_PADDING = 6;
    private static final int INITIAL_IMAGE_OFFSET = 8;

    public PatternKeyClientTooltipComponent(PatternKeyTooltipComponent tooltipComponent) {
        this.tooltipComponent = tooltipComponent;
    }

    @Override
    public int getHeight() {
        int height = 0;

        if (!tooltipComponent.outputs().isEmpty()) {
            height += TEXT_HEIGHT;
            height += tooltipComponent.outputs().size() * LINE_HEIGHT;
        }

        if (!tooltipComponent.inputs().isEmpty()) {
            height += TEXT_HEIGHT;
            height += tooltipComponent.inputs().size() * LINE_HEIGHT;
        }

        if (tooltipComponent.isCrafting()) {
            height += TEXT_HEIGHT;
            height += TEXT_HEIGHT;
        }
        if (!tooltipComponent.author().isEmpty()) {
            height += TEXT_HEIGHT;
        }

        return height + TOOLTIP_PADDING;
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;

        if (!tooltipComponent.outputs().isEmpty()) {
            var outputsLabel = (tooltipComponent.isCrafting() ? GuiText.Crafts.text() : GuiText.Produces.text())
                    .withStyle(ChatFormatting.DARK_AQUA);
            width = Math.max(width, font.width(outputsLabel) + TEXT_PADDING);

            for (var output : tooltipComponent.outputs()) {
                var text = getStackText(output, false);
                width = Math.max(width, font.width(text) + TEXT_PADDING);
            }
        }

        if (!tooltipComponent.inputs().isEmpty()) {
            var inputsLabel = GuiText.Ingredients.text().withStyle(ChatFormatting.DARK_GREEN);
            width = Math.max(width, font.width(inputsLabel) + TEXT_PADDING);

            for (var input : tooltipComponent.inputs()) {
                var text = getStackText(input, true);
                width = Math.max(width, font.width(text) + TEXT_PADDING);
            }
        }

        if (tooltipComponent.isCrafting()) {
            var substitutionLabel = GuiText.Substitute.text(tooltipComponent.canSubstitute());
            var fluidSubstitutionLabel = GuiText.FluidSubstitutions.text(tooltipComponent.canSubstituteFluids());
            width = Math.max(width, font.width(substitutionLabel));
            width = Math.max(width, font.width(fluidSubstitutionLabel));
        }
        return width;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        int currentY = y;

        var outputs = tooltipComponent.outputs();
        if (!outputs.isEmpty()) {
            var outputsLabel = (tooltipComponent.isCrafting() ? GuiText.Crafts.text() : GuiText.Produces.text())
                    .withStyle(ChatFormatting.DARK_AQUA);
            font.drawInBatch(outputsLabel, x, currentY, -1, false, matrix4f, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0, 0);
            currentY += TEXT_HEIGHT;

            for (var output : outputs) {
                var text = getStackText(output, false);
                font.drawInBatch(text, x + TEXT_PADDING, currentY + TEXT_VERTICAL_OFFSET, -1, false, matrix4f,
                        bufferSource,
                        Font.DisplayMode.SEE_THROUGH, 0, 0);
                if (outputs.indexOf(output) < outputs.size() - 1)
                    currentY += LINE_HEIGHT;
                else
                    currentY += 14;
            }
        }

        var inputs = tooltipComponent.inputs();
        if (!inputs.isEmpty()) {
            var inputsLabel = GuiText.Ingredients.text().withStyle(ChatFormatting.DARK_GREEN);
            font.drawInBatch(inputsLabel, x, currentY + TEXT_VERTICAL_OFFSET, -1, false, matrix4f, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0, 0);
            currentY += LINE_HEIGHT;

            for (var input : inputs) {
                var text = getStackText(input, true);
                font.drawInBatch(text, x + TEXT_PADDING, currentY + TEXT_VERTICAL_OFFSET, -1, false, matrix4f,
                        bufferSource,
                        Font.DisplayMode.SEE_THROUGH, 0, 0);
                if (inputs.indexOf(input) < inputs.size() - 1)
                    currentY += LINE_HEIGHT;
                else
                    currentY += SECTION_SPACING;
            }
        }

        if (tooltipComponent.isCrafting()) {
            var yes = GuiText.Yes.text().withStyle(ChatFormatting.GREEN);
            var no = GuiText.No.text().withStyle(ChatFormatting.RED);
            var canSubstitute = tooltipComponent.canSubstitute() ? yes : no;
            var canSubstituteFluids = tooltipComponent.canSubstituteFluids() ? yes : no;

            var substitutionLabel = GuiText.Substitute.text(canSubstitute);
            var fluidSubstitutionLabel = GuiText.FluidSubstitutions.text(canSubstituteFluids);

            font.drawInBatch(substitutionLabel, x, currentY + TEXT_VERTICAL_OFFSET, -1, false, matrix4f, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0, 0);
            currentY += TEXT_HEIGHT;

            font.drawInBatch(fluidSubstitutionLabel, x, currentY + TEXT_VERTICAL_OFFSET, -1, false, matrix4f,
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0, 0);
            currentY += TEXT_HEIGHT;
        }
        if (!tooltipComponent.author().isEmpty()) {
            var authorText = GuiText.EncodedBy.text(tooltipComponent.author()).withStyle(ChatFormatting.LIGHT_PURPLE);
            font.drawInBatch(authorText, x, currentY + AUTHOR_VERTICAL_OFFSET, -1, false, matrix4f, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0, 0);
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int currentY = y;

        currentY += INITIAL_IMAGE_OFFSET;
        var showAmounts = tooltipComponent.showAmounts();

        if (!tooltipComponent.outputs().isEmpty()) {
            for (var output : tooltipComponent.outputs()) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, x, currentY, output.what());
                if (showAmounts) {
                    var amtText = output.what().formatAmount(output.amount(), AmountFormat.SLOT);
                    StackSizeRenderer.renderSizeLabel(guiGraphics, font, x, currentY, amtText, false);
                }
                currentY += LINE_HEIGHT;
            }
        }

        currentY += 2;

        if (!tooltipComponent.inputs().isEmpty()) {
            currentY += SECTION_SPACING;

            for (var input : tooltipComponent.inputs()) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, x, currentY, input.what());
                if (showAmounts) {
                    var amtText = input.what().formatAmount(input.amount(), AmountFormat.SLOT);
                    StackSizeRenderer.renderSizeLabel(guiGraphics, font, x, currentY, amtText, false);
                }
                currentY += LINE_HEIGHT;
            }
        }
    }

    private Component getStackText(GenericStack stack, boolean isInput) {
        var type = stack.what().getType();
        var what = stack.what();
        var displayName = what.getDisplayName().plainCopy();
        if (type == appeng.api.stacks.AEKeyType.items()) {
            displayName = displayName.withStyle(isInput ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
        } else {
            displayName = displayName.withStyle(isInput ? ChatFormatting.AQUA : ChatFormatting.BLUE);
        }
        var amountInfo = what.formatAmount(stack.amount(), AmountFormat.FULL);
        return Component.literal(amountInfo + " §7x ").append(displayName);
    }
}
