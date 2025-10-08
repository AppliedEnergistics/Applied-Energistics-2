package appeng.client.renderer.part;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;

import appeng.api.orientation.BlockOrientation;
import appeng.client.api.renderer.parts.PartRenderer;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.parts.reporting.AbstractMonitorPart;

public class MonitorRenderer implements PartRenderer<AbstractMonitorPart, MonitorRenderState> {
    @Override
    public MonitorRenderState createRenderState() {
        return new MonitorRenderState();
    }

    @Override
    public void extractRenderState(AbstractMonitorPart part, MonitorRenderState state, float partialTicks) {
        state.item.clear();

        if (!part.isActive()) {
            return;
        }

        var configuredItem = part.getDisplayed();
        if (configuredItem == null) {
            return;
        }

        state.orientation = BlockOrientation.get(part.getSide(), part.getSpin());
        state.canCraft = part.canCraft();
        state.amount = part.getAmount();
        state.textColor = part.getColor().contrastTextColor;
    }

    @Override
    public void submit(MonitorRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5); // Move into the center of the block
        BlockEntityRenderHelper.rotateToFace(poseStack, state.orientation);
        poseStack.translate(0, 0.05, 0.5);

        // TODO 1.21.9 lockEntityRenderHelper.renderItem2dWithAmount(poseStack, buffers, configuredItem, state.amount,
        // TODO 1.21.9 state.canCraft,
        // TODO 1.21.9 0.4f, -0.23f, state.textColor, part.getLevel());

        poseStack.popPose();
    }
}
