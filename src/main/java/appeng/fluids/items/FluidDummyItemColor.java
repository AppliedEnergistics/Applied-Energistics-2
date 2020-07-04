package appeng.fluids.items;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


@Environment(EnvType.CLIENT)
public class FluidDummyItemColor implements ItemColorProvider {

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
