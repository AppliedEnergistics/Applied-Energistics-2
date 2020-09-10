package appeng.client.render;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import appeng.items.misc.FluidDummyItem;

@OnlyIn(Dist.CLIENT)
public class FluidDummyItemColor implements IItemColor {

    @Override
    public int getColor(ItemStack stack, int tintIndex) {

        Item item = stack.getItem();
        if (!(item instanceof FluidDummyItem)) {
            return -1;
        }

        FluidDummyItem fluidItem = (FluidDummyItem) item;
        FluidStack fluidStack = fluidItem.getFluidStack(stack);

        return fluidStack.getFluid().getAttributes().getColor(fluidStack);
    }

}
