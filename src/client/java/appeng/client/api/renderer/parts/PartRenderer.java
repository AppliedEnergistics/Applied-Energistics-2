package appeng.client.api.renderer.parts;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;

import appeng.api.parts.IPart;

/**
 * Allows dynamic rendering of geometry for parts to attached to a cable bus.
 *
 * @param <T> The type of part that this renderer supports.
 */
public interface PartRenderer<T extends IPart, S extends PartDynamicRenderState> {
    /**
     * {@return a new uninitialized instance of the state used by this renderer.}
     */
    S createState();

    Class<S> stateClass();

    /**
     * Extracts the state needed to later render the dynamic parts of the part.
     * 
     * @see net.minecraft.client.renderer.blockentity.BlockEntityRenderer#extractRenderState
     */
    void extract(T part, S state, float partialTicks);

    /**
     * Render dynamic portions of a part attached to a cable bus.
     * 
     * @see net.minecraft.client.renderer.blockentity.BlockEntityRenderer#submit
     */
    void submit(S state, PoseStack poseStack, SubmitNodeCollector nodes, CameraRenderState cameraRenderState);
}
