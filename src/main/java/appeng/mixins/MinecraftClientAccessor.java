package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {

    @Accessor
    ItemColors getItemColors();

}
