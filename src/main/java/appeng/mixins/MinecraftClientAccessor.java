package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Accessor
    ItemColors getItemColors();

}
