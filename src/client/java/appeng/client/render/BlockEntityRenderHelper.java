package appeng.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Quaternionf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

import appeng.api.orientation.BlockOrientation;
import appeng.client.api.AEKeyRenderState;

/**
 * Helper methods for rendering block entities.
 */
public final class BlockEntityRenderHelper {
    private BlockEntityRenderHelper() {
    }

    private static final Quaternionf ROTATE_TO_FRONT;

    static {
        ROTATE_TO_FRONT = new Quaternionf().rotationY(Mth.DEG_TO_RAD * 180);
    }

    /**
     * Rotate the current coordinate system, so it is on the face of the given block side. This can be used to render on
     * the given face as if it was a 2D canvas, where x+ is facing right and y+ is facing up.
     */
    public static void rotateToFace(PoseStack stack, BlockOrientation orientation) {
        stack.mulPose(orientation.getQuaternion());
        stack.mulPose(ROTATE_TO_FRONT);
    }

    /**
     * Render an item in 2D.
     */
    public static void submitRenderItem2d(
            PoseStack poseStack,
            AEKeyRenderState what,
            float scale,
            SubmitNodeCollector nodes) {
        poseStack.pushPose();
        poseStack.translate(0, .5f / 16f, 0);
        poseStack.scale(scale, scale, scale);
        what.submit(poseStack, nodes, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }

    /**
     * Render an item in 2D and the given text below it.
     */
    public static void submitItem2dWithAmount(
            PoseStack poseStack,
            AEKeyRenderState what,
            FormattedCharSequence text,
            int textColor,
            int textWidth,
            SubmitNodeCollector nodes,
            float itemScale) {

        if (!what.isEmpty()) {
            submitRenderItem2d(poseStack, what, itemScale, nodes);

            if (text != null) {
                // Try rendering the item count below the item, with a small spacing
                double spacing = -(itemScale / 2) - 0.25 / 16f;

                poseStack.pushPose();
                poseStack.translate(0.0f, spacing, 0.02f);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.scale(1.0f / 62.0f, -1.0f / 62.0f, 1.0f / 62.0f);
                poseStack.translate(-0.5f * textWidth, -0.5f * Minecraft.getInstance().font.lineHeight, 0.5f);
                nodes.submitText(poseStack, 0, 0, text, false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT,
                        textColor, 0, 0);
                poseStack.popPose();
            }
        }

    }
}
