package appeng.mixins;

import java.util.List;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Container.class)
public interface ScreenHandlerListeners {

    @Accessor("listeners")
    List<IContainerListener> ae2_getListeners();

}
