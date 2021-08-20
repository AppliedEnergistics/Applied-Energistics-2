package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    @Invoker("renderGuiItem")
    void callRenderGuiItemModel(ItemStack stack, int x, int y, BakedModel model);

}
