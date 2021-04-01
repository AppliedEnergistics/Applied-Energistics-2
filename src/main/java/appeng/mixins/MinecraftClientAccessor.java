package appeng.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {

    @Accessor
    ItemColors getItemColors();

}
