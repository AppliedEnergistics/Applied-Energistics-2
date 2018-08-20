package appeng.client.render;

import appeng.client.render.cablebus.FacadeBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by covers1624 on 14/08/18.
 */
public class FacadeBakedItemModel extends DelegateBakedModel {

    private final ItemStack textureStack;
    private final FacadeBuilder facadeBuilder;

    protected FacadeBakedItemModel(IBakedModel base, ItemStack textureStack, FacadeBuilder facadeBuilder) {
        super(base);
        this.textureStack = textureStack;
        this.facadeBuilder = facadeBuilder;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if(side != null) {
            return Collections.emptyList();
        }
        List<BakedQuad> quads = new ArrayList<>();
        quads.addAll(facadeBuilder.buildFacadeItemQuads(textureStack, EnumFacing.NORTH));
        quads.addAll(getBaseModel().getQuads(state, side, rand));
        return quads;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
