package appeng.client.guidebook.document.block;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;

public class LytRecipeFor extends LytBlock {
    private ResourceLocation itemId;

    public LytRecipeFor(ResourceLocation itemId) {
        this.itemId = itemId;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return LytRect.empty();
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {

    }

    @Override
    public void render(RenderContext context) {

    }
}
