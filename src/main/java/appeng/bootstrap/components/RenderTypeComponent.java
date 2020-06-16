package appeng.bootstrap.components;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

/**
 * Sets the rendering type for a block.
 */
public class RenderTypeComponent implements IClientSetupComponent {

    private final Block block;

    private final RenderType renderType;

    private final Predicate<RenderType> renderTypes;

    public RenderTypeComponent(Block block, RenderType renderType, Predicate<RenderType> renderTypes) {
        this.block = block;
        this.renderType = renderType;
        this.renderTypes = renderTypes;
    }

    @Override
    public void setup() {
        if (renderType != null) {
            RenderTypeLookup.setRenderLayer(block, renderType);
        }

        if (renderTypes != null) {
            RenderTypeLookup.setRenderLayer(block, renderTypes);
        }
    }

}
