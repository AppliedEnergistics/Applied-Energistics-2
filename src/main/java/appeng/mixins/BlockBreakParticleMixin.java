package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Replicates Forge's addHitEffects patch.
 */
@Mixin(Minecraft.class)
@SuppressWarnings("ConstantConditions")
public class BlockBreakParticleMixin {

    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;crack(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void customBreakEffect(boolean bl, CallbackInfo ci, BlockHitResult blockHitResult) {
        var self = (Minecraft) (Object) this;
    }

}
