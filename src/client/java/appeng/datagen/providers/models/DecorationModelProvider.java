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

import static net.minecraft.client.data.models.BlockModelGenerators.createAxisAlignedPillarBlock;
import static net.minecraft.client.data.models.BlockModelGenerators.createSimpleBlock;
import static net.minecraft.client.data.models.BlockModelGenerators.createSlab;
import static net.minecraft.client.data.models.BlockModelGenerators.createStairs;
import static net.minecraft.client.data.models.BlockModelGenerators.createWall;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

import java.util.Map;

import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;

import appeng.block.misc.QuartzFixtureBlock;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;

public class DecorationModelProvider extends ModelSubProvider {
    public DecorationModelProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
            PartModelOutput partModels) {
        super(blockModels, itemModels, partModels);
    }

    @Override
    protected void register() {
        chiseledQuartz();
        fluix();
        quartz();
        cutQuartz();
        smoothQuartz();
        quartzBrick();
        quartzPillar();
        skyStone();
        skyStoneSmallBrick();
        skyStoneBrick();
        smoothSkyStone();

        quartzFixture();
    }

    private void smoothSkyStone() {
        blockModels.blockStateOutput.accept(
                createSimpleBlock(
                        AEBlocks.SMOOTH_SKY_STONE_BLOCK.block(),
                        applyAthena(
                                plainVariant(
                                        ModelTemplates.CUBE_ALL.create(AEBlocks.SMOOTH_SKY_STONE_BLOCK.block(),
                                                TextureMapping.cube(AEBlocks.SMOOTH_SKY_STONE_BLOCK.block()),
                                                modelOutput)),
                                "athena:ctm",
                                Map.of(
                                        "center", AppEng.makeId("block/smooth_sky_stone_block_center"),
                                        "empty", AppEng.makeId("block/smooth_sky_stone_block_empty"),
                                        "horizontal", AppEng.makeId("block/smooth_sky_stone_block_h"),
                                        "vertical", AppEng.makeId("block/smooth_sky_stone_block_v"),
                                        "particle", AppEng.makeId("block/smooth_sky_stone_block")))));
        stairsBlock(AEBlocks.SMOOTH_SKY_STONE_STAIRS, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        slabBlock(AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        wall(AEBlocks.SMOOTH_SKY_STONE_WALL, getBlockTexture(AEBlocks.SMOOTH_SKY_STONE_BLOCK));
    }

    private void skyStoneBrick() {
        simpleBlockAndItem(AEBlocks.SKY_STONE_BRICK);
        stairsBlock(AEBlocks.SKY_STONE_BRICK_STAIRS, AEBlocks.SKY_STONE_BRICK);
        slabBlock(AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_BRICK);
        wall(AEBlocks.SKY_STONE_BRICK_WALL, getBlockTexture(AEBlocks.SKY_STONE_BRICK));
    }

    private void skyStoneSmallBrick() {
        simpleBlockAndItem(AEBlocks.SKY_STONE_SMALL_BRICK);
        stairsBlock(AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS, AEBlocks.SKY_STONE_SMALL_BRICK);
        slabBlock(AEBlocks.SKY_STONE_SMALL_BRICK_SLAB, AEBlocks.SKY_STONE_SMALL_BRICK);
        wall(AEBlocks.SKY_STONE_SMALL_BRICK_WALL, getBlockTexture(AEBlocks.SKY_STONE_SMALL_BRICK));
    }

    private void skyStone() {
        simpleBlockAndItem(AEBlocks.SKY_STONE_BLOCK);
        stairsBlock(AEBlocks.SKY_STONE_STAIRS, AEBlocks.SKY_STONE_BLOCK);
        slabBlock(AEBlocks.SKY_STONE_SLAB, AEBlocks.SKY_STONE_BLOCK);
        wall(AEBlocks.SKY_STONE_WALL, getBlockTexture(AEBlocks.SKY_STONE_BLOCK));
    }

    private void quartzPillar() {
        var topTexture = getBlockTexture(AEBlocks.QUARTZ_PILLAR, "_top");
        var sideTexture = getBlockTexture(AEBlocks.QUARTZ_PILLAR, "_side");

        blockModels.blockStateOutput.accept(
                createAxisAlignedPillarBlock(
                        AEBlocks.QUARTZ_PILLAR.block(),
                        applyAthena(plainVariant(ModelTemplates.CUBE_COLUMN.create(
                                AEBlocks.QUARTZ_PILLAR.block(),
                                TextureMapping.column(sideTexture, getBlockTexture(AEBlocks.CUT_QUARTZ_BLOCK)),
                                modelOutput)),
                                "athena:pillar",
                                Map.of(
                                        "bottom", AppEng.makeId("block/quartz_pillar_bottom"),
                                        "center", sideTexture,
                                        "self", sideTexture,
                                        "top", topTexture,
                                        "particle", getBlockTexture(AEBlocks.CUT_QUARTZ_BLOCK)))));
        stairsBlock(AEBlocks.QUARTZ_PILLAR_STAIRS, topTexture, sideTexture, topTexture);
        slabBlock(AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR, topTexture, sideTexture, topTexture);
        wall(AEBlocks.QUARTZ_PILLAR_WALL, sideTexture);
    }

    private void quartzBrick() {
        simpleBlockAndItem(AEBlocks.QUARTZ_BRICKS);
        stairsBlock(AEBlocks.QUARTZ_BRICK_STAIRS, AEBlocks.QUARTZ_BRICKS);
        slabBlock(AEBlocks.QUARTZ_BRICK_SLAB, AEBlocks.QUARTZ_BRICKS);
        wall(AEBlocks.QUARTZ_BRICK_WALL, getBlockTexture(AEBlocks.QUARTZ_BRICKS));
    }

    private void smoothQuartz() {
        simpleBlockAndItem(AEBlocks.SMOOTH_QUARTZ_BLOCK);
        stairsBlock(AEBlocks.SMOOTH_QUARTZ_STAIRS, AEBlocks.SMOOTH_QUARTZ_BLOCK);
        slabBlock(AEBlocks.SMOOTH_QUARTZ_SLAB, AEBlocks.SMOOTH_QUARTZ_BLOCK);
        wall(AEBlocks.SMOOTH_QUARTZ_WALL, getBlockTexture(AEBlocks.SMOOTH_QUARTZ_BLOCK));
    }

    private void cutQuartz() {
        blockModels.blockStateOutput.accept(
                createSimpleBlock(
                        AEBlocks.CUT_QUARTZ_BLOCK.block(),
                        applyAthena(plainVariant(ModelTemplates.CUBE_ALL.create(AEBlocks.CUT_QUARTZ_BLOCK.block(),
                                TextureMapping.cube(AEBlocks.CUT_QUARTZ_BLOCK.block()), modelOutput)),
                                "athena:ctm",
                                Map.of(
                                        "center", AppEng.makeId("block/cut_quartz_block_center"),
                                        "empty", AppEng.makeId("block/smooth_quartz_block"),
                                        "horizontal", AppEng.makeId("block/cut_quartz_block_h"),
                                        "vertical", AppEng.makeId("block/cut_quartz_block_v"),
                                        "particle", AppEng.makeId("block/cut_quartz_block")))));
        stairsBlock(AEBlocks.CUT_QUARTZ_STAIRS, AEBlocks.CUT_QUARTZ_BLOCK);
        slabBlock(AEBlocks.CUT_QUARTZ_SLAB, AEBlocks.CUT_QUARTZ_BLOCK);
        wall(AEBlocks.CUT_QUARTZ_WALL, getBlockTexture(AEBlocks.CUT_QUARTZ_BLOCK));
    }

    private void quartz() {
        blockModels.blockStateOutput.accept(
                createSimpleBlock(
                        AEBlocks.QUARTZ_BLOCK.block(),
                        applyAthena(plainVariant(ModelTemplates.CUBE_ALL.create(AEBlocks.QUARTZ_BLOCK.block(),
                                TextureMapping.cube(AEBlocks.QUARTZ_BLOCK.block()), modelOutput)),
                                "athena:ctm",
                                Map.of(
                                        "center", AppEng.makeId("block/quartz_block_center"),
                                        "empty", AppEng.makeId("block/quartz_block_empty"),
                                        "horizontal", AppEng.makeId("block/quartz_block_h"),
                                        "vertical", AppEng.makeId("block/quartz_block_v"),
                                        "particle", AppEng.makeId("block/quartz_block")))));
        stairsBlock(AEBlocks.QUARTZ_STAIRS, AEBlocks.QUARTZ_BLOCK);
        slabBlock(AEBlocks.QUARTZ_SLAB, AEBlocks.QUARTZ_BLOCK);
        wall(AEBlocks.QUARTZ_WALL, getBlockTexture(AEBlocks.QUARTZ_BLOCK));
    }

    private void fluix() {
        var blockTexture = getBlockTexture(AEBlocks.FLUIX_BLOCK);

        blockModels.blockStateOutput.accept(
                createSimpleBlock(
                        AEBlocks.FLUIX_BLOCK.block(),
                        applyAthena(
                                plainVariant(ModelTemplates.CUBE_ALL.create(AEBlocks.FLUIX_BLOCK.block(),
                                        TextureMapping.cube(blockTexture),
                                        modelOutput)),
                                "athena:ctm",
                                Map.of(
                                        "center", getBlockTexture(AEBlocks.FLUIX_BLOCK, "_center"),
                                        "empty", getBlockTexture(AEBlocks.FLUIX_BLOCK, "_empty"),
                                        "horizontal", getBlockTexture(AEBlocks.FLUIX_BLOCK, "_h"),
                                        "vertical", getBlockTexture(AEBlocks.FLUIX_BLOCK, "_v"),
                                        "particle", blockTexture))));
        stairsBlock(AEBlocks.FLUIX_STAIRS, AEBlocks.FLUIX_BLOCK);
        slabBlock(AEBlocks.FLUIX_SLAB, AEBlocks.FLUIX_BLOCK);
        wall(AEBlocks.FLUIX_WALL, blockTexture);
    }

    private void chiseledQuartz() {
        var topTexture = getBlockTexture(AEBlocks.CHISELED_QUARTZ_BLOCK, "_top");
        var bottomTexture = getBlockTexture(AEBlocks.CHISELED_QUARTZ_BLOCK, "_bottom");
        var sideTexture = getBlockTexture(AEBlocks.CHISELED_QUARTZ_BLOCK, "_side");

        blockModels.blockStateOutput.accept(
                createSimpleBlock(
                        AEBlocks.CHISELED_QUARTZ_BLOCK.block(),
                        applyAthena(
                                plainVariant(ModelTemplates.CUBE_COLUMN.create(AEBlocks.CHISELED_QUARTZ_BLOCK.block(),
                                        TextureMapping.column(sideTexture, getBlockTexture(AEBlocks.CUT_QUARTZ_BLOCK)),
                                        modelOutput)),
                                "athena:limited_pillar",
                                Map.of(
                                        "bottom", bottomTexture,
                                        "center", sideTexture,
                                        "self", sideTexture,
                                        "top", topTexture,
                                        "particle", getBlockTexture(AEBlocks.CUT_QUARTZ_BLOCK)))));
        stairsBlock(AEBlocks.CHISELED_QUARTZ_STAIRS, topTexture, sideTexture, topTexture);
        slabBlock(AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_BLOCK, topTexture, sideTexture, topTexture);
        wall(AEBlocks.CHISELED_QUARTZ_WALL, sideTexture);
    }

    private void quartzFixture() {
        var block = AEBlocks.QUARTZ_FIXTURE.block();

        var standing = ModelLocationUtils.getModelLocation(block, "_standing");
        var standingOdd = ModelLocationUtils.getModelLocation(block, "_standing_odd");
        var wall = ModelLocationUtils.getModelLocation(block, "_wall");
        var wallOdd = ModelLocationUtils.getModelLocation(block, "_wall_odd");

        var dispatch = PropertyDispatch.initial(QuartzFixtureBlock.FACING, QuartzFixtureBlock.ODD);
        dispatch.select(Direction.DOWN, false,
                plainVariant(standing).with(VariantMutator.X_ROT.withValue(Quadrant.R180)));
        dispatch.select(Direction.DOWN, true,
                plainVariant(standingOdd).with(VariantMutator.X_ROT.withValue(Quadrant.R180)));
        dispatch.select(Direction.EAST, false, plainVariant(wall).with(VariantMutator.Y_ROT.withValue(Quadrant.R90)));
        dispatch.select(Direction.EAST, true, plainVariant(wallOdd).with(VariantMutator.Y_ROT.withValue(Quadrant.R90)));
        dispatch.select(Direction.NORTH, false, plainVariant(wall));
        dispatch.select(Direction.NORTH, true, plainVariant(wallOdd));
        dispatch.select(Direction.SOUTH, false, plainVariant(wall).with(VariantMutator.Y_ROT.withValue(Quadrant.R180)));
        dispatch.select(Direction.SOUTH, true,
                plainVariant(wallOdd).with(VariantMutator.Y_ROT.withValue(Quadrant.R180)));
        dispatch.select(Direction.UP, false, plainVariant(standing));
        dispatch.select(Direction.UP, true, plainVariant(standingOdd));
        dispatch.select(Direction.WEST, false, plainVariant(wall).with(VariantMutator.Y_ROT.withValue(Quadrant.R270)));
        dispatch.select(Direction.WEST, true,
                plainVariant(wallOdd).with(VariantMutator.Y_ROT.withValue(Quadrant.R270)));

        multiVariantGenerator(AEBlocks.QUARTZ_FIXTURE, dispatch);
        multiVariantGenerator(AEBlocks.LIGHT_DETECTOR, dispatch);

        // Special item model
        blockModels.registerSimpleItemModel(
                AEBlocks.QUARTZ_FIXTURE.asItem(),
                ModelLocationUtils.getModelLocation(AEBlocks.QUARTZ_FIXTURE.asItem()));
        blockModels.registerSimpleItemModel(
                AEBlocks.LIGHT_DETECTOR.asItem(),
                ModelLocationUtils.getModelLocation(AEBlocks.QUARTZ_FIXTURE.asItem()));
    }

    private MultiVariant applyAthena(MultiVariant delegate, String loader,
            Map<String, ResourceLocation> ctmTextures) {
        /*
         * {
         * 
         * @Override public JsonElement get() { var root = (JsonObject) delegate.get();
         * 
         * var ctmTexturesObj = new JsonObject(); for (var entry : ctmTextures.entrySet()) {
         * ctmTexturesObj.addProperty(entry.getKey(), entry.getValue().toString()); }
         * 
         * root.addProperty("athena:loader", loader); root.add("ctm_textures", ctmTexturesObj); return root; } };
         */

        // TODO 1.21.5: Once Athena ports, we need to check how to apply CTM again
        return delegate;
    }

    private void wall(BlockDefinition<? extends WallBlock> blockDef, ResourceLocation texture) {
        var block = blockDef.block();

        var textures = new TextureMapping().put(TextureSlot.WALL, texture);

        var lowSideModel = plainVariant(ModelTemplates.WALL_LOW_SIDE.create(block, textures, modelOutput));
        var postModel = plainVariant(ModelTemplates.WALL_POST.create(block, textures, modelOutput));
        var tallSideModel = plainVariant(ModelTemplates.WALL_TALL_SIDE.create(block, textures, modelOutput));

        blockModels.blockStateOutput.accept(createWall(
                block,
                postModel,
                lowSideModel,
                tallSideModel));

        var invModel = ModelTemplates.WALL_INVENTORY.create(block, textures, modelOutput);
        blockModels.registerSimpleItemModel(block, invModel);
    }

    private void slabBlock(BlockDefinition<? extends SlabBlock> blockDef, BlockDefinition<?> baseBlock) {
        var texture = getBlockTexture(baseBlock);
        slabBlock(blockDef, baseBlock, texture, texture, texture);
    }

    private void slabBlock(BlockDefinition<? extends SlabBlock> blockDef, BlockDefinition<?> doubleModelDonor,
            ResourceLocation topTexture, ResourceLocation sideTexture, ResourceLocation bottomTexture) {
        var block = blockDef.block();

        var textures = new TextureMapping()
                .put(TextureSlot.TOP, topTexture)
                .put(TextureSlot.BOTTOM, bottomTexture)
                .put(TextureSlot.SIDE, sideTexture);

        var topModel = plainVariant(ModelTemplates.SLAB_TOP.create(block, textures, modelOutput));
        var bottomModel = ModelTemplates.SLAB_BOTTOM.create(block, textures, modelOutput);

        blockModels.blockStateOutput.accept(createSlab(
                block,
                plainVariant(bottomModel),
                topModel,
                plainVariant(ModelLocationUtils.getModelLocation(doubleModelDonor.block()))));

        blockModels.registerSimpleItemModel(block, bottomModel);
    }

    private void stairsBlock(BlockDefinition<? extends StairBlock> stairsBlock, BlockDefinition<?> templateBlock) {
        var blockTexture = getBlockTexture(templateBlock);
        stairsBlock(stairsBlock, blockTexture, blockTexture, blockTexture);
    }

    private void stairsBlock(BlockDefinition<? extends StairBlock> blockDef, ResourceLocation topTexture,
            ResourceLocation sideTexture, ResourceLocation bottomTexture) {
        var block = blockDef.block();

        var textures = new TextureMapping()
                .put(TextureSlot.TOP, topTexture)
                .put(TextureSlot.BOTTOM, bottomTexture)
                .put(TextureSlot.SIDE, sideTexture);

        var straightModel = ModelTemplates.STAIRS_STRAIGHT.create(block, textures, modelOutput);
        var innerModel = ModelTemplates.STAIRS_INNER.create(block, textures, modelOutput);
        var outerModel = ModelTemplates.STAIRS_OUTER.create(block, textures, modelOutput);

        blockModels.blockStateOutput.accept(createStairs(
                block,
                plainVariant(innerModel),
                plainVariant(straightModel),
                plainVariant(outerModel)));
        blockModels.registerSimpleItemModel(block, straightModel);
    }
}
