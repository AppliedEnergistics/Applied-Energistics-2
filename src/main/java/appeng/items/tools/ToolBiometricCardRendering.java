package appeng.items.tools;


import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.model.BiometricCardModel;
import appeng.core.AppEng;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ToolBiometricCardRendering extends ItemRenderingCustomizer {

    private static final ResourceLocation MODEL = new ResourceLocation(AppEng.MOD_ID, "builtin/biometric_card");

    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IItemRendering rendering) {
        rendering.builtInModel("models/item/builtin/biometric_card", new BiometricCardModel());
        rendering.model(new ModelResourceLocation(MODEL, "inventory")).variants(MODEL);
    }
}
