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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

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
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.helpers.AEMaterials;
import appeng.integration.abstraction.IAEFacade;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.networking.CableBusBlockEntity;
import appeng.util.Platform;

public class CableBusBlock extends AEBaseTileBlock<CableBusBlockEntity> implements IAEFacade {

    private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();

    private static final IntProperty LIGHT_LEVEL = IntProperty.of("light_level", 0, 15);

    public CableBusBlock() {
        super(defaultProps(AEMaterials.GLASS).nonOpaque().dropsNothing().dynamicBounds()
                .luminance(state -> state.get(LIGHT_LEVEL)));
        setDefaultState(getDefaultState().with(LIGHT_LEVEL, 0));
    }

    static {
        ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
            if (result instanceof BlockHitResult) {
                BlockHitResult blockResult = (BlockHitResult) result;
                BlockState blockState = player.world.getBlockState(((BlockHitResult) result).getBlockPos());
                if (blockState.getBlock() instanceof CableBusBlock) {
                    CableBusBlock cableBus = (CableBusBlock) blockState.getBlock();
                    return cableBus.getPickBlock(blockState, result, player.world, blockResult.getBlockPos(), player);
                }
            }
            return ItemStack.EMPTY;
        });
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(final BlockState state, final World worldIn, final BlockPos pos, final Random rand) {
        this.cb(worldIn, pos).randomDisplayTick(worldIn, pos, rand);
    }

    @Override
    public int getWeakRedstonePower(final BlockState state, final BlockView w, final BlockPos pos,
            final Direction side) {
        return this.cb(w, pos).isProvidingWeakPower(side.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    public boolean emitsRedstonePower(final BlockState state) {
        return true;
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entityIn) {
        this.cb(w, pos).onEntityCollision(entityIn);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.cb(world, pos).isProvidingStrongPower(direction.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIGHT_LEVEL);
    }

    // FIXME: Must hook isClimbing ourselves
// FIXME FABRIC    @Override
// FIXME FABRIC    public boolean isLadder(BlockState state, WorldView world, BlockPos pos, LivingEntity entity) {
// FIXME FABRIC        return this.cb(world, pos).isLadder(entity);
// FIXME FABRIC }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        // FIXME: Potentially check the fluid one too
        return super.canReplace(state, context) && this.cb(context.getWorld(), context.getBlockPos()).isEmpty();
    }

    // We drop the parts and the facades here, and the contents of the parts are handled by the block entity.
    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state)
                .build(LootContextTypes.BLOCK);
        BlockEntity be = lootContext.get(LootContextParameters.BLOCK_ENTITY);
        if (be instanceof CableBusBlockEntity) {
            CableBusBlockEntity bus = (CableBusBlockEntity) be;
            List<ItemStack> drops = new ArrayList<>();
            bus.getCableBus().appendPartStacks(drops);
            return drops;
        } else {
            AELog.warn("The block entity was either null or of the wrong type! Skipped cable bus drops!");
            return Collections.emptyList();
        }
    }

