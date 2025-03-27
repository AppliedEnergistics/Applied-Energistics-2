package appeng.client.api.renderer.parts;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IPart;

/**
 * Allows dynamic rendering of geometry for parts to attached to a cable bus.
 *
 * @param <T> The type of part that this renderer supports.
 */
public interface PartRenderer<T extends IPart> {
    /**
     * Render dynamic portions of a part attached to a cable bus.
     */
    void renderDynamic(T part,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            Vec3 cameraPosition);
}
