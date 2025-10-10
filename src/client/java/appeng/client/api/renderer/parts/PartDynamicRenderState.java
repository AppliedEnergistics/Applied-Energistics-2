package appeng.client.api.renderer.parts;

import net.minecraft.core.Direction;

/**
 * The base class for part-specific render state used for dynamically rendering parts.
 * <p>
 * Note that this is not used for static part models.
 *
 * @see PartRenderer
 */
public class PartDynamicRenderState {
    public PartRenderer<?, ?> renderer;
    /**
     * @see net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState#lightCoords
     */
    public int lightCoords;
    /**
     * The side the part is attached to.
     */
    public Direction side;
}
