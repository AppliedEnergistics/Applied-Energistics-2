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
import java.util.Optional;

import javax.annotation.Nullable;

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
import net.minecraft.world.inventory.tooltip.TooltipComponent;
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

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.packets.MatterCannonPacket;
import appeng.items.contents.CellConfig;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.LookDirection;
import appeng.util.Platform;

public class MatterCannonItem extends AEBasePoweredItem implements IBasicCellItem {

    /**
     * AE energy units consumer per shot fired.
     */
    private static final int ENERGY_PER_SHOT = 1600;

    public MatterCannonItem(Item.Properties props) {
        super(AEConfig.instance().getMatterCannonBattery(), props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800d + 800d * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return getCellTooltipImage(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        var stack = p.getItemInHand(hand);

        var direction = InteractionUtil.getPlayerRay(p, 32);

        if (fireCannon(level, stack, p, direction)) {
            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                    stack);
        } else {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
    }

    public boolean fireCannon(Level level, ItemStack stack, Player player, LookDirection dir) {
        if (getAECurrentPower(stack) < ENERGY_PER_SHOT) {
            return false;
        }

        var inv = StorageCells.getCellInventory(stack, null);
        if (inv == null) {
            return false;
        }

        var itemList = inv.getAvailableStacks();
        var req = itemList.getFirstEntry(AEItemKey.class);
        if (req == null || !(req.getKey() instanceof AEItemKey itemKey)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(PlayerMessages.AmmoDepleted.text());
            }
            return true;
        }

        int shots = 1;
        var cu = getUpgrades(stack);
        if (cu != null) {
            shots += cu.getInstalledUpgrades(AEItems.SPEED_CARD);
        }
        shots = Math.min(shots, (int) req.getLongValue());

        for (int sh = 0; sh < shots; sh++) {
            extractAEPower(stack, ENERGY_PER_SHOT, Actionable.MODULATE);

            if (level.isClientSide()) {
                // Up until this point, we can simulate on the client, after this,
                // we need to run the server-side version
                return true;
            }

            var aeAmmo = inv.extract(req.getKey(), 1, Actionable.MODULATE, new PlayerSource(player));
            if (aeAmmo == 0) {
                return true;
            }

            var rayFrom = dir.getA();
            var rayTo = dir.getB();
            var direction = rayTo.subtract(rayFrom);
            direction.normalize();

            var x = rayFrom.x;
            var y = rayFrom.y;
            var z = rayFrom.z;

            var ammoStack = itemKey.toStack();
            var penetration = getPenetration(ammoStack); // 196.96655f;
            if (penetration <= 0) {
                if (ammoStack.getItem() instanceof PaintBallItem) {
                    shootPaintBalls(ammoStack, level, player, rayFrom, rayTo, direction, x, y, z);
                    return true;
                }
            } else {
                standardAmmo(penetration, level, player, rayFrom, rayTo, direction, x, y, z);
            }
        }

        return true;
    }

    private void shootPaintBalls(ItemStack type, Level level, @Nullable Player p, Vec3 Vector3d,
            Vec3 Vector3d1, Vec3 direction, double d0, double d1, double d2) {
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
        } catch (Exception err) {
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

    private void standardAmmo(float penetration, Level level, Player p, Vec3 Vector3d,
            Vec3 Vector3d1, Vec3 direction, double d0, double d1, double d2) {
        boolean hasDestroyed = true;
        while (penetration > 0 && hasDestroyed) {
            hasDestroyed = false;

            final AABB bb = new AABB(Math.min(Vector3d.x, Vector3d1.x),
                    Math.min(Vector3d.y, Vector3d1.y), Math.min(Vector3d.z, Vector3d1.z),
                    Math.max(Vector3d.x, Vector3d1.x), Math.max(Vector3d.y, Vector3d1.y),
                    Math.max(Vector3d.z, Vector3d1.z)).inflate(16, 16, 16);

            Entity entity = null;
            Vec3 entityIntersection = null;
            var list = level.getEntities(p, bb, e -> !(e instanceof ItemEntity) && e.isAlive());
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
            } catch (Exception err) {
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
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 4, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        // Item is crafted with a normal cell, base energy card contains a dense cell (x8)
        setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades) * 8);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(AEItemKey.filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {

        if (requestedAddition instanceof AEItemKey itemKey) {
            var pen = getPenetration(itemKey.toStack());
            if (pen > 0) {
                return false;
            }

            return !(itemKey.getItem() instanceof PaintBallItem);
        }

        return true;
    }

    private float getPenetration(ItemStack itemStack) {
        // We need a server to query the recipes if the cache is empty
        var server = AppEng.instance().getCurrentServer();
        if (server == null) {
            AELog.warn("Tried to get penetration of matter cannon ammo for %s while no server was running", itemStack);
            return 0;
        }

        var recipes = server.getRecipeManager().byType(MatterCannonAmmo.TYPE);
        for (var ammoRecipe : recipes.values()) {
            if (ammoRecipe.getAmmo().test(itemStack)) {
                return ammoRecipe.getWeight();
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
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }
}
