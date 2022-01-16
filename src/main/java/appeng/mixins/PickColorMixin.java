package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import appeng.hooks.ColorApplicatorPickColorHook;

/**
 * This mixin hooks the pick block method on the client to allow a player to easily switch the color of a held color
 * applicator to that of a block or part in the world. pickBlock is also called in survival mode.
 */
@Mixin(Minecraft.class)
public class PickColorMixin {
    @Shadow
    LocalPlayer player;
    @Shadow
    HitResult hitResult;

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    public void pickColor(CallbackInfo ci) {
        if (this.player != null && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
            if (ColorApplicatorPickColorHook.onPickColor(player, (BlockHitResult) this.hitResult)) {
                ci.cancel();
            }
        }
    }
}
