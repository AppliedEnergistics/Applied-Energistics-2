package appeng.client.model;

import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Extends Vanillas {@link net.minecraft.client.renderer.block.model.Variant} with rotation around the Z-axis.
 */
public record SpinnableVariant(ResourceLocation modelLocation,
                               SimpleModelState modelState) implements BlockModelPart.Unbaked {
    public static final MapCodec<SpinnableVariant> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            ResourceLocation.CODEC.fieldOf("model").forGetter(SpinnableVariant::modelLocation),
                            SimpleModelState.MAP_CODEC.forGetter(SpinnableVariant::modelState)
                    )
                    .apply(builder, SpinnableVariant::new)
    );
    public static final Codec<SpinnableVariant> CODEC = MAP_CODEC.codec();

    public SpinnableVariant(ResourceLocation model) {
        this(model, SimpleModelState.DEFAULT);
    }

    public SpinnableVariant withXRot(Quadrant xRot) {
        return this.withState(this.modelState.withX(xRot));
    }

    public SpinnableVariant withYRot(Quadrant yRot) {
        return this.withState(this.modelState.withY(yRot));
    }

    public SpinnableVariant withZRot(Quadrant zRot) {
        return this.withState(this.modelState.withZ(zRot));
    }

    public SpinnableVariant withUvLock(boolean uvLock) {
        return this.withState(this.modelState.withUvLock(uvLock));
    }

    public SpinnableVariant withModel(ResourceLocation model) {
        return new SpinnableVariant(model, this.modelState);
    }

    public SpinnableVariant withState(SimpleModelState state) {
        return new SpinnableVariant(this.modelLocation, state);
    }

    @Override
    public BlockModelPart bake(ModelBaker baker) {
        return SimpleModelWrapper.bake(baker, this.modelLocation, this.modelState.asModelState());
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver p_410425_) {
        p_410425_.markDependency(this.modelLocation);
    }

    @OnlyIn(Dist.CLIENT)
    public record SimpleModelState(Quadrant x, Quadrant y, Quadrant z, boolean uvLock) {
        public static final MapCodec<SimpleModelState> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                Quadrant.CODEC.optionalFieldOf("x", Quadrant.R0).forGetter(SimpleModelState::x),
                                Quadrant.CODEC.optionalFieldOf("y", Quadrant.R0).forGetter(SimpleModelState::y),
                                Quadrant.CODEC.optionalFieldOf("z", Quadrant.R0).forGetter(SimpleModelState::z),
                                Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(SimpleModelState::uvLock)
                        )
                        .apply(builder, SimpleModelState::new)
        );
        public static final SimpleModelState DEFAULT = new SimpleModelState(Quadrant.R0, Quadrant.R0, Quadrant.R0, false);
        private static final ModelState[] STATES = createTransformations(false);
        private static final ModelState[] UV_LOCKED_STATES = createTransformations(true);

        private static ModelState[] createTransformations(boolean uvLocked) {
            var result = new ModelState[4 * 4 * 4];
            var quadrants = Quadrant.values();
            var angles = new float[]{0, 90, 180, 270};

            for (var xRot : quadrants) {
                for (var yRot : quadrants) {
                    // Reuse existing states from Vanilla
                    int baseIdx = indexFromAngles(xRot, yRot, Quadrant.R0);
                    var blockModelRotation = BlockModelRotation.by(xRot, yRot);
                    if (uvLocked) {
                        result[baseIdx] = blockModelRotation.withUvLock();
                    } else {
                        result[baseIdx] = blockModelRotation;
                    }

                    for (var zRot : quadrants) {
                        if (zRot == Quadrant.R0) {
                            continue;
                        }
                        var idx = indexFromAngles(xRot, yRot, zRot);

                        // NOTE: Mojangs block model rotation rotates in the opposite direction
                        var quaternion = new Quaternionf().rotateYXZ(
                                -angles[yRot.shift] * Mth.DEG_TO_RAD,
                                -angles[xRot.shift] * Mth.DEG_TO_RAD,
                                -angles[zRot.shift] * Mth.DEG_TO_RAD);

                        var rotationMatrix = new Matrix4f()
                                .identity()
                                .rotate(quaternion);
                        var transformation = new Transformation(rotationMatrix);

                        if (uvLocked) {
                            var faceMapping = new EnumMap<Direction, Matrix4fc>(Direction.class);
                            var inverseFaceMapping = new EnumMap<Direction, Matrix4fc>(Direction.class);
                            for (Direction direction : Direction.values()) {
                                Matrix4fc matrix4fc = BlockMath.getFaceTransformation(transformation, direction).getMatrix();
                                faceMapping.put(direction, matrix4fc);
                                inverseFaceMapping.put(direction, matrix4fc.invertAffine(new Matrix4f()));
                            }
                            result[idx] = new SpinnableModelState(transformation, faceMapping, inverseFaceMapping);
                        } else {
                            result[idx] = new SpinnableModelState(transformation, Collections.emptyMap(), Collections.emptyMap());
                        }
                    }
                }
            }

            return result;
        }

        private static int indexFromAngles(Quadrant xRot, Quadrant yRot, Quadrant zRot) {
            return xRot.shift * 16 + yRot.shift * 4 + zRot.shift;
        }

        public ModelState asModelState() {
            int idx = indexFromAngles(x, y, z);
            return this.uvLock ? UV_LOCKED_STATES[idx] : STATES[idx];
        }

        public SimpleModelState withX(Quadrant quadrant) {
            return new SimpleModelState(quadrant, this.y, this.z, this.uvLock);
        }

        public SimpleModelState withY(Quadrant quadrant) {
            return new SimpleModelState(this.x, quadrant, this.z, this.uvLock);
        }

        public SimpleModelState withZ(Quadrant quadrant) {
            return new SimpleModelState(this.x, this.y, quadrant, this.uvLock);
        }

        public SimpleModelState withUvLock(boolean uvLock) {
            return new SimpleModelState(this.x, this.y, this.z, uvLock);
        }

        private record SpinnableModelState(Transformation transformation,
                                           Map<Direction, Matrix4fc> faceTransformations,
                                           Map<Direction, Matrix4fc> inverseFaceTransformations
        ) implements ModelState {
            @Override
            public Matrix4fc faceTransformation(Direction face) {
                return faceTransformations.getOrDefault(face, NO_TRANSFORM);
            }

            @Override
            public Matrix4fc inverseFaceTransformation(Direction face) {
                return inverseFaceTransformations.getOrDefault(face, NO_TRANSFORM);
            }
        }
    }
}
