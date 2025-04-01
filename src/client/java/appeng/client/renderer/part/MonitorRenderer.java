package appeng.client.renderer.part;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import appeng.api.orientation.BlockOrientation;
import appeng.client.api.renderer.parts.PartRenderer;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.parts.reporting.AbstractMonitorPart;

public class MonitorRenderer implements PartRenderer<AbstractMonitorPart> {
    @Override
    public void renderDynamic(AbstractMonitorPart part, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay, Vec3 cameraPosition) {
        if (!part.isActive()) {
            return;
        }

        var configuredItem = part.getDisplayed();
        if (configuredItem == null) {
            return;
        }

        poseStack.pushPose();

        var orientation = BlockOrientation.get(part.getSide(), part.getSpin());

        poseStack.translate(0.5, 0.5, 0.5); // Move into the center of the block
        BlockEntityRenderHelper.rotateToFace(poseStack, orientation);
        poseStack.translate(0, 0.05, 0.5);

        BlockEntityRenderHelper.renderItem2dWithAmount(poseStack, buffers, configuredItem, part.getAmount(),
                part.canCraft(),
                0.4f, -0.23f, part.getColor().contrastTextColor, part.getLevel());

        poseStack.popPose();
    }
}
