package appeng.mixins;

import appeng.core.definitions.AEParts;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the ME Annihilation Plane to the items to check for when applying
 * unbreaking.
 */
@Mixin(DigDurabilityEnchantment.class)
public class UnbreakingMixin {
    @Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
    public void enchantPlane(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() == AEParts.ANNIHILATION_PLANE.asItem()) {
            cir.setReturnValue(true);
        }
    }
}
