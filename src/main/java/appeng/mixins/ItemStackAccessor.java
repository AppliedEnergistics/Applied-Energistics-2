package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Accessor(value = "capNBT", remap = false)
    CompoundNBT appeng2_getCapNBT();
}
