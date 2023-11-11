package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SoundType;

import appeng.core.AEConfig;

@Mixin(BlockItem.class)
public abstract class FormationPlaneVolumeMixin {

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getVolume()F"))
    private float modifyVolume(SoundType instance, BlockPlaceContext context) {
        if (context.getPlayer() instanceof FakePlayer
                && context.getPlayer().getGameProfile().getName().equals("[AE2]")) {
            return ((float) AEConfig.instance().getPlaneVolumeLevel()) - 1;
        } else {
            return instance.getVolume();
        }
    }
}
