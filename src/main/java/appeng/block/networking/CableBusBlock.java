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

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.*;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorldReader;
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
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.helpers.AEGlassMaterial;
import appeng.integration.abstraction.IAEFacade;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import appeng.tile.AEBaseBlockEntity;
import appeng.tile.networking.CableBusBlockEntity;
import appeng.util.Platform;

public class CableBusBlock extends AEBaseTileBlock<CableBusBlockEntity> implements IAEFacade {

    private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();

    public CableBusBlock() {
        super(defaultProps(AEGlassMaterial.INSTANCE).notSolid().noDrops());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockView reader, BlockPos pos) {
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
    public int getWeakPower(final BlockState state, final BlockView w, final BlockPos pos, final Direction side) {
        return this.cb(w, pos).isProvidingWeakPower(side.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    public boolean canProvidePower(final BlockState state) {
        return true;
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entityIn) {
        this.cb(w, pos).onEntityCollision(entityIn);
    }

    @Override
    public int getStrongPower(final BlockState state, final BlockView w, final BlockPos pos, final Direction side) {
        return this.cb(w, pos).isProvidingStrongPower(side.getOpposite()); // TODO:
        // IS
        // OPPOSITE!?
    }

    @Override
    public int getLightValue(final BlockState state, final BlockView world, final BlockPos pos) {
        if (state.getBlock() != this) {
            return state.getBlock().getLightValue(state, world, pos);
        }
        return this.cb(world, pos).getLightValue();
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return this.cb(world, pos).isLadder(entity);
    }

    @Override
    public boolean isReplaceable(BlockState state, ItemPlacementContext useContext) {
        // FIXME: Potentially check the fluid one too
        return super.isReplaceable(state, useContext) && this.cb(useContext.getWorld(), useContext.getPos()).isEmpty();
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player,
            boolean willHarvest, IFluidState fluid) {
        if (player.abilities.isCreativeMode) {
            final AEBaseBlockEntity tile = this.getBlockEntity(world, pos);
            if (tile != null) {
                tile.disableDrops();
            }
            // maybe ray trace?
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public boolean canConnectRedstone(final BlockState state, final BlockView w, final BlockPos pos,
            Direction side) {
        if (side == null) {
            side = Direction.UP;
        }

        return this.cb(w, pos).canConnectRedstone(EnumSet.of(side));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockView world, BlockPos pos,
            PlayerEntity player) {
        final Vec3d v3 = target.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
        final SelectedPart sp = this.cb(world, pos).selectPart(v3);

        if (sp.part != null) {
            return sp.part.getItemStack(PartItemStack.PICK);
        } else if (sp.facade != null) {
            return sp.facade.getItemStack();
        }

        return ItemStack.EMPTY;
    }

    @Override
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
        BlockPos blockPos = new BlockPos(target.getHitVec().x, target.getHitVec().y, target.getHitVec().z);

        ICableBusContainer cb = this.cb(world, blockPos);

        // Our built-in model has the actual baked sprites we need
        BakedModel model = MinecraftClient.getInstance().getBlockRendererDispatcher()
                .getModelForState(this.getDefaultState());

        // We cannot add the effect if we don't have the model
        if (!(model instanceof CableBusBakedModel)) {
            return true;
        }

        CableBusBakedModel cableBusModel = (CableBusBakedModel) model;

        CableBusRenderState renderState = cb.getRenderState();

        // Spawn a particle for one of the particle textures
        TextureAtlasSprite texture = Platform.pickRandom(cableBusModel.getParticleTextures(renderState));
        if (texture != null) {
            double x = target.getHitVec().x;
            double y = target.getHitVec().y;
            double z = target.getHitVec().z;
            // FIXME: Check how this looks, probably like shit, maybe provide parts the
            // ability to supply particle textures???
            effectRenderer
                    .addEffect(new CableBusBreakingParticle(world, x, y, z, texture).multiplyParticleScaleBy(0.8F));
        }

        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager effectRenderer) {
        ICableBusContainer cb = this.cb(world, pos);

        // Our built-in model has the actual baked sprites we need
        BakedModel model = MinecraftClient.getInstance().getBlockRendererDispatcher()
                .getModelForState(this.getDefaultState());

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
                        Particle effect = new CableBusBreakingParticle(world, x, y, z, x - pos.getX() - 0.5D,
                                y - pos.getY() - 0.5D, z - pos.getZ() - 0.5D, texture);
                        effectRenderer.addEffect(effect);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        if (Platform.isServer()) {
            this.cb(world, pos).onNeighborChanged(world, pos, fromPos);
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

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isClient()) {
            final HitResult rtr = MinecraftClient.getInstance().objectMouseOver;
            if (rtr instanceof BlockHitResult) {
                BlockHitResult brtr = (BlockHitResult) rtr;
                if (brtr.getPos().equals(pos)) {
                    final Vec3d hitVec = rtr.getHitVec().subtract(new Vec3d(pos));

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
        Vec3d hitVec = hit.getHitVec();
        Vec3d hitInBlock = new Vec3d(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
        return this.cb(w, pos).activate(player, hand, hitInBlock) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    public boolean recolorBlock(final BlockView world, final BlockPos pos, final Direction side,
            final DyeColor color, final PlayerEntity who) {
        try {
            return this.cb(world, pos).recolourBlock(side, AEColor.values()[color.ordinal()], who);
        } catch (final Throwable ignored) {
        }
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> itemStacks) {
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
    public VoxelShape getShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {
        CableBusBlockEntity te = getBlockEntity(w, pos);
        if (te == null) {
            return VoxelShapes.empty();
        } else {
            return te.getCableBus().getShape();
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {
        CableBusBlockEntity te = getBlockEntity(w, pos);
        if (te == null) {
            return VoxelShapes.empty();
        } else {
            return te.getCableBus().getCollisionShape(context.getEntity());
        }
    }

}