// FIXME FABRIC    @Override
// FIXME FABRIC    public boolean canConnectRedstone(final BlockState state, final BlockView w, final BlockPos pos,
// FIXME FABRIC            Direction side) {
// FIXME FABRIC        // TODO: Verify this.
// FIXME FABRIC        if (side == null) {
// FIXME FABRIC            return false;
// FIXME FABRIC        }
// FIXME FABRIC
// FIXME FABRIC        return this.cb(w, pos).canConnectRedstone(side.getOpposite());
// FIXME FABRIC    }

    public ItemStack getPickBlock(BlockState state, HitResult target, BlockView world, BlockPos pos,
            PlayerEntity player) {
        final Vec3d v3 = target.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        final SelectedPart sp = this.cb(world, pos).selectPart(v3);

        if (sp.part != null) {
            return sp.part.getItemStack(PartItemStack.PICK);
        } else if (sp.facade != null) {
            return sp.facade.getItemStack();
        }

        return ItemStack.EMPTY;
    }

    // FIXME FABRIC MIXIN
    // net.minecraft.client.particle.ParticleManager.addBlockBreakingParticles
    @Environment(EnvType.CLIENT)
    public boolean addHitEffects(final BlockState state, final World world, final HitResult target,
            final ParticleManager effectRenderer) {

        // Half the particle rate. Since we're spawning concentrated on a specific spot,
        // our particle effect otherwise looks too strong
        if (Platform.getRandom().nextBoolean()) {
            return true;
        }

        if (target.getType() != Type.BLOCK) {
            return false;
        }
        BlockPos blockPos = new BlockPos(target.getPos().x, target.getPos().y, target.getPos().z);

        ICableBusContainer cb = this.cb(world, blockPos);

        // Our built-in model has the actual baked sprites we need
        BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(this.getDefaultState());

        // We cannot add the effect if we don't have the model
        if (!(model instanceof CableBusBakedModel)) {
            return true;
        }

        CableBusBakedModel cableBusModel = (CableBusBakedModel) model;

        CableBusRenderState renderState = cb.getRenderState();

        // Spawn a particle for one of the particle textures
        Sprite texture = Platform.pickRandom(cableBusModel.getParticleTextures(renderState));
        if (texture != null) {
            double x = target.getPos().x;
            double y = target.getPos().y;
            double z = target.getPos().z;
            // FIXME: Check how this looks, probably like shit, maybe provide parts the
            // ability to supply particle textures???
            effectRenderer.addParticle(new CableBusBreakingParticle((ClientWorld) world, x, y, z, texture).scale(0.8F));
        }

        return true;
    }

    // FIXME FABRIC: Mixin to
    // net.minecraft.client.particle.ParticleManager.addBlockBreakParticles
    @Environment(EnvType.CLIENT)
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager effectRenderer) {
        ICableBusContainer cb = this.cb(world, pos);

        // Our built-in model has the actual baked sprites we need
        BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(this.getDefaultState());

        // We cannot add the effect if we dont have the model
        if (!(model instanceof CableBusBakedModel)) {
            return true;
        }

        CableBusBakedModel cableBusModel = (CableBusBakedModel) model;

        CableBusRenderState renderState = cb.getRenderState();

        List<Sprite> textures = cableBusModel.getParticleTextures(renderState);

        if (!textures.isEmpty()) {
            // Shamelessly inspired by ParticleManager.addBlockDestroyEffects
            for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 4; ++k) {
                    for (int l = 0; l < 4; ++l) {
                        // Randomly select one of the textures if the cable bus has more than just one
                        // possibility here
                        final Sprite texture = Platform.pickRandom(textures);

                        final double x = pos.getX() + (j + 0.5D) / 4.0D;
                        final double y = pos.getY() + (k + 0.5D) / 4.0D;
                        final double z = pos.getZ() + (l + 0.5D) / 4.0D;

                        // FIXME: Check how this looks, probably like shit, maybe provide parts the
                        // ability to supply particle textures???
                        Particle effect = new CableBusBreakingParticle((ClientWorld) world, x, y, z,
                                x - pos.getX() - 0.5D, y - pos.getY() - 0.5D, z - pos.getZ() - 0.5D, texture);
                        effectRenderer.addParticle(effect);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        if (Platform.isServer()) {
            this.cb(world, pos).onneighborUpdate(world, pos, fromPos);
        }
    }

    private ICableBusContainer cb(final BlockView w, final BlockPos pos) {
        final BlockEntity te = w.getBlockEntity(pos);
        ICableBusContainer out = null;

        if (te instanceof CableBusBlockEntity) {
            out = ((CableBusBlockEntity) te).getCableBus();
        }

        return out == null ? NULL_CABLE_BUS : out;
    }

    @Nullable
    private IFacadeContainer fc(final BlockView w, final BlockPos pos) {
        final BlockEntity te = w.getBlockEntity(pos);
        IFacadeContainer out = null;

        if (te instanceof CableBusBlockEntity) {
            out = ((CableBusBlockEntity) te).getCableBus().getFacadeContainer();
        }

        return out;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onBlockBreakStart(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isClient()) {
            final HitResult rtr = AppEng.instance().getRTR();
            if (rtr instanceof BlockHitResult) {
                BlockHitResult brtr = (BlockHitResult) rtr;
                if (brtr.getBlockPos().equals(pos)) {
                    final Vec3d hitVec = rtr.getPos().subtract(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

                    if (this.cb(worldIn, pos).clicked(player, Hand.MAIN_HAND, hitVec)) {
                        NetworkHandler.instance().sendToServer(new ClickPacket(pos, brtr.getSide(), (float) hitVec.x,
                                (float) hitVec.y, (float) hitVec.z, Hand.MAIN_HAND, true));
                    }
                }
            }
        }
    }

    public void onBlockClickPacket(World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, Vec3d hitVec) {
        this.cb(worldIn, pos).clicked(playerIn, hand, hitVec);
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        // Transform from world into block space
        Vec3d hitVec = hit.getPos();
        Vec3d hitInBlock = new Vec3d(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
        return this.cb(w, pos).activate(player, hand, hitInBlock) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    public boolean recolorBlock(final BlockView world, final BlockPos pos, final Direction side, final DyeColor color,
            final PlayerEntity who) {
        try {
            return this.cb(world, pos).recolourBlock(side, AEColor.values()[color.ordinal()], who);
        } catch (final Throwable ignored) {
        }
        return false;
    }

    @Override
    public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> list) {
        // do nothing
    }

    @Override
    public BlockState getFacadeState(BlockView world, BlockPos pos, Direction side) {
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
    public VoxelShape getOutlineShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {
        CableBusBlockEntity te = getBlockEntity(w, pos);
        if (te == null) {
            return VoxelShapes.empty();
        } else {
            return te.getCableBus().getOutlineShape();
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {
        CableBusBlockEntity te = getBlockEntity(w, pos);
        if (te == null) {
            return VoxelShapes.empty();
        } else {
            Entity entity = null;
            // FIXME FABRIC: even EntityShapeContext doesn't give us the actual entity we're
            // colliding with :|
            return te.getCableBus().getCollisionShape(entity);
        }
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, CableBusBlockEntity te) {
        if (currentState.getBlock() != this) {
            return currentState;
        }
        int lightLevel = te.getCableBus().getLightValue();
        return super.updateBlockStateFromTileEntity(currentState, te).with(LIGHT_LEVEL, lightLevel);
    }

}
