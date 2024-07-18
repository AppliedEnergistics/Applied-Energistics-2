package appeng.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.GenericStack;

@Mixin(value = Screen.class, priority = 1001)
public class WrappedGenericStackTooltipModIdMixin {

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    private static void getTooltipFromItem(Minecraft client, ItemStack itemStack,
            CallbackInfoReturnable<List<Component>> cri) {
        var unwrapped = GenericStack.unwrapItemStack(itemStack);
        if (unwrapped != null) {
            cri.setReturnValue(AEKeyRendering.getTooltip(unwrapped.what()));
        }
    }

}
