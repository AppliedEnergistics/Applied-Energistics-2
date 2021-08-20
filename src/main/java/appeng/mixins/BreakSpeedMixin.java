package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import appeng.hooks.SkyStoneBreakSpeed;

@Mixin(value = Player.class)
public class BreakSpeedMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    public void modifyBreakSpeed(BlockState blockState, CallbackInfoReturnable<Float> cri) {
        var self = (Player) (Object) this;
        var result = SkyStoneBreakSpeed.handleBreakFaster(self, blockState, cri.getReturnValue());
        if (result != null) {
            cri.setReturnValue(result);
        }
    }

}
