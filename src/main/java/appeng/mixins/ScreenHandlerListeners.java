package appeng.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerListeners {

    @Accessor("listeners")
    List<ScreenHandlerListener> ae2_getListeners();

}
