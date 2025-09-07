package appeng.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;

import appeng.core.definitions.AEParts;

/**
 * Workaround to make enchanted annihilation planes combinable in anvils to upgrade their enchantment levels.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    public AnvilMenuMixin(int containerId, Inventory playerInventory) {
        super(null, 0, null, null, null);
        throw new AssertionError();
    }

    @ModifyExpressionValue(method = "createResultInternal", at = @At(value = "INVOKE", target = "net/minecraft/world/item/ItemStack.isDamageableItem()Z", ordinal = 1))
    public boolean setAnnihilationPlaneThreadLocal(boolean isDamageable) {
        if (AEParts.ANNIHILATION_PLANE.is(inputSlots.getItem(0))
                && AEParts.ANNIHILATION_PLANE.is(inputSlots.getItem(1))) {
            return true; // Act damageable to allow combining items
        } else {
            return isDamageable;
        }
    }
}
