package appeng.bootstrap.components;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

/**
 * Sets the rendering type for a block.
 */
public class RenderTypeComponent implements IClientSetupComponent {

    private final Block block;

    private final RenderType renderType;

    public RenderTypeComponent(Block block, RenderType renderType) {
        this.block = block;
        this.renderType = renderType;
    }

    @Override
    public void setup() {
        RenderTypeLookup.setRenderLayer(block, renderType);
    }

}
