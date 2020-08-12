package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.Bootstrap;

import appeng.core.AppEngBootstrap;

@Mixin(Bootstrap.class)
public class BootstrapMixin {

    @Inject(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/Bootstrap;setOutputStreams()V", shift = At.Shift.AFTER), require = 1, allow = 1)
    private static void afterInitialize(CallbackInfo ci) {
        AppEngBootstrap.initialize();
    }

}
