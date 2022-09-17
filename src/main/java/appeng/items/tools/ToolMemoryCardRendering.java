package appeng.items.tools;


import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.model.MemoryCardModel;
import appeng.core.AppEng;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ToolMemoryCardRendering extends ItemRenderingCustomizer {

    private static final ResourceLocation MODEL = new ResourceLocation(AppEng.MOD_ID, "builtin/memory_card");

    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IItemRendering rendering) {
        rendering.builtInModel("models/item/builtin/memory_card", new MemoryCardModel());
        rendering.model(new ModelResourceLocation(MODEL, "inventory")).variants(MODEL);
    }
}
