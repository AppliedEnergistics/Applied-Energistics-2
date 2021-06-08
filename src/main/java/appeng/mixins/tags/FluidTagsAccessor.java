package appeng.mixins.tags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;

@Mixin(FluidTags.class)
public interface FluidTagsAccessor {

    @Invoker("makeWrapperTag")
    static ITag.INamedTag<Fluid> register(String id) {
        throw new AssertionError();
    }

}
