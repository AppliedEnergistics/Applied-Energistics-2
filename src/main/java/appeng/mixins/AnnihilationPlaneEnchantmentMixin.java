package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.Item;

import appeng.core.definitions.AEParts;

/**
 * Adds the ME Annihilation Plane to the items to check for when applying
 * {@link net.minecraft.world.item.enchantment.EnchantmentCategory#DIGGER} enchantments.
 */
@Mixin(targets = "net/minecraft/world/item/enchantment/EnchantmentCategory$7")
public class AnnihilationPlaneEnchantmentMixin {
    @Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
    public void enchantPlane(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item == AEParts.ANNIHILATION_PLANE.asItem()) {
            cir.setReturnValue(true);
        }
    }
}
