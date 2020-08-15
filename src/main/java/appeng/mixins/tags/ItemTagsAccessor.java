package appeng.mixins.tags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.Item;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;

@Mixin(ItemTags.class)
public interface ItemTagsAccessor {

    @Invoker("register")
    static Tag.Identified<Item> register(String id) {
        throw new AssertionError();
    }

}
