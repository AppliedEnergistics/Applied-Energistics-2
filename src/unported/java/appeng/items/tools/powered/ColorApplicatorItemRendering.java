package appeng.items.tools.powered;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;

public class ColorApplicatorItemRendering extends ItemRenderingCustomizer {

    @Override
    @Environment(EnvType.CLIENT)
    public void customize(IItemRendering rendering) {
        rendering.color(this::getColor);
    }

    private int getColor(ItemStack itemStack, int idx) {
        if (idx == 0) {
            return -1;
        }

        final AEColor col = ((ColorApplicatorItem) itemStack.getItem()).getActiveColor(itemStack);

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
