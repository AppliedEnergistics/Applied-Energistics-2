package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.Level;

import appeng.hooks.VisualStateSaving;

/**
 * Similar to {@link StructureTemplateMixin}, this allows us to detect if Ponder is restoring backups of our block
 * entities from a block entity with no attached level. Replaces {@link appeng.util.Platform#isPonderLevel(Level)} in
 * such cases.
 * <p/>
 * Used for compatibility with PonderJS.
 */
@Mixin(targets = "com.simibubi.create.foundation.ponder.PonderWorld", remap = false)
@Pseudo
public class PonderWorldMixin {
    @Inject(method = "restore", at = @At("HEAD"), remap = false, require = 0)
    public void enableClientSideStateSavingForRestore(CallbackInfo ci) {
        VisualStateSaving.setEnabled(true);
    }

    @Inject(method = "restore", at = @At("TAIL"), remap = false, require = 0)
    public void disableClientSideStateSavingForRestore(CallbackInfo ci) {
        VisualStateSaving.setEnabled(false);
    }

    @Inject(method = "restoreBlocks", at = @At("HEAD"), remap = false, require = 0)
    public void enableClientSideStateSavingForRestoreBlocks(CallbackInfo ci) {
        VisualStateSaving.setEnabled(true);
    }

    @Inject(method = "restoreBlocks", at = @At("TAIL"), remap = false, require = 0)
    public void disableClientSideStateSavingForRestoreBlocks(CallbackInfo ci) {
        VisualStateSaving.setEnabled(false);
    }
}
