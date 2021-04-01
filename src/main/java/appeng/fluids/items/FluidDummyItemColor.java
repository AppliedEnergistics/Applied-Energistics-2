package appeng.fluids.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

@Environment(EnvType.CLIENT)
public class FluidDummyItemColor implements IItemColor {

    @Override
    public int getColor(ItemStack stack, int tintIndex) {

        Item item = stack.getItem();
        if (!(item instanceof FluidDummyItem)) {
            return -1;
        }

        FluidDummyItem fluidItem = (FluidDummyItem) item;
        FluidVolume fluidStack = fluidItem.getFluidStack(stack);

        return fluidStack.getRenderColor();
    }

}
