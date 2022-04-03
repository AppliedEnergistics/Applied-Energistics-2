package appeng.mixins;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.menu.slot.AppEngSlot;

/**
 * Ensure we render {@link AppEngSlot#getDisplayStack()} instead of {@link AppEngSlot#getItem()}.
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @ModifyVariable(method = "renderSlot", index = 5, at = @At(value = "STORE", ordinal = 0))
    protected ItemStack ae2_changeStackForDisplay(ItemStack stack, PoseStack poseStack, Slot slot) {
        if (slot instanceof AppEngSlot aeSlot) {
            return aeSlot.getDisplayStack();
        }
        return stack;
    }
}
