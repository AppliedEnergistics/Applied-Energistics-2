package appeng.client.render.model;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import appeng.client.model.SpinnableVariant;
import appeng.core.AppEng;

public class SingleSpinnableVariant implements BlockStateModel {
    private final BlockModelPart model;

    public SingleSpinnableVariant(BlockModelPart p_410307_) {
        this.model = p_410307_;
    }

    public void collectParts(RandomSource p_410554_, List<BlockModelPart> p_410007_) {
        p_410007_.add(this.model);
    }

    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this;
    }

    public TextureAtlasSprite particleIcon() {
        return this.model.particleIcon();
    }

    public record Unbaked(SpinnableVariant variant) implements CustomUnbakedBlockStateModel {
        public static final Identifier ID = AppEng.makeId("spinnable_variant");
        public static final MapCodec<Unbaked> MAP_CODEC = SpinnableVariant.MAP_CODEC.xmap(Unbaked::new,
                Unbaked::variant);
        public static final Codec<Unbaked> CODEC = MAP_CODEC.codec();

        public Unbaked(SpinnableVariant variant) {
            this.variant = variant;
        }

        public BlockStateModel bake(ModelBaker p_410856_) {
            return new net.minecraft.client.renderer.block.model.SingleVariant(this.variant.bake(p_410856_));
        }

        public void resolveDependencies(ResolvableModel.Resolver p_410724_) {
            this.variant.resolveDependencies(p_410724_);
        }

        public SpinnableVariant variant() {
            return this.variant;
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }
    }
}
