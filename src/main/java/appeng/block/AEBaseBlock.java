/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.block;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import appeng.api.util.IOrientableBlock;
import appeng.block.orientation.IOrientationStrategy;
import appeng.block.orientation.OrientationStrategies;
import appeng.helpers.AEMaterials;

public abstract class AEBaseBlock extends Block implements IOrientableBlock {

    protected AEBaseBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.none();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        for (var property : getOrientationStrategy().getProperties()) {
            builder.add(property);
        }
    }

    /**
     * Utility function to create block properties with some sensible defaults for AE blocks.
     */
    public static FabricBlockSettings defaultProps(Material material) {
        return defaultProps(material, material.getColor());
    }

    /**
     * Utility function to create block properties with some sensible defaults for AE blocks.
     */
    public static FabricBlockSettings defaultProps(Material material, MaterialColor color) {
        return FabricBlockSettings.of(material, color)
                // These values previously were encoded in AEBaseBlock
                .strength(2.2f, 11.f)
                .sounds(getDefaultSoundByMaterial(material));
    }

    private static SoundType getDefaultSoundByMaterial(Material mat) {
        if (mat == AEMaterials.GLASS || mat == Material.GLASS) {
            return SoundType.GLASS;
        } else if (mat == Material.STONE) {
            return SoundType.STONE;
        } else if (mat == Material.WOOD) {
            return SoundType.WOOD;
        } else {
            return SoundType.METAL;
        }
    }

    public void addToMainCreativeTab(CreativeModeTab.Output output) {
        output.accept(this);
    }

    @Override
    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        var id = BuiltInRegistries.BLOCK.getKey(this);
        return id != BuiltInRegistries.BLOCK.getDefaultKey() ? id : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = this.defaultBlockState();
        return getOrientationStrategy().getStateForPlacement(state, context);
    }

    //
//    /**
//     * Called when a player uses a wrench on this block entity to rotate it.
//     */
//    public InteractionResult rotateWithWrench(Player player, Level level, BlockHitResult hitResult) {
//        BlockPos pos = hitResult.getBlockPos();
//
//        var block = getBlockState().getBlock();
//        if (block instanceof AEBaseBlock aeBlock) {
//            if (aeBlock.rotateAroundFaceAxis(level, pos, hitResult.getDirection())) {
//                return InteractionResult.sidedSuccess(level.isClientSide());
//            }
//        }
//
//        return InteractionResult.PASS;
//    }
//    /**
//     * TODO: 1.17 Refactor to use Block#onBlockPlacedBy(), BlockItem#setTileEntityNBT() or equivalent.
//     */
//    @Override
//    public InteractionResult place(BlockPlaceContext context) {
//
//        Direction up = null;
//        Direction forward = null;
//
//        Direction side = context.getClickedFace();
//        Player player = context.getPlayer();
//
//        if (this.blockType instanceof AEBaseEntityBlock) {
//            if (this.blockType instanceof LightDetectorBlock) {
//                up = side;
//                if (up == Direction.UP || up == Direction.DOWN) {
//                    forward = Direction.SOUTH;
//                } else {
//                    forward = Direction.UP;
//                }
//            } else if (this.blockType instanceof WirelessBlock) {
//                forward = side;
//                if (forward == Direction.UP || forward == Direction.DOWN) {
//                    up = Direction.SOUTH;
//                } else {
//                    up = Direction.UP;
//                }
//            } else {
//                up = Direction.UP;
//                forward = context.getHorizontalDirection().getOpposite();
//
//                if (player != null) {
//                    if (player.getXRot() > 65) {
//                        up = forward.getOpposite();
//                        forward = Direction.UP;
//                    } else if (player.getXRot() < -65) {
//                        up = forward.getOpposite();
//                        forward = Direction.DOWN;
//                    }
//                }
//            }
//        }
//
//        IOrientable ori = null;
//        if (this.blockType instanceof IOrientableBlock) {
//            ori = ((IOrientableBlock) this.blockType).getOrientable(context.getLevel(), context.getClickedPos());
//            up = side;
//            forward = Direction.SOUTH;
//            if (up.getStepY() == 0) {
//                forward = Direction.UP;
//            }
//        }
//
//        if (!this.blockType.isValidOrientation(context.getLevel(), context.getClickedPos(), forward, up)) {
//            return InteractionResult.FAIL;
//        }
//
//        InteractionResult result = super.place(context);
//        if (!result.consumesAction()) {
//            return result;
//        }
//
//        if (this.blockType instanceof AEBaseEntityBlock && !(this.blockType instanceof LightDetectorBlock)) {
//            final AEBaseBlockEntity blockEntity = ((AEBaseEntityBlock<?>) this.blockType).getBlockEntity(
//                    context.getLevel(),
//                    context.getClickedPos());
//            ori = blockEntity;
//
//            if (blockEntity == null) {
//                return result;
//            }
//
//            if (ori.canBeRotated() && !this.blockType.hasCustomRotation()) {
//                ori.setOrientation(forward, up);
//            }
//
//            if (blockEntity instanceof IOwnerAwareBlockEntity ownerAware) {
//                ownerAware.setOwner(player);
//            }
//        } else if (this.blockType instanceof IOrientableBlock) {
//            ori.setOrientation(forward, up);
//        }
//
//        return result;
//
//    }
}
