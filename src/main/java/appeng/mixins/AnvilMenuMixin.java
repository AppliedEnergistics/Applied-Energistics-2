package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.inventory.AnvilMenu;

import appeng.parts.automation.AnnihilationPlanePartItem;

/**
 * @see AnnihilationPlanePartItem#CALLING_DAMAGEABLE_FROM_ANVIL
 */
@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "net/minecraft/world/item/ItemStack.isDamageableItem()Z", ordinal = 1))
    public void setAnnihilationPlaneThreadLocal(CallbackInfo ci) {
        AnnihilationPlanePartItem.CALLING_DAMAGEABLE_FROM_ANVIL.set(Boolean.TRUE);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "net/minecraft/world/item/ItemStack.isDamageableItem()Z", ordinal = 1, shift = At.Shift.AFTER))
    public void clearAnnihilationPlaneThreadLocal(CallbackInfo ci) {
        AnnihilationPlanePartItem.CALLING_DAMAGEABLE_FROM_ANVIL.set(null);
    }
}
