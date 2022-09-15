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

package appeng.block.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.client.model.data.ModelData;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusBreakingParticle;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.helpers.AEMaterials;
import appeng.integration.abstraction.IAEFacade;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.util.Platform;

public class CableBusBlock extends AEBaseEntityBlock<CableBusBlockEntity> implements IAEFacade, SimpleWaterloggedBlock {

    private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();

    private static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CableBusBlock() {
        super(defaultProps(AEMaterials.GLASS).noOcclusion().noLootTable().dynamicShape()
                .lightLevel(state -> state.getValue(LIGHT_LEVEL)));
        registerDefaultState(defaultBlockState().setValue(LIGHT_LEVEL, 0).setValue(WATERLOGGED, false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CableBusBlockEntity bus) {
            var drops = new ArrayList<ItemStack>();
            bus.getCableBus().addPartDrops(drops);
            return drops;
        } else {
            return List.of();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        this.cb(level, pos).animateTick(level, pos, rand);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        this.cb(level, pos).onNeighborChanged(level, pos, neighbor);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.cb(level, pos).isProvidingWeakPower(side.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
        this.cb(level, pos).onEntityCollision(entityIn);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos,
            Direction side) {
        return this.cb(level, pos).isProvidingStrongPower(side.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL, WATERLOGGED);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return this.cb(level, pos).isLadder(entity);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        // FIXME: Potentially check the fluid one too
        return super.canBeReplaced(state, useContext)
                && this.cb(useContext.getLevel(), useContext.getClickedPos()).isEmpty();
    }

    // TODO-1.17 This hook was removed from Forge with replacement and may be unnecessary
    public boolean canConnectRedstone(BlockGetter level, BlockPos pos, Direction side) {
        // TODO: Verify this.
        if (side == null) {
            return false;
        }

        return this.cb(level, pos).canConnectRedstone(side.getOpposite());
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
            Player player) {
        var v3 = target.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        var sp = this.cb(level, pos).selectPartLocal(v3);

        if (sp.part != null) {
            return new ItemStack(sp.part.getPartItem());
        } else if (sp.facade != null) {
            return sp.facade.getItemStack();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        if (!level.isClientSide()) {
            this.cb(level, pos).onNeighborChanged(level, pos, fromPos);
        }
    }

    private ICableBusContainer cb(BlockGetter level, BlockPos pos) {
        final BlockEntity te = level.getBlockEntity(pos);
        ICableBusContainer out = null;

        if (te instanceof CableBusBlockEntity) {
            out = ((CableBusBlockEntity) te).getCableBus();
        }

        return out == null ? NULL_CABLE_BUS : out;
    }

    @Nullable
    private IFacadeContainer fc(BlockGetter level, BlockPos pos) {
        final BlockEntity te = level.getBlockEntity(pos);
        IFacadeContainer out = null;

        if (te instanceof CableBusBlockEntity) {
            out = ((CableBusBlockEntity) te).getCableBus().getFacadeContainer();
        }

        return out;
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player player,
            InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        // Transform from world into block space
        Vec3 hitVec = hit.getLocation();
        Vec3 hitInBlock = new Vec3(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
        return this.cb(level, pos).activate(player, hand, hitInBlock)
                ? InteractionResult.sidedSuccess(level.isClientSide())
                : InteractionResult.PASS;
    }

    public boolean recolorBlock(BlockGetter level, BlockPos pos, Direction side,
            DyeColor color, Player who) {
        try {
            return this.cb(level, pos).recolourBlock(side, AEColor.values()[color.ordinal()], who);
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> itemStacks) {
        // do nothing
    }

    @Override
    public BlockState getFacadeState(BlockGetter level, BlockPos pos, Direction side) {
        if (side != null) {
            IFacadeContainer container = this.fc(level, pos);
            if (container != null) {
                IFacadePart facade = container.getFacade(side);
                if (facade != null) {
                    return facade.getBlockState();
                }
            }
        }
        return level.getBlockState(pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        CableBusBlockEntity te = getBlockEntity(level, pos);
        if (te == null) {
            return Shapes.empty();
        } else {
            return te.getCableBus().getShape();
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        CableBusBlockEntity te = getBlockEntity(level, pos);
        if (te == null) {
            return Shapes.empty();
        } else {
            return te.getCableBus().getCollisionShape(context);
        }
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, CableBusBlockEntity be) {
        if (currentState.getBlock() != this) {
            return currentState;
        }
        int lightLevel = be.getCableBus().getLightValue();
        return super.updateBlockStateFromBlockEntity(currentState, be).setValue(LIGHT_LEVEL, lightLevel);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(Level level, BlockPos pos) {
        var fluidState = level.getFluidState(pos);
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        if (blockState.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(blockState, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {

            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target,
                    ParticleEngine effectRenderer) {

                // Half the particle rate. Since we're spawning concentrated on a specific spot,
                // our particle effect otherwise looks too strong
                if (Platform.getRandom().nextBoolean()) {
                    return true;
                }

                if (target.getType() != Type.BLOCK) {
                    return false;
                }
                BlockPos blockPos = new BlockPos(target.getLocation().x, target.getLocation().y,
                        target.getLocation().z);

                ICableBusContainer cb = cb(level, blockPos);

                // Our built-in model has the actual baked sprites we need
                BakedModel model = Minecraft.getInstance().getBlockRenderer()
                        .getBlockModel(defaultBlockState());

                // We cannot add the effect if we don't have the model
                if (!(model instanceof CableBusBakedModel cableBusModel)) {
                    return true;
                }

                CableBusRenderState renderState = cb.getRenderState();

                // Spawn a particle for one of the particle textures
                TextureAtlasSprite texture = Platform.pickRandom(cableBusModel.getParticleTextures(renderState));
                if (texture != null) {
                    double x = target.getLocation().x;
                    double y = target.getLocation().y;
                    double z = target.getLocation().z;
                    // FIXME: Check how this looks, probably like shit, maybe provide parts the
                    // ability to supply particle textures???
                    effectRenderer.add(
                            new CableBusBreakingParticle((ClientLevel) level, x, y, z, texture).scale(0.8F));
                }

                return true;
            }

            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos,
                    ParticleEngine effectRenderer) {
                ICableBusContainer cb = cb(level, pos);

                // Our built-in model has the actual baked sprites we need
                BakedModel model = Minecraft.getInstance().getBlockRenderer()
                        .getBlockModel(defaultBlockState());

                // We cannot add the effect if we dont have the model
                if (!(model instanceof CableBusBakedModel cableBusModel)) {
                    return true;
                }

                CableBusRenderState renderState = cb.getRenderState();

                List<TextureAtlasSprite> textures = cableBusModel.getParticleTextures(renderState);

                if (!textures.isEmpty()) {
                    // Shamelessly inspired by ParticleManager.addBlockDestroyEffects
                    for (int j = 0; j < 4; ++j) {
                        for (int k = 0; k < 4; ++k) {
                            for (int l = 0; l < 4; ++l) {
                                // Randomly select one of the textures if the cable bus has more than just one
                                // possibility here
                                final TextureAtlasSprite texture = Platform.pickRandom(textures);

                                final double x = pos.getX() + (j + 0.5D) / 4.0D;
                                final double y = pos.getY() + (k + 0.5D) / 4.0D;
                                final double z = pos.getZ() + (l + 0.5D) / 4.0D;

                                // FIXME: Check how this looks, probably like shit, maybe provide parts the
                                // ability to supply particle textures???
                                Particle effect = new CableBusBreakingParticle((ClientLevel) level, x, y, z,
                                        x - pos.getX() - 0.5D, y - pos.getY() - 0.5D, z - pos.getZ() - 0.5D, texture);
                                effectRenderer.add(effect);
                            }
                        }
                    }
                }

                return true;
            }
        });
    }

    /**
     * WTF does this do you ask?
     * <p>
     * Well, this is needed to properly handle adjacent facades, as we need to know on which side the source facade is.
     * The cases to handle are:
     * <ul>
     * <li>Not rendering a facade: then we just look at the facade on the requested side.</li>
     * <li>Rendering a facade and the requested side is the opposite: then this is likely an interior quad of the
     * facade, so we actually check the rendering side instead of the requested side.</li>
     * <li>Rendering a facade in other cases: check requested side first, otherwise check rendering side since we might
     * also connect to a facade in another direction due to the 2 extra pixels on the side of a facade.</li>
     * </ul>
     */
    public static ThreadLocal<Direction> RENDERING_FACADE_DIRECTION = new ThreadLocal<>();

    // TODO: fix ugliness when forge finally merges #9066
    // @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side,
            @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        // Just for access to the model data from the BE's level (waiting for #9066 to get it from renderView directly)
        var be = renderView.getBlockEntity(pos);
        if (be == null) {
            return state;
        }
        ModelData modelData;
        var modelDataManager = be.getLevel().getModelDataManager();
        if (modelDataManager == null) {
            // We're on the server, use BE directly
            modelData = be.getModelData();
        } else {
            modelData = Objects.requireNonNullElse(modelDataManager.getAt(pos), ModelData.EMPTY);
        }

        CableBusRenderState cableBusRenderState = modelData.get(CableBusRenderState.PROPERTY);
        if (cableBusRenderState != null) {
            var renderingFacadeDir = RENDERING_FACADE_DIRECTION.get();
            var facades = cableBusRenderState.getFacades();

            if (side.getOpposite() != renderingFacadeDir) {
                var facadeState = facades.get(side);
                if (facadeState != null) {
                    return facadeState.getSourceBlock();
                }
            }

            if (renderingFacadeDir != null && facades.containsKey(renderingFacadeDir)) {
                return facades.get(renderingFacadeDir).getSourceBlock();
            }
        }
        return state;
    }

    // Just for query until #9066 is merged
    public static BlockState getAppearanceTemp(BlockState state, BlockAndTintGetter renderView, BlockPos pos,
            Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        if (state.getBlock() instanceof CableBusBlock cableBus) {
            return cableBus.getAppearance(state, renderView, pos, side, sourceState, sourcePos);
        } else {
            return state;
        }
    }
}
