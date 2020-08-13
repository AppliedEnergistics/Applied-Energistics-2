package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStorageHelper;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin sets the teleport destination, because otherwise Vanilla will not
 * move the player.
 */
@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "func_241829_a", at = @At("HEAD"), cancellable = true, allow = 1)
    public void getTeleportTarget(ServerWorld destination, CallbackInfoReturnable<PortalInfo> cri) {
        // Check if a destination has been set for the entity currently being teleported
        if (destination.func_234923_W_() == SpatialStorageDimensionIds.WORLD_ID) {
            PortalInfo target = SpatialStorageHelper.getInstance().getTeleportTarget();
            if (target != null) {
                cri.setReturnValue(target);
            }
        }
    }

}
