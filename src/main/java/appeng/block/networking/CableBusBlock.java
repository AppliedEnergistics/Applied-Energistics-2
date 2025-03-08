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
import java.util.function.Consumer;

import appeng.util.InteractionUtil;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusBreakingParticle;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.integration.abstraction.IAEFacade;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;

public class CableBusBlock extends AEBaseEntityBlock<CableBusBlockEntity> implements IAEFacade, SimpleWaterloggedBlock {

    private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();

    private static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CableBusBlock(Properties p) {
        super(glassProps(p).noOcclusion().noLootTable().dynamicShape().forceSolidOn()
                .lightLevel(state -> state.getValue(LIGHT_LEVEL)));
        registerDefaultState(defaultBlockState().setValue(LIGHT_LEVEL, 0).setValue(WATERLOGGED, false));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
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

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
            @Nullable Direction side) {
        if (side == null) {
            return false;
        }

        return this.cb(level, pos).canConnectRedstone(side.getOpposite());
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        var playerRay = InteractionUtil.getPlayerRay(player, 100);

        // TODO 1.21.4 bad emulation of client-side hit result
        var collisionShape = state.getCollisionShape(level, pos);
        var hitResult = collisionShape.clip(playerRay.getA(), playerRay.getB(), pos);
        if (hitResult == null) {
            return ItemStack.EMPTY;
        }

        var v3 = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        var sp = this.cb(level, pos).selectPartLocal(v3);

        if (sp.part != null) {
            return new ItemStack(sp.part.getPartItem());
        } else if (sp.facade != null) {
            return sp.facade.getItemStack();
        }

        return ItemStack.EMPTY;
    }

    ICableBusContainer cb(BlockGetter level, BlockPos pos) {
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
    protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        // Transform from world into block space
        Vec3 hitVec = hit.getLocation();
        Vec3 hitInBlock = new Vec3(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
        return this.cb(level, pos).useItemOn(heldItem, player, hand, hitInBlock)
                ? InteractionResult.SUCCESS
                : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        // Transform from world into block space
        Vec3 hitVec = hitResult.getLocation();
        Vec3 hitInBlock = new Vec3(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
        return this.cb(level, pos).useWithoutItem(player, hitInBlock)
                ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }

    public boolean recolorBlock(BlockGetter level, BlockPos pos, Direction side,
            DyeColor color, Player who) {
        try {
            return this.cb(level, pos).recolourBlock(side, AEColor.fromDye(color), who);
        } catch (Throwable ignored) {
        }
        return false;
    }

    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
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
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        this.cb(level, pos).onUpdateShape(level, pos, direction);

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        this.cb(level, pos).onNeighborChanged(level, pos, neighbor);
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

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side,
            @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        ModelData modelData;
        if (renderView instanceof ServerLevel serverLevel) {
            // We're on the server, use BE directly
            BlockEntity be = renderView.getBlockEntity(pos);
            modelData = be != null ? be.getModelData() : ModelData.EMPTY;
        } else {
            modelData = renderView.getModelData(pos);
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
}
