
package appeng.client.render.crafting;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * This special model handles switching between rendering the crafting output of an encoded pattern (when shift is being
 * held), and showing the encoded pattern itself. Matters are further complicated by only wanting to show the crafting output when
 * the pattern is being rendered in the GUI, and not anywhere else.
 */
@OnlyIn(Dist.CLIENT)
public class EncodedPatternRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void render(ItemStack is, MatrixStack ms, IRenderTypeBuffer buffers, int combinedLight, int combinedOverlay) {
    }

}
