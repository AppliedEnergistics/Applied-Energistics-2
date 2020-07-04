package appeng.mixins;

import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotMixin {

    @Accessor("x")
    void setX(int value);

    @Accessor("y")
    void setY(int value);

    @Accessor("index")
    int getIndex();

}
