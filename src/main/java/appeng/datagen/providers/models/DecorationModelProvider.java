/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2025, TeamAppliedEnergistics, All rights reserved.
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

package appeng.datagen.providers.models;

import appeng.block.misc.QuartzFixtureBlock;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockStateGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;

import java.util.Map;

public class DecorationModelProvider extends ModelSubProvider {
    public DecorationModelProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        super(blockModels, itemModels);
    }

    @Override
    protected void register() {

        blockModels.blockStateOutput.accept(applyAthena(
                BlockModelGenerators.createSimpleBlock(
                        AEBlocks.CHISELED_QUARTZ_BLOCK.block(),
                        ModelTemplates.CUBE_ALL.create(AEBlocks.CHISELED_QUARTZ_BLOCK.block(), TextureMapping.cube(AEBlocks.CHISELED_QUARTZ_BLOCK.block()), modelOutput)
                ),
                "athena:limited_pillar",
                Map.of(
                        "bottom", AppEng.makeId("block/chiseled_quartz_block_bottom"),
                        "center", AppEng.makeId("block/chiseled_quartz_block_side"),
                        "self", AppEng.makeId("block/chiseled_quartz_block_side"),
                        "top", AppEng.makeId("block/chiseled_quartz_block_top"),
                        "particle", AppEng.makeId("block/cut_quartz_block")
                )
        ));
        stairsBlock(AEBlocks.CHISELED_QUARTZ_STAIRS, "block/chiseled_quartz_block_top",
                "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
        slabBlock(AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_BLOCK, "block/chiseled_quartz_block_top",
                "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
        wall(AEBlocks.CHISELED_QUARTZ_WALL, "block/chiseled_quartz_block_side");

        blockModels.blockStateOutput.accept(applyAthena(
                BlockModelGenerators.createSimpleBlock(
                        AEBlocks.FLUIX_BLOCK.block(),
                        ModelTemplates.CUBE_ALL.create(AEBlocks.FLUIX_BLOCK.block(), TextureMapping.cube(AEBlocks.FLUIX_BLOCK.block()), modelOutput)
                ),
                "athena:ctm",
                Map.of(
                        "center", AppEng.makeId("block/fluix_block_center"),
                        "empty", AppEng.makeId("block/fluix_block_empty"),
                        "horizontal", AppEng.makeId("block/fluix_block_h"),
                        "vertical", AppEng.makeId("block/fluix_block_v"),
                        "particle", AppEng.makeId("block/fluix_block")
                )
        ));
        stairsBlock(AEBlocks.FLUIX_STAIRS, AEBlocks.FLUIX_BLOCK);
        slabBlock(AEBlocks.FLUIX_SLAB, AEBlocks.FLUIX_BLOCK);
        wall(AEBlocks.FLUIX_WALL, "block/fluix_block");

        blockModels.blockStateOutput.accept(applyAthena(
                BlockModelGenerators.createSimpleBlock(
                        AEBlocks.QUARTZ_BLOCK.block(),
                        ModelTemplates.CUBE_ALL.create(AEBlocks.QUARTZ_BLOCK.block(), TextureMapping.cube(AEBlocks.QUARTZ_BLOCK.block()), modelOutput)
                ),
                "athena:ctm",
                Map.of(
                        "center", AppEng.makeId("block/quartz_block_center"),
                        "empty", AppEng.makeId("block/quartz_block_empty"),
                        "horizontal", AppEng.makeId("block/quartz_block_h"),
                        "vertical", AppEng.makeId("block/quartz_block_v"),
                        "particle", AppEng.makeId("block/quartz_block")
                )
        ));
        stairsBlock(AEBlocks.QUARTZ_STAIRS, AEBlocks.QUARTZ_BLOCK);
        slabBlock(AEBlocks.QUARTZ_SLAB, AEBlocks.QUARTZ_BLOCK);
        wall(AEBlocks.QUARTZ_WALL, "block/quartz_block");

        blockModels.blockStateOutput.accept(applyAthena(
                BlockModelGenerators.createSimpleBlock(
                        AEBlocks.CUT_QUARTZ_BLOCK.block(),
                        ModelTemplates.CUBE_ALL.create(AEBlocks.CUT_QUARTZ_BLOCK.block(), TextureMapping.cube(AEBlocks.CUT_QUARTZ_BLOCK.block()), modelOutput)
                ),
                "athena:ctm",
                Map.of(
                        "center", AppEng.makeId("block/cut_quartz_block_center"),
                        "empty", AppEng.makeId("block/smooth_quartz_block"),
                        "horizontal", AppEng.makeId("block/cut_quartz_block_h"),
                        "vertical", AppEng.makeId("block/cut_quartz_block_v"),
                        "particle", AppEng.makeId("block/cut_quartz_block")
                )
        ));
        stairsBlock(AEBlocks.CUT_QUARTZ_STAIRS, AEBlocks.CUT_QUARTZ_BLOCK);
        slabBlock(AEBlocks.CUT_QUARTZ_SLAB, AEBlocks.CUT_QUARTZ_BLOCK);
        wall(AEBlocks.CUT_QUARTZ_WALL, "block/cut_quartz_block");

        simpleBlockAndItem(AEBlocks.SMOOTH_QUARTZ_BLOCK);
        stairsBlock(AEBlocks.SMOOTH_QUARTZ_STAIRS, AEBlocks.SMOOTH_QUARTZ_BLOCK);
        slabBlock(AEBlocks.SMOOTH_QUARTZ_SLAB, AEBlocks.SMOOTH_QUARTZ_BLOCK);
        wall(AEBlocks.SMOOTH_QUARTZ_WALL, "block/smooth_quartz_block");

        simpleBlockAndItem(AEBlocks.QUARTZ_BRICKS);
        stairsBlock(AEBlocks.QUARTZ_BRICK_STAIRS, AEBlocks.QUARTZ_BRICKS);
        slabBlock(AEBlocks.QUARTZ_BRICK_SLAB, AEBlocks.QUARTZ_BRICKS);
        wall(AEBlocks.QUARTZ_BRICK_WALL, "block/quartz_bricks");

        blockModels.blockStateOutput.accept(applyAthena(
                BlockModelGenerators.createAxisAlignedPillarBlock(
                        AEBlocks.QUARTZ_PILLAR.block(),
                        ModelTemplates.CUBE_COLUMN.create(AEBlocks.QUARTZ_PILLAR.block(), TextureMapping.cube(AEBlocks.QUARTZ_PILLAR.block()), modelOutput)
                ),
                "athena:pillar",
                Map.of(
                        "bottom", AppEng.makeId("block/quartz_pillar_bottom"),
                        "center", AppEng.makeId("block/quartz_pillar_side"),
                        "self", AppEng.makeId("block/quartz_pillar_side"),
                        "top", AppEng.makeId("block/quartz_pillar_top"),
                        "particle", AppEng.makeId("block/cut_quartz_block")
                )
        ));
        stairsBlock(AEBlocks.QUARTZ_PILLAR_STAIRS, "block/quartz_pillar_top", "block/quartz_pillar_side",
                "block/quartz_pillar_top");
        slabBlock(AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR, "block/quartz_pillar_top",
                "block/quartz_pillar_side", "block/quartz_pillar_top");
        wall(AEBlocks.QUARTZ_PILLAR_WALL, "block/quartz_pillar_side");

        simpleBlockAndItem(AEBlocks.SKY_STONE_BLOCK);
        stairsBlock(AEBlocks.SKY_STONE_STAIRS, AEBlocks.SKY_STONE_BLOCK);
        slabBlock(AEBlocks.SKY_STONE_SLAB, AEBlocks.SKY_STONE_BLOCK);
        wall(AEBlocks.SKY_STONE_WALL, "block/sky_stone_block");

        simpleBlockAndItem(AEBlocks.SKY_STONE_SMALL_BRICK);
        stairsBlock(AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS, AEBlocks.SKY_STONE_SMALL_BRICK);
        slabBlock(AEBlocks.SKY_STONE_SMALL_BRICK_SLAB, AEBlocks.SKY_STONE_SMALL_BRICK);
        wall(AEBlocks.SKY_STONE_SMALL_BRICK_WALL, "block/sky_stone_small_brick");

        simpleBlockAndItem(AEBlocks.SKY_STONE_BRICK);
        stairsBlock(AEBlocks.SKY_STONE_BRICK_STAIRS, AEBlocks.SKY_STONE_BRICK);
        slabBlock(AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_BRICK);
        wall(AEBlocks.SKY_STONE_BRICK_WALL, "block/sky_stone_brick");

        blockModels.blockStateOutput.accept(applyAthena(
                BlockModelGenerators.createSimpleBlock(
                        AEBlocks.SMOOTH_SKY_STONE_BLOCK.block(),
                        ModelTemplates.CUBE_ALL.create(AEBlocks.SMOOTH_SKY_STONE_BLOCK.block(), TextureMapping.cube(AEBlocks.SMOOTH_SKY_STONE_BLOCK.block()), modelOutput)
                ),
                "athena:ctm",
                Map.of(
                        "center", AppEng.makeId("block/smooth_sky_stone_block_center"),
                        "empty", AppEng.makeId("block/smooth_sky_stone_block_empty"),
                        "horizontal", AppEng.makeId("block/smooth_sky_stone_block_h"),
                        "vertical", AppEng.makeId("block/smooth_sky_stone_block_v"),
                        "particle", AppEng.makeId("block/smooth_sky_stone_block")
                )
        ));
        stairsBlock(AEBlocks.SMOOTH_SKY_STONE_STAIRS, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        slabBlock(AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        wall(AEBlocks.SMOOTH_SKY_STONE_WALL, "block/smooth_sky_stone_block");

        quartzFixture();
    }

    private void quartzFixture() {
        var block = AEBlocks.QUARTZ_FIXTURE.block();

        var standing = ModelLocationUtils.getModelLocation(block, "_standing");
        var standingOdd = ModelLocationUtils.getModelLocation(block, "_standing_odd");
        var wall = ModelLocationUtils.getModelLocation(block, "_wall");
        var wallOdd = ModelLocationUtils.getModelLocation(block, "_wall_odd");

        var dispatch = PropertyDispatch.properties(QuartzFixtureBlock.FACING, QuartzFixtureBlock.ODD);

        dispatch.select(Direction.DOWN, false, Variant.variant()
                .with(VariantProperties.MODEL, standing)
                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180));

        dispatch.select(Direction.DOWN, true, Variant.variant()
                .with(VariantProperties.MODEL, standingOdd)
                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180));

        dispatch.select(Direction.EAST, false, Variant.variant()
                .with(VariantProperties.MODEL, wall)
                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));

        dispatch.select(Direction.EAST, true, Variant.variant()
                .with(VariantProperties.MODEL, wallOdd)
                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));

        dispatch.select(Direction.NORTH, false, Variant.variant()
                .with(VariantProperties.MODEL, wall));

        dispatch.select(Direction.NORTH, true, Variant.variant()
                .with(VariantProperties.MODEL, wallOdd));

        dispatch.select(Direction.SOUTH, false, Variant.variant()
                .with(VariantProperties.MODEL, wall)
                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));

        dispatch.select(Direction.SOUTH, true, Variant.variant()
                .with(VariantProperties.MODEL, wallOdd)
                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180));

        dispatch.select(Direction.UP, false, Variant.variant()
                .with(VariantProperties.MODEL, standing));

        dispatch.select(Direction.UP, true, Variant.variant()
                .with(VariantProperties.MODEL, standingOdd));

        dispatch.select(Direction.WEST, false, Variant.variant()
                .with(VariantProperties.MODEL, wall)
                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));

        dispatch.select(Direction.WEST, true, Variant.variant()
                .with(VariantProperties.MODEL, wallOdd)
                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));

        multiVariantGenerator(AEBlocks.QUARTZ_FIXTURE).with(dispatch);
        multiVariantGenerator(AEBlocks.LIGHT_DETECTOR).with(dispatch);

        // Special item model
        blockModels.registerSimpleItemModel(
                AEBlocks.QUARTZ_FIXTURE.asItem(),
                ModelLocationUtils.getModelLocation(AEBlocks.QUARTZ_FIXTURE.asItem())
        );
        blockModels.registerSimpleItemModel(
                AEBlocks.LIGHT_DETECTOR.asItem(),
                ModelLocationUtils.getModelLocation(AEBlocks.QUARTZ_FIXTURE.asItem())
        );
    }

    private BlockStateGenerator applyAthena(BlockStateGenerator delegate, String loader, Map<String, ResourceLocation> ctmTextures) {
        return new BlockStateGenerator() {
            @Override
            public Block getBlock() {
                return delegate.getBlock();
            }

            @Override
            public JsonElement get() {
                var root = (JsonObject) delegate.get();

                var ctmTexturesObj = new JsonObject();
                for (var entry : ctmTextures.entrySet()) {
                    ctmTexturesObj.addProperty(entry.getKey(), entry.getValue().toString());
                }

                root.addProperty("athena:loader", loader);
                root.add("ctm_textures", ctmTexturesObj);
                return root;
            }
        };
    }

    private void wall(BlockDefinition<? extends WallBlock> blockDef, String texture) {
        var block = blockDef.block();

        var textures = new TextureMapping().put(TextureSlot.WALL, AppEng.makeId(texture));

        var lowSideModel = ModelTemplates.WALL_LOW_SIDE.create(block, textures, modelOutput);
        var postModel = ModelTemplates.WALL_POST.create(block, textures, modelOutput);
        var tallSideModel = ModelTemplates.WALL_TALL_SIDE.create(block, textures, modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createWall(
                block,
                postModel,
                lowSideModel,
                tallSideModel
        ));

        var invModel = ModelTemplates.WALL_INVENTORY.create(block, textures, modelOutput);
        blockModels.registerSimpleItemModel(block, invModel);
    }

    private void slabBlock(BlockDefinition<? extends SlabBlock> blockDef, BlockDefinition<?> baseBlock) {
        var texture = TextureMapping.getBlockTexture(baseBlock.block()).getPath();
        slabBlock(blockDef, baseBlock, texture, texture, texture);
    }

    private void slabBlock(BlockDefinition<? extends SlabBlock> blockDef, BlockDefinition<?> baseBlock, String topTexture, String sideTexture, String bottomTexture) {
        var block = blockDef.block();

        var textures = new TextureMapping()
                .put(TextureSlot.TOP, AppEng.makeId(topTexture))
                .put(TextureSlot.BOTTOM, AppEng.makeId(bottomTexture))
                .put(TextureSlot.SIDE, AppEng.makeId(sideTexture));

        var topModel = ModelTemplates.SLAB_TOP.create(block, textures, modelOutput);
        var bottomModel = ModelTemplates.SLAB_BOTTOM.create(block, textures, modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSlab(
                block,
                bottomModel,
                topModel,
                ModelLocationUtils.getModelLocation(baseBlock.block())
        ));

        blockModels.registerSimpleItemModel(block, bottomModel);
    }

    private void stairsBlock(BlockDefinition<? extends StairBlock> stairsBlock, BlockDefinition<?> templateBlock) {
        var blockTexture = TextureMapping.getBlockTexture(templateBlock.block());
        stairsBlock(stairsBlock, blockTexture.getPath(), blockTexture.getPath(), blockTexture.getPath());
    }

    private void stairsBlock(BlockDefinition<? extends StairBlock> blockDef, String topTexture, String sideTexture, String bottomTexture) {
        var block = blockDef.block();

        var textures = new TextureMapping()
                .put(TextureSlot.TOP, AppEng.makeId(topTexture))
                .put(TextureSlot.BOTTOM, AppEng.makeId(bottomTexture))
                .put(TextureSlot.SIDE, AppEng.makeId(sideTexture));

        var straightModel = ModelTemplates.STAIRS_STRAIGHT.create(block, textures, modelOutput);
        var innerModel = ModelTemplates.STAIRS_INNER.create(block, textures, modelOutput);
        var outerModel = ModelTemplates.STAIRS_OUTER.create(block, textures, modelOutput);

        blockModels.blockStateOutput.accept(BlockModelGenerators.createStairs(
                block,
                innerModel,
                straightModel,
                outerModel
        ));
        blockModels.registerSimpleItemModel(block, straightModel);
    }
}
