package appeng.mixins;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import appeng.client.gui.AEBaseScreen;

/**
 * Allows custom highlight for slots in {@link AEBaseScreen}.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ResizableSlotHighlightMixin {
    @Inject(method = "renderSlotHighlight", at = @At("HEAD"), cancellable = true)
    private static void renderResizableSlotHighlight(PoseStack poseStack, int x, int y, int z, CallbackInfo ci) {
        var minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        var screen = minecraft.screen;
        if (screen instanceof AEBaseScreen<?>self) {
            self.renderCustomSlotHighlight(poseStack, x, y, z);
            ci.cancel();
        }
    }
}
