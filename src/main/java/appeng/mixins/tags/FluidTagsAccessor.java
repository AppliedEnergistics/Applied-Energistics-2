package appeng.mixins.tags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagRegistry;

@Mixin(FluidTags.class)
public interface FluidTagsAccessor {

    @Invoker("register")
    static ITag.INamedTag<Fluid> register(String id) {
        throw new AssertionError();
    }

    @Accessor("REQUIRED_TAGS")
    static TagRegistry<Fluid> getRequiredTags() {
        throw new AssertionError();
    }

}
