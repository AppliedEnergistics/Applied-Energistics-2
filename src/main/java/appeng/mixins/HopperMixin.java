package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.ActionResult;

import appeng.hooks.HopperHooks;

// SEE: https://github.com/AlexIIL/LibBlockAttributes/pull/27
@SuppressWarnings("ConstantConditions")
@Mixin(HopperBlockEntity.class)
public class HopperMixin {

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private void onInsert(CallbackInfoReturnable<Boolean> cri) {
        HopperBlockEntity self = (HopperBlockEntity) (Object) this;
        ActionResult result = HopperHooks.tryInsert(self);
        if (result != ActionResult.PASS) {
            cri.setReturnValue(result.isAccepted());
        }
    }

    @Inject(method = "extract", at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private static void onExtract(Hopper hopper, CallbackInfoReturnable<Boolean> cri) {
        ActionResult result = HopperHooks.tryExtract(hopper);
        if (result != ActionResult.PASS) {
            cri.setReturnValue(result.isAccepted());
        }
    }

}
