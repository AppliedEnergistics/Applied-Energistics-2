package appeng.client.render.cablebus;

import net.minecraft.block.BlockState;

/**
 * Captures the state required to render a facade properly.
 */
public class FacadeRenderState {

    // The block state to use for rendering this facade
    private final BlockState sourceBlock;

    private final boolean transparent;

    public FacadeRenderState(BlockState sourceBlock, boolean transparent) {
        this.sourceBlock = sourceBlock;
        this.transparent = transparent;
    }

    public BlockState getSourceBlock() {
        return this.sourceBlock;
    }

    public boolean isTransparent() {
        return this.transparent;
    }
}
