package appeng.init.client;

import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.features.ItemDefinition;
import appeng.fluids.items.FluidDummyItem;
import appeng.items.misc.PaintBallItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.tools.powered.ColorApplicatorItem;

public final class InitItemColors {
    private InitItemColors() {
    }

    public static void init(ItemColors itemColors) {
        itemColors.register(new StaticItemColor(AEColor.TRANSPARENT), ApiBlocks.securityStation.blockItem());
        // I checked, the ME chest doesn't keep its color in item form
        itemColors.register(new StaticItemColor(AEColor.TRANSPARENT), ApiBlocks.chest.blockItem());

        itemColors.register(InitItemColors::getColorApplicatorColor, ApiItems.COLOR_APPLICATOR);

        itemColors.register(InitItemColors::getDummyFluidItemColor, ApiItems.DUMMY_FLUID_ITEM);

        // Automatically register colors for certain items we register
        for (ItemDefinition definition : ApiItems.getItems()) {
            Item item = definition.item();
            if (item instanceof PartItem) {
                AEColor color = AEColor.TRANSPARENT;
                if (item instanceof ColoredPartItem) {
                    color = ((ColoredPartItem<?>) item).getColor();
                }
                itemColors.register(new StaticItemColor(color), item);
            } else if (item instanceof PaintBallItem) {
                registerPaintBall(itemColors, (PaintBallItem) item);
            }
        }
    }

    /**
     * We use a white base item icon for paint balls. This applies the correct color to it.
     */
    private static void registerPaintBall(ItemColors colors, PaintBallItem item) {
        AEColor color = item.getColor();
        final int colorValue = item.isLumen() ? color.mediumVariant : color.mediumVariant;
        final int r = colorValue >> 16 & 0xff;
        final int g = colorValue >> 8 & 0xff;
        final int b = colorValue & 0xff;

        int renderColor;
        if (item.isLumen()) {
            final float fail = 0.7f;
            final int full = (int) (255 * 0.3);
            renderColor = (int) (full + r * fail) << 16 | (int) (full + g * fail) << 8 | (int) (full + b * fail)
                    | 0xff << 24;
        } else {
            renderColor = r << 16 | g << 8 | b | 0xff << 24;
        }

        colors.register((is, tintIndex) -> renderColor, item);
    }

    private static int getColorApplicatorColor(ItemStack itemStack, int idx) {
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

    private static int getDummyFluidItemColor(ItemStack stack, int tintIndex) {

        Item item = stack.getItem();
        if (!(item instanceof FluidDummyItem)) {
            return -1;
        }

        FluidDummyItem fluidItem = (FluidDummyItem) item;
        FluidStack fluidStack = fluidItem.getFluidStack(stack);

        return fluidStack.getFluid().getAttributes().getColor(fluidStack);
    }

}
