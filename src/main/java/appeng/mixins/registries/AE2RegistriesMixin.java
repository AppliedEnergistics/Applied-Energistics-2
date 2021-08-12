package appeng.mixins.registries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.Bootstrap;

import appeng.init.internal.InitBlockEntityMoveStrategies;
import appeng.init.internal.InitGridServices;
import appeng.init.internal.InitStorageChannels;

/**
 * Very early, but controlled initialization of AE2's internal registries. This allows other mods to freely use them
 * within their mod constructors.
 */
@Mixin(Bootstrap.class)
public abstract class AE2RegistriesMixin {

    private volatile static boolean appeng2_initialized;

    @Inject(at = @At("TAIL"), method = "bootStrap")
    private static void initRegistries(CallbackInfo ci) {
        if (!appeng2_initialized) {
            appeng2_initialized = true;
            InitStorageChannels.init();
            InitGridServices.init();
            InitBlockEntityMoveStrategies.init();
        }
    }

}
