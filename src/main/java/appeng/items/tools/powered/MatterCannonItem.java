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

package appeng.items.tools.powered;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MatterCannonPacket;
import appeng.hooks.ticking.PlayerColor;
import appeng.hooks.ticking.TickHandler;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.util.InteractionUtil;
import appeng.util.LookDirection;
import appeng.util.Platform;

public class MatterCannonItem extends AEBasePoweredItem implements IStorageCell<IAEItemStack> {

    /**
     * AE energy units consumer per shot fired.
     */
    private static final int ENERGY_PER_SHOT = 1600;

    public MatterCannonItem(Item.Properties props) {
        super(AEConfig.instance().getMatterCannonBattery(), props);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);

        final ICellInventoryHandler<IAEItemStack> cdi = Api.instance().registries().cell().getCellInventory(stack, null,
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

        Api.instance().client().addCellInformation(cdi, lines);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player p,
            final @Nullable InteractionHand hand) {
        if (this.getAECurrentPower(p.getItemInHand(hand)) > ENERGY_PER_SHOT) {
            int shots = 1;

            final CellUpgrades cu = (CellUpgrades) this.getUpgradesInventory(p.getItemInHand(hand));
            if (cu != null) {
                shots += cu.getInstalledUpgrades(Upgrades.SPEED);
            }

            final ICellInventoryHandler<IAEItemStack> inv = Api.instance().registries().cell().getCellInventory(
                    p.getItemInHand(hand), null, Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
            if (inv != null) {
                final IItemList<IAEItemStack> itemList = inv.getAvailableItems(
                        Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
                IAEItemStack req = itemList.getFirstItem();
                if (req instanceof IAEItemStack) {
                    shots = Math.min(shots, (int) req.getStackSize());
                    for (int sh = 0; sh < shots; sh++) {
                        IAEItemStack aeAmmo = req.copy();
                        this.extractAEPower(p.getItemInHand(hand), ENERGY_PER_SHOT, Actionable.MODULATE);

                        if (level.isClientSide()) {
                            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                                    p.getItemInHand(hand));
                        }

                        aeAmmo.setStackSize(1);
                        final ItemStack ammo = aeAmmo.createItemStack();
                        if (ammo.isEmpty()) {
                            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                                    p.getItemInHand(hand));
                        }

                        aeAmmo = inv.extractItems(aeAmmo, Actionable.MODULATE, new PlayerSource(p, null));
                        if (aeAmmo == null) {
                            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                                    p.getItemInHand(hand));
                        }

                        final LookDirection dir = InteractionUtil.getPlayerRay(p, 32);

                        final Vec3 rayFrom = dir.getA();
                        final Vec3 rayTo = dir.getB();
                        final Vec3 direction = rayTo.subtract(rayFrom);
                        direction.normalize();

                        final double d0 = rayFrom.x;
                        final double d1 = rayFrom.y;
                        final double d2 = rayFrom.z;

                        final float penetration = Api.instance().registries().matterCannon().getPenetration(ammo); // 196.96655f;
                        if (penetration <= 0) {
                            final ItemStack type = aeAmmo.asItemStackRepresentation();
                            if (type.getItem() instanceof PaintBallItem) {
                                this.shootPaintBalls(type, level, p, rayFrom, rayTo, direction, d0, d1, d2);
                            }
                            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                                    p.getItemInHand(hand));
                        } else {
                            this.standardAmmo(penetration, level, p, rayFrom, rayTo, direction, d0, d1, d2);
                        }
                    }
                } else {
                    if (!level.isClientSide()) {
                        p.sendMessage(PlayerMessages.AmmoDepleted.get(), Util.NIL_UUID);
                    }
                    return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                            p.getItemInHand(hand));
                }
            }
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, p.getItemInHand(hand));
    }

    private void shootPaintBalls(final ItemStack type, final Level level, final Player p, final Vec3 Vector3d,
            final Vec3 Vector3d1, final Vec3 direction, final double d0, final double d1, final double d2) {
        final AABB bb = new AABB(Math.min(Vector3d.x, Vector3d1.x), Math.min(Vector3d.y, Vector3d1.y),
                Math.min(Vector3d.z, Vector3d1.z), Math.max(Vector3d.x, Vector3d1.x), Math.max(Vector3d.y, Vector3d1.y),
                Math.max(Vector3d.z, Vector3d1.z)).inflate(16, 16, 16);

        Entity entity = null;
        Vec3 entityIntersection = null;
        final List<Entity> list = level.getEntities(p, bb,
                e -> !(e instanceof ItemEntity) && e.isAlive());
        double closest = 9999999.0D;

        for (Entity entity1 : list) {
            if (p.isPassenger() && entity1.hasPassenger(p)) {
                continue;
            }

            final float f1 = 0.3F;

            final AABB boundingBox = entity1.getBoundingBox().inflate(f1, f1, f1);
            final Vec3 intersection = boundingBox.clip(Vector3d, Vector3d1).orElse(null);

            if (intersection != null) {
                final double nd = Vector3d.distanceToSqr(intersection);

                if (nd < closest) {
                    entity = entity1;
                    entityIntersection = intersection;
                    closest = nd;
                }
            }
        }

        ClipContext rayTraceContext = new ClipContext(Vector3d, Vector3d1, Block.COLLIDER,
                Fluid.NONE, p);
        HitResult pos = level.clip(rayTraceContext);

        final Vec3 vec = new Vec3(d0, d1, d2);
        if (entity != null && pos.getType() != Type.MISS
                && pos.getLocation().distanceToSqr(vec) > closest) {
            pos = new EntityHitResult(entity, entityIntersection);
        } else if (entity != null && pos.getType() == Type.MISS) {
            pos = new EntityHitResult(entity, entityIntersection);
        }

        try {
            AppEng.instance().sendToAllNearExcept(null, d0, d1, d2, 128, level,
                    new MatterCannonPacket(d0, d1, d2, (float) direction.x, (float) direction.y, (float) direction.z,
                            (byte) (pos.getType() == Type.MISS ? 32
                                    : pos.getLocation().distanceToSqr(vec) + 1)));
        } catch (final Exception err) {
            AELog.debug(err);
        }

        if (pos.getType() != Type.MISS && type != null && type.getItem() instanceof PaintBallItem) {
            final PaintBallItem ipb = (PaintBallItem) type.getItem();

            final AEColor col = ipb.getColor();
            // boolean lit = ipb.isLumen( type );

            if (pos instanceof EntityHitResult) {
                EntityHitResult entityResult = (EntityHitResult) pos;
                Entity entityHit = entityResult.getEntity();

                final int id = entityHit.getId();
                final PlayerColor marker = new PlayerColor(id, col, 20 * 30);
                TickHandler.instance().getPlayerColors().put(id, marker);

                if (entityHit instanceof Sheep) {
                    final Sheep sh = (Sheep) entityHit;
                    sh.setColor(col.dye);
                }

                entityHit.hurt(DamageSource.playerAttack(p), 0);
                NetworkHandler.instance().sendToAll(marker.getPacket());
            } else if (pos instanceof BlockHitResult) {
                BlockHitResult blockResult = (BlockHitResult) pos;
                final Direction side = blockResult.getDirection();
                final BlockPos hitPos = blockResult.getBlockPos().relative(side);

                if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitPos), p)) {
                    return;
                }

                final BlockState whatsThere = level.getBlockState(hitPos);
                if (whatsThere.getMaterial().isReplaceable() && level.isEmptyBlock(hitPos)) {
                    level.setBlock(hitPos, AEBlocks.PAINT.block().defaultBlockState(), 3);
                }

                final BlockEntity te = level.getBlockEntity(hitPos);
                if (te instanceof PaintSplotchesBlockEntity) {
                    final Vec3 hp = pos.getLocation().subtract(hitPos.getX(), hitPos.getY(), hitPos.getZ());
                    ((PaintSplotchesBlockEntity) te).addBlot(type, side.getOpposite(), hp);
                }
            }
        }
    }

    private void standardAmmo(float penetration, final Level level, final Player p, final Vec3 Vector3d,
            final Vec3 Vector3d1, final Vec3 direction, final double d0, final double d1, final double d2) {
        boolean hasDestroyed = true;
        while (penetration > 0 && hasDestroyed) {
            hasDestroyed = false;

            final AABB bb = new AABB(Math.min(Vector3d.x, Vector3d1.x),
                    Math.min(Vector3d.y, Vector3d1.y), Math.min(Vector3d.z, Vector3d1.z),
                    Math.max(Vector3d.x, Vector3d1.x), Math.max(Vector3d.y, Vector3d1.y),
                    Math.max(Vector3d.z, Vector3d1.z)).inflate(16, 16, 16);

            Entity entity = null;
            Vec3 entityIntersection = null;
            final List<Entity> list = level.getEntities(p, bb,
                    e -> !(e instanceof ItemEntity) && e.isAlive());
            double closest = 9999999.0D;

            for (Entity entity1 : list) {
                if (p.isPassenger() && entity1.hasPassenger(p)) {
                    continue;
                }

                final float f1 = 0.3F;

                final AABB boundingBox = entity1.getBoundingBox().inflate(f1, f1, f1);
                final Vec3 intersection = boundingBox.clip(Vector3d, Vector3d1).orElse(null);

                if (intersection != null) {
                    final double nd = Vector3d.distanceToSqr(intersection);

                    if (nd < closest) {
                        entity = entity1;
                        entityIntersection = intersection;
                        closest = nd;
                    }
                }
            }

            ClipContext rayTraceContext = new ClipContext(Vector3d, Vector3d1,
                    Block.COLLIDER, Fluid.NONE, p);
            final Vec3 vec = new Vec3(d0, d1, d2);
            HitResult pos = level.clip(rayTraceContext);
            if (entity != null && pos.getType() != Type.MISS
                    && pos.getLocation().distanceToSqr(vec) > closest) {
                pos = new EntityHitResult(entity, entityIntersection);
            } else if (entity != null && pos.getType() == Type.MISS) {
                pos = new EntityHitResult(entity, entityIntersection);
            }

            try {
                AppEng.instance().sendToAllNearExcept(null, d0, d1, d2, 128, level,
                        new MatterCannonPacket(d0, d1, d2, (float) direction.x, (float) direction.y,
                                (float) direction.z, (byte) (pos.getType() == Type.MISS ? 32
                                        : pos.getLocation().distanceToSqr(vec) + 1)));
            } catch (final Exception err) {
                AELog.debug(err);
            }

            if (pos.getType() != Type.MISS) {
                final DamageSource dmgSrc = new EntityDamageSource("matter_cannon", p);

                if (pos instanceof EntityHitResult) {
                    EntityHitResult entityResult = (EntityHitResult) pos;
                    Entity entityHit = entityResult.getEntity();

                    final int dmg = (int) Math.ceil(penetration / 20.0f);
                    if (entityHit instanceof LivingEntity) {
                        final LivingEntity el = (LivingEntity) entityHit;
                        penetration -= dmg;
                        el.knockback(0, -direction.x, -direction.z);
                        // el.knockBack( p, 0, Vector3d.x,
                        // Vector3d.z );
                        el.hurt(dmgSrc, dmg);
                        if (!el.isAlive()) {
                            hasDestroyed = true;
                        }
                    } else if (entityHit instanceof ItemEntity) {
                        hasDestroyed = true;
                        entityHit.discard();
                    } else if (entityHit.hurt(dmgSrc, dmg)) {
                        hasDestroyed = true;
                    }
                } else if (pos instanceof BlockHitResult) {
                    BlockHitResult blockResult = (BlockHitResult) pos;

                    if (!AEConfig.instance().isMatterCanonBlockDamageEnabled()) {
                        penetration = 0;
                    } else {
                        BlockPos blockPos = blockResult.getBlockPos();
                        final BlockState bs = level.getBlockState(blockPos);

                        final float hardness = bs.getDestroySpeed(level, blockPos) * 9.0f;
                        if (hardness >= 0.0 && penetration > hardness
                                && Platform.hasPermissions(new DimensionalBlockPos(level, blockPos), p)) {
                            hasDestroyed = true;
                            penetration -= hardness;
                            penetration *= 0.60;
                            level.destroyBlock(blockPos, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 4);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean isBlackListed(final ItemStack cellItem, final IAEItemStack requestedAddition) {
        final float pen = Api.instance().registries().matterCannon()
                .getPenetration(requestedAddition.createItemStack());
        if (pen > 0) {
            return false;
        }

        if (requestedAddition.getItem() instanceof PaintBallItem) {
            return false;
        }

        return true;
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }
}
