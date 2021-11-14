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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
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
import net.minecraft.world.item.crafting.Recipe;
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

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.data.AEItemKey;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.packets.MatterCannonPacket;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.parts.automation.UpgradeInventory;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class MatterCannonItem extends AEBasePoweredItem implements IBasicCellItem<AEItemKey> {

    /**
     * AE energy units consumer per shot fired.
     */
    private static final int ENERGY_PER_SHOT = 1600;

    public MatterCannonItem(Item.Properties props) {
        super(AEConfig.instance().getMatterCannonBattery(), props);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
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

            var inv = StorageCells.getCellInventory(p.getItemInHand(hand), null, StorageChannels.items());
            if (inv != null) {
                var itemList = inv.getAvailableStacks();
                var req = itemList.getFirstEntry();
                if (req != null) {
                    shots = Math.min(shots, (int) req.getLongValue());
                    for (int sh = 0; sh < shots; sh++) {
                        this.extractAEPower(p.getItemInHand(hand), ENERGY_PER_SHOT, Actionable.MODULATE);

                        if (level.isClientSide()) {
                            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                                    p.getItemInHand(hand));
                        }

                        var aeAmmo = inv.extract(req.getKey(), 1, Actionable.MODULATE, new PlayerSource(p));
                        if (aeAmmo == 0) {
                            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                                    p.getItemInHand(hand));
                        }

                        var dir = InteractionUtil.getPlayerRay(p, 32);

                        final Vec3 rayFrom = dir.getA();
                        final Vec3 rayTo = dir.getB();
                        final Vec3 direction = rayTo.subtract(rayFrom);
                        direction.normalize();

                        var x = rayFrom.x;
                        var y = rayFrom.y;
                        var z = rayFrom.z;

                        var stack = req.getKey().toStack();
                        var penetration = getPenetration(stack); // 196.96655f;
                        if (penetration <= 0) {
                            if (stack.getItem() instanceof PaintBallItem) {
                                this.shootPaintBalls(stack, level, p, rayFrom, rayTo, direction, x, y, z);
                            }
                            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                                    p.getItemInHand(hand));
                        } else {
                            this.standardAmmo(penetration, level, p, rayFrom, rayTo, direction, x, y, z);
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

        if (pos.getType() != Type.MISS && type != null && type.getItem() instanceof PaintBallItem ipb) {

            final AEColor col = ipb.getColor();
            // boolean lit = ipb.isLumen( type );

            if (pos instanceof EntityHitResult entityResult) {
                var entityHit = entityResult.getEntity();

                if (entityHit instanceof Sheep sh) {
                    sh.setColor(col.dye);
                }

                entityHit.hurt(DamageSource.playerAttack(p), 0);
            } else if (pos instanceof BlockHitResult blockResult) {
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

                if (pos instanceof EntityHitResult entityResult) {
                    Entity entityHit = entityResult.getEntity();

                    final int dmg = (int) Math.ceil(penetration / 20.0f);
                    if (entityHit instanceof LivingEntity el) {
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
                        hasDestroyed = !entityHit.isAlive();
                    }
                } else if (pos instanceof BlockHitResult blockResult) {

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
    public UpgradeInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 4);
    }

    @Override
    public ConfigInventory<AEItemKey> getConfigInventory(final ItemStack is) {
        return CellConfig.create(StorageChannels.items(), is);
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
    public boolean isBlackListed(ItemStack cellItem, AEItemKey requestedAddition) {

        var pen = getPenetration(requestedAddition.toStack());
        if (pen > 0) {
            return false;
        }

        return !(requestedAddition.getItem() instanceof PaintBallItem);
    }

    private float getPenetration(ItemStack itemStack) {
        // We need a server to query the recipes if the cache is empty
        var server = AppEng.instance().getCurrentServer();
        if (server == null) {
            AELog.warn("Tried to get penetration of matter cannon ammo for %s while no server was running", itemStack);
            return 0;
        }

        var recipes = server.getRecipeManager().byType(MatterCannonAmmo.TYPE);
        for (Recipe<Container> recipe : recipes.values()) {
            if (recipe instanceof MatterCannonAmmo ammoRecipe) {
                if (ammoRecipe.getAmmo().test(itemStack)) {
                    return ammoRecipe.getWeight();
                }
            }
        }

        return 0;
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public IStorageChannel<AEItemKey> getChannel() {
        return StorageChannels.items();
    }
}
