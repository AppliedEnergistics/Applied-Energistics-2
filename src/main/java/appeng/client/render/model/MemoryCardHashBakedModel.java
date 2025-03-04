package appeng.client.render.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.items.MemoryCardColors;
import appeng.client.render.cablebus.CubeBuilder;

final class MemoryCardHashBakedModel extends DelegateBakedModel {
    private final List<BakedQuad> generalQuads;

    public MemoryCardHashBakedModel(BakedModel baseModel, TextureAtlasSprite texture, MemoryCardColors colors) {
        super(baseModel);
        this.generalQuads = List.copyOf(this.buildGeneralQuads(texture, colors));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        if (side == null) {
            return this.generalQuads;
        } else {
            return List.of();
        }
    }

    private List<BakedQuad> buildGeneralQuads(TextureAtlasSprite texture, MemoryCardColors colors) {
        CubeBuilder builder = new CubeBuilder();

        builder.setTexture(texture);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                var color = colors.get(x, y);
                builder.setColorRGB(color.mediumVariant);
                builder.addCube(8 + x, 8 + 1 - y, 7.5f, 8 + x + 1, 8 + 1 - y + 1, 8.5f);
            }
        }

        return builder.getOutput();
    }
}
