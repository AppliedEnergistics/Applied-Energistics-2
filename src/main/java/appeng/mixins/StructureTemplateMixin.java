package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import appeng.hooks.VisualStateSaving;

/**
 * This Mixin allows us to save additional data in block entity NBT when we're saved as part of a structure file. We
 * need this to support saving and restoring client-side state if the structure is used to set up Create Ponders.
 */
@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Inject(method = "fillFromWorld", at = @At("HEAD"))
    public void enableClientSideStateSaving(CallbackInfo ci) {
        VisualStateSaving.setEnabled(true);
    }

    @Inject(method = "fillFromWorld", at = @At("TAIL"))
    public void disableClientSideStateSaving(CallbackInfo ci) {
        VisualStateSaving.setEnabled(false);
    }
}
