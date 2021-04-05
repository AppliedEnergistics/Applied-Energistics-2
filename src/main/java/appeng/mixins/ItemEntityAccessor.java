package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.item.ItemEntity;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {

    @Accessor
    int getAge();

    @Accessor
    void setAge(int age);

}
