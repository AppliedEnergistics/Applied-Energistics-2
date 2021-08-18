package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.Bootstrap;

import appeng.core.AppEngBootstrap;

/**
 * Very early, but controlled initialization of AE2's internal registries. This allows other mods to freely use them
 * within their mod constructors.
 */
@Mixin(Bootstrap.class)
public abstract class EarlyStartupMixin {

    @Inject(at = @At("TAIL"), method = "bootStrap")
    private static void initRegistries(CallbackInfo ci) {
        AppEngBootstrap.runEarlyStartup();
    }

}
