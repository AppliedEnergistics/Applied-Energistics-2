/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.model;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.matrix.MatrixStack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverride;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.common.model.TransformationHelper;

public class UVLModelLoader implements IModelLoader<UVLModelLoader.UVLModelWrapper> {

    public static final UVLModelLoader INSTANCE = new UVLModelLoader();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public UVLModelWrapper read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        modelContents.remove("loader");
        BlockModel blockModel = UVLSERIALIZER.fromJson(modelContents, BlockModel.class);
        return new UVLModelWrapper(blockModel);
    }

    final Gson UVLSERIALIZER = (new GsonBuilder())
            .registerTypeAdapter(BlockModel.class, new ModelLoaderRegistry.ExpandedBlockModelDeserializer())
            .registerTypeAdapter(BlockPart.class, new BlockPart.Deserializer())
            .registerTypeAdapter(BlockPartFace.class, new BlockPartFaceOverrideSerializer())
            .registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
            .registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer())
            .registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer())
            .registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer())
            .registerTypeAdapter(TransformationMatrix.class, new TransformationHelper.Deserializer()).create();

    private static class BlockPartFaceOverrideSerializer extends BlockPartFace.Deserializer {
        @Override
        public BlockPartFace deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_,
                JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            BlockPartFace blockFace = super.deserialize(p_deserialize_1_, p_deserialize_2_, p_deserialize_3_);
            Pair<Float, Float> uvl = this.parseUVL(p_deserialize_1_.getAsJsonObject());
            if (uvl != null) {
                return new BlockPartFaceWithUVL(blockFace.cullFace, blockFace.tintIndex, blockFace.texture,
                        blockFace.blockFaceUV, uvl.getLeft(), uvl.getRight());
            }
            return blockFace;
        }

        protected Pair<Float, Float> parseUVL(JsonObject object) {
            if (!object.has("uvlightmap")) {
                return null;
            }
            object = object.get("uvlightmap").getAsJsonObject();
            return new ImmutablePair<>(JSONUtils.getFloat(object, "sky", 0), JSONUtils.getFloat(object, "block", 0));
        }
    }

    private static class BlockPartFaceWithUVL extends BlockPartFace {

        private final float sky;
        private final float block;

        public BlockPartFaceWithUVL(@Nullable Direction cullFaceIn, int tintIndexIn, String textureIn,
                BlockFaceUV blockFaceUVIn, float sky, float block) {
            super(cullFaceIn, tintIndexIn, textureIn, blockFaceUVIn);
            this.sky = sky;
            this.block = block;
        }

        public float getSky() {
            return sky;
        }

        public float getBlock() {
            return block;
        }
    }

    public static class UVLModelWrapper implements IModelGeometry<UVLModelWrapper> {
        private final BlockModel parent;

        public UVLModelWrapper(BlockModel parent) {
            this.parent = parent;
        }

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
                Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                ItemOverrideList overrides, ResourceLocation modelLocation) {
            TextureAtlasSprite particle = spriteGetter.apply(owner.resolveTexture("particle"));

            IModelBuilder<?> builder = IModelBuilder.of(owner, overrides, particle);
            for (BlockPart blockpart : parent.getElements()) {
                for (Direction direction : blockpart.mapFaces.keySet()) {
                    BlockPartFace blockpartface = blockpart.mapFaces.get(direction);
                    TextureAtlasSprite textureatlassprite1 = spriteGetter
                            .apply(owner.resolveTexture(blockpartface.texture));
                    BakedQuad quad = BlockModel.makeBakedQuad(blockpart, blockpartface, textureatlassprite1, direction,
                            modelTransform, modelLocation);

                    if (blockpartface instanceof BlockPartFaceWithUVL) {
                        BlockPartFaceWithUVL uvlFace = (BlockPartFaceWithUVL) blockpartface;
                        quad = applyPreBakedLighting(quad, textureatlassprite1, uvlFace.sky, uvlFace.block);
                    }

                    if (blockpartface.cullFace == null) {
                        builder.addGeneralQuad(quad);
                    } else {
                        builder.addFaceQuad(modelTransform.getRotation().rotateTransform(blockpartface.cullFace), quad);
                    }
                }
            }
            return builder.build();
        }

        private BakedQuad applyPreBakedLighting(BakedQuad quad, TextureAtlasSprite sprite, float sky, float block) {
            // FIXME: just piping through the quads and manipulating uv index 2 directly
            // seems way easier than this
            BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
            VertexLighterFlat trans = new VertexLighterFlat(Minecraft.getInstance().getBlockColors()) {

                @Override
                protected void updateLightmap(float[] normal, float[] lightmap, float x, float y, float z) {
                    lightmap[0] = block;
                    lightmap[1] = sky;
                }

                @Override
                public void setQuadTint(int tint) {
                    // Tint requires a block state which we don't have at this point
                }
            };
            MatrixStack identity = new MatrixStack();
            trans.setTransform(identity.getLast());
            trans.setParent(builder);
            quad.pipe(trans);
            builder.setQuadTint(quad.getTintIndex());
            builder.setQuadOrientation(quad.getFace());
            builder.setApplyDiffuseLighting(false);
            return builder.build();
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner,
                Function<ResourceLocation, IUnbakedModel> modelGetter,
                Set<com.mojang.datafixers.util.Pair<String, String>> missingTextureErrors) {
            return parent.getTextures(modelGetter, missingTextureErrors);
        }

    }

}
