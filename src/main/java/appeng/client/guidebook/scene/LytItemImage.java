package appeng.client.guidebook.scene;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class LytItemImage extends LytBlock {
    private ItemStack item = ItemStack.EMPTY;

    private float scale = 1;

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, Mth.ceil(16 * scale), Mth.ceil(16 * scale));
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(RenderContext context) {
        context.renderItem(item, bounds.x(), bounds.y(), 16 * scale, 16 * scale);
    }
}
