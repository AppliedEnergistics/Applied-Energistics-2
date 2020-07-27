package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public interface SlotMixin {

    @Accessor("x")
    void setX(int value);

    @Accessor("y")
    void setY(int value);

    @Accessor("index")
    int getIndex();

}
