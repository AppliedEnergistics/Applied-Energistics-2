package appeng.mixins.tags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;

@Mixin(ItemTags.class)
public interface ItemTagsAccessor {

    @Invoker("makeWrapperTag")
    static ITag.INamedTag<Item> register(String id) {
        throw new AssertionError();
    }

}
