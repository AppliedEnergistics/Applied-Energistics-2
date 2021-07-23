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

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusBreakingParticle;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.helpers.AEMaterials;
import appeng.integration.abstraction.IAEFacade;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.AEBaseTileEntity;
import appeng.tile.networking.CableBusTileEntity;
import appeng.util.Platform;

public class CableBusBlock extends AEBaseTileBlock<CableBusTileEntity> implements IAEFacade, IWaterLoggable {

    private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();

    private static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CableBusBlock() {
        super(defaultProps(AEMaterials.GLASS).noOcclusion().noDrops().dynamicShape()
                .lightLevel(state -> state.getValue(LIGHT_LEVEL)));
        registerDefaultState(defaultBlockState().setValue(LIGHT_LEVEL, 0).setValue(WATERLOGGED, false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public void animateTick(final BlockState state, final World worldIn, final BlockPos pos, final Random rand) {
        this.cb(worldIn, pos).animateTick(worldIn, pos, rand);
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader w, BlockPos pos, BlockPos neighbor) {
        this.cb(w, pos).onNeighborChanged(w, pos, neighbor);
    }

    @Override
    public int getSignal(final BlockState state, final IBlockReader w, final BlockPos pos, final Direction side) {
        return this.cb(w, pos).isProvidingWeakPower(side.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    public boolean isSignalSource(final BlockState state) {
        return true;
    }

    @Override
    public void entityInside(BlockState state, World w, BlockPos pos, Entity entityIn) {
        this.cb(w, pos).onEntityCollision(entityIn);
    }

    @Override
    public int getDirectSignal(final BlockState state, final IBlockReader w, final BlockPos pos, final Direction side) {
        return this.cb(w, pos).isProvidingStrongPower(side.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL, WATERLOGGED);
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return this.cb(world, pos).isLadder(entity);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext) {
        // FIXME: Potentially check the fluid one too
        return super.canBeReplaced(state, useContext) && this.cb(useContext.getLevel(), useContext.getClickedPos()).isEmpty();
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player,
            boolean willHarvest, FluidState fluid) {
        if (player.abilities.instabuild) {
            final AEBaseTileEntity tile = this.getTileEntity(world, pos);
            if (tile != null) {
                tile.disableDrops();
            }
            // maybe ray trace?
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public boolean canConnectRedstone(final BlockState state, final IBlockReader w, final BlockPos pos,
            Direction side) {
        // TODO: Verify this.
        if (side == null) {
            return false;
        }

        return this.cb(w, pos).canConnectRedstone(side.getOpposite());
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
            PlayerEntity player) {
        final Vector3d v3 = target.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        final SelectedPart sp = this.cb(world, pos).selectPart(v3);

        if (sp.part != null) {
            return sp.part.getItemStack(PartItemStack.PICK);
        } else if (sp.facade != null) {
            return sp.facade.getItemStack();
        }

        return ItemStack.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(final BlockState state, final World world, final RayTraceResult target,
            final ParticleManager effectRenderer) {

        // Half the particle rate. Since we're spawning concentrated on a specific spot,
        // our particle effect otherwise looks too strong
        if (Platform.getRandom().nextBoolean()) {
            return true;
        }

        if (target.getType() != Type.BLOCK) {
            return false;
        }
        BlockPos blockPos = new BlockPos(target.getLocation().x, target.getLocation().y, target.getLocation().z);

        ICableBusContainer cb = this.cb(world, blockPos);

        // Our built-in model has the actual baked sprites we need
        IBakedModel model = Minecraft.getInstance().getBlockRenderer()
                .getBlockModel(this.defaultBlockState());

        // We cannot add the effect if we don't have the model
        if (!(model instanceof CableBusBakedModel)) {
            return true;
        }

        CableBusBakedModel cableBusModel = (CableBusBakedModel) model;

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
                    new CableBusBreakingParticle((ClientWorld) world, x, y, z, texture).scale(0.8F));
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager effectRenderer) {
        ICableBusContainer cb = this.cb(world, pos);

        // Our built-in model has the actual baked sprites we need
        IBakedModel model = Minecraft.getInstance().getBlockRenderer()
                .getBlockModel(this.defaultBlockState());

        // We cannot add the effect if we dont have the model
        if (!(model instanceof CableBusBakedModel)) {
            return true;
        }

        CableBusBakedModel cableBusModel = (CableBusBakedModel) model;

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
                        Particle effect = new CableBusBreakingParticle((ClientWorld) world, x, y, z,
                                x - pos.getX() - 0.5D, y - pos.getY() - 0.5D, z - pos.getZ() - 0.5D, texture);
                        effectRenderer.add(effect);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        if (!world.isClientSide()) {
            this.cb(world, pos).onNeighborChanged(world, pos, fromPos);
        }
    }

    private ICableBusContainer cb(final IBlockReader w, final BlockPos pos) {
        final TileEntity te = w.getBlockEntity(pos);
        ICableBusContainer out = null;

        if (te instanceof CableBusTileEntity) {
            out = ((CableBusTileEntity) te).getCableBus();
        }

        return out == null ? NULL_CABLE_BUS : out;
    }

    @Nullable
    private IFacadeContainer fc(final IBlockReader w, final BlockPos pos) {
        final TileEntity te = w.getBlockEntity(pos);
        IFacadeContainer out = null;

        if (te instanceof CableBusTileEntity) {
            out = ((CableBusTileEntity) te).getCableBus().getFacadeContainer();
        }

        return out;
    }

    @Override
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isClientSide()) {
            final RayTraceResult rtr = Minecraft.getInstance().hitResult;
            if (rtr instanceof BlockRayTraceResult) {
                BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
                if (brtr.getBlockPos().equals(pos)) {
                    final Vector3d hitVec = rtr.getLocation().subtract(new Vector3d(pos.getX(), pos.getY(), pos.getZ()));

                    if (this.cb(worldIn, pos).clicked(player, Hand.MAIN_HAND, hitVec)) {
                        NetworkHandler.instance().sendToServer(new ClickPacket(pos, brtr.getDirection(), (float) hitVec.x,
                                (float) hitVec.y, (float) hitVec.z, Hand.MAIN_HAND, true));
                    }
                }
            }
        }
    }

    public void onBlockClickPacket(World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, Vector3d hitVec) {
        this.cb(worldIn, pos).clicked(playerIn, hand, hitVec);
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        // Transform from world into block space
        Vector3d hitVec = hit.getLocation();
        Vector3d hitInBlock = new Vector3d(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
        return this.cb(w, pos).activate(player, hand, hitInBlock)
                ? ActionResultType.sidedSuccess(w.isClientSide())
                : ActionResultType.PASS;
    }

    public boolean recolorBlock(final IBlockReader world, final BlockPos pos, final Direction side,
            final DyeColor color, final PlayerEntity who) {
        try {
            return this.cb(world, pos).recolourBlock(side, AEColor.values()[color.ordinal()], who);
        } catch (final Throwable ignored) {
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> itemStacks) {
        // do nothing
    }

    @Override
    public BlockState getFacadeState(IBlockReader world, BlockPos pos, Direction side) {
        if (side != null) {
            IFacadeContainer container = this.fc(world, pos);
            if (container != null) {
                IFacadePart facade = container.getFacade(AEPartLocation.fromFacing(side));
                if (facade != null) {
                    return facade.getBlockState();
                }
            }
        }
        return world.getBlockState(pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader w, BlockPos pos, ISelectionContext context) {
        CableBusTileEntity te = getTileEntity(w, pos);
        if (te == null) {
            return VoxelShapes.empty();
        } else {
            return te.getCableBus().getShape();
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader w, BlockPos pos, ISelectionContext context) {
        CableBusTileEntity te = getTileEntity(w, pos);
        if (te == null) {
            return VoxelShapes.empty();
        } else {
            return te.getCableBus().getCollisionShape(context.getEntity());
        }
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, CableBusTileEntity te) {
        if (currentState.getBlock() != this) {
            return currentState;
        }
        int lightLevel = te.getCableBus().getLightValue();
        return super.updateBlockStateFromTileEntity(currentState, te).setValue(LIGHT_LEVEL, lightLevel);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        BlockState blockState = this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return blockState;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, IWorld world,
            BlockPos currentPos, BlockPos facingPos) {
        if (blockState.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER,
                    Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(blockState, facing, facingState, world, currentPos, facingPos);
    }

}
