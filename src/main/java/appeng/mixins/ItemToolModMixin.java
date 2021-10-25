package appeng.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.items.misc.WrappedFluidStack;

@Mixin(value = Screen.class, priority = 1001)
public class ItemToolModMixin {

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    public void getTooltipFromItem(ItemStack itemStack, CallbackInfoReturnable<List<Component>> cri) {
        if (WrappedFluidStack.isWrapped(itemStack)) {
            WrappedFluidStack.modifyTooltip(itemStack, cri.getReturnValue());
        }
    }

}
