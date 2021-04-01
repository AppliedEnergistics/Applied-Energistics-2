package appeng.mixins.tags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

@Mixin(BlockTags.class)
public interface BlockTagsAccessor {

    @Invoker("register")
    static ITag.INamedTag<Block> register(String id) {
        throw new AssertionError();
    }

}
