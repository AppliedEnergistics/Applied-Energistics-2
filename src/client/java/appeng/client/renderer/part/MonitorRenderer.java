package appeng.client.renderer.part;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;

import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AmountFormat;
import appeng.client.api.renderer.parts.PartRenderer;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.parts.reporting.AbstractMonitorPart;

public class MonitorRenderer implements PartRenderer<AbstractMonitorPart, MonitorRenderState> {
    private final Font font;

    public MonitorRenderer() {
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public MonitorRenderState createState() {
        return new MonitorRenderState();
    }

    @Override
    public Class<MonitorRenderState> stateClass() {
        return MonitorRenderState.class;
    }

    @Override
    public void extract(AbstractMonitorPart part, MonitorRenderState state, float partialTicks) {
        if (part.isActive()) {
            state.orientation = BlockOrientation.get(part.getSide(), part.getSpin());
            state.textColor = part.getColor().contrastTextColor;
            state.textColor |= 0xFF000000; // ensure full visibility
            var displayed = part.getDisplayed();
            if (displayed != null) {
                int seed = (int) part.getHost().getBlockEntity().getBlockPos().asLong();
                state.what.extract(displayed, part.getLevel(), seed);

                var canCraft = part.canCraft();
                var amount = part.getAmount();
                Component displayText;
                if (amount == 0 && canCraft) {
                    displayText = Component.literal("Craft");
                } else {
                    displayText = Component.literal(displayed.formatAmount(amount, AmountFormat.SLOT));
                }
                state.text = displayText.getVisualOrderText();
                state.textWidth = font.width(state.text);
            } else {
                state.what.clear();
                state.text = null;
                state.textWidth = 0;
            }
        }
    }

    @Override
    public void submit(MonitorRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        if (state.what.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5); // Move to the center of the block
        BlockEntityRenderHelper.rotateToFace(poseStack, state.orientation);
        // Move to the "front" of the face.
        poseStack.translate(0, 0, 0.5);

        float itemScale = 6 / 16f;

        BlockEntityRenderHelper.submitItem2dWithAmount(
                poseStack,
                state.what,
                state.text,
                state.textColor,
                state.textWidth,
                nodes,
                itemScale);

        poseStack.popPose();
    }
}
