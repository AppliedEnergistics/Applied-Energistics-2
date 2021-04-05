package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    @Invoker("renderItemModelIntoGUI")
    void callRenderGuiItemModel(ItemStack stack, int x, int y, IBakedModel model);

}
