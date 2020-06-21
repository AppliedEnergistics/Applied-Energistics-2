package appeng.items.tools.powered;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.core.AppEng;

public class ToolColorApplicatorRendering extends ItemRenderingCustomizer {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void customize(IItemRendering rendering) {
        rendering.color(this::getColor);
    }

    private int getColor(ItemStack itemStack, int idx) {
        if (idx == 0) {
            return -1;
        }

        final AEColor col = ((ToolColorApplicator) itemStack.getItem()).getActiveColor(itemStack);

        if (col == null) {
            return -1;
        }

        switch (idx) {
            case 1:
                return col.blackVariant;
            case 2:
                return col.mediumVariant;
            case 3:
                return col.whiteVariant;
            default:
                return -1;
        }
    }
}
