package appeng.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;

import appeng.core.AEConfig;
import appeng.parts.automation.PlaneSoundVolume;
import appeng.util.Platform;

/**
 * Scales the placement sound volume of blocks placed by a Formation Plane (i.e. by one of AE2's own fake players), per
 * the {@code planeVolumeMultiplier} config option.
 */
@Mixin(BlockItem.class)
public abstract class FormationPlaneVolumeMixin {

    @ModifyExpressionValue(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getVolume()F"))
    private float scalePlaneVolume(float originalVolume, @Local Player player) {
        if (Platform.isAe2FakePlayer(player)) {
            return PlaneSoundVolume.scale(originalVolume, (float) AEConfig.instance().getPlaneVolumeMultiplier());
        }
        return originalVolume;
    }

}
