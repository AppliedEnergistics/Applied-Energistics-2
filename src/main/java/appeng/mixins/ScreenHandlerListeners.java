package appeng.mixins;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerListeners {

    @Accessor("listeners")
    List<ScreenHandlerListener> ae2_getListeners();

}
