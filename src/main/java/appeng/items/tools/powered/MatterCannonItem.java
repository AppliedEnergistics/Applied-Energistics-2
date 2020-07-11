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

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.features.AEFeature;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MatterCannonPacket;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.tile.misc.PaintSplotchesTileEntity;
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
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);

        final ICellInventoryHandler<IAEItemStack> cdi = Api.instance().registries().cell().getCellInventory(stack, null,
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

        Api.instance().client().addCellInformation(cdi, lines);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity p, final @Nullable Hand hand) {
        if (this.getAECurrentPower(p.getHeldItem(hand)) > ENERGY_PER_SHOT) {
            int shots = 1;

            final CellUpgrades cu = (CellUpgrades) this.getUpgradesInventory(p.getHeldItem(hand));
            if (cu != null) {
                shots += cu.getInstalledUpgrades(Upgrades.SPEED);
            }

            final ICellInventoryHandler<IAEItemStack> inv = Api.instance().registries().cell().getCellInventory(
                    p.getHeldItem(hand), null, Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
            if (inv != null) {
                final IItemList<IAEItemStack> itemList = inv.getAvailableItems(
                        Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
                IAEItemStack req = itemList.getFirstItem();
                if (req instanceof IAEItemStack) {
                    shots = Math.min(shots, (int) req.getStackSize());
                    for (int sh = 0; sh < shots; sh++) {
                        IAEItemStack aeAmmo = req.copy();
                        this.extractAEPower(p.getHeldItem(hand), ENERGY_PER_SHOT, Actionable.MODULATE);

                        if (Platform.isClient()) {
                            return new ActionResult<>(ActionResultType.SUCCESS, p.getHeldItem(hand));
                        }

                        aeAmmo.setStackSize(1);
                        final ItemStack ammo = aeAmmo.createItemStack();
                        if (ammo.isEmpty()) {
                            return new ActionResult<>(ActionResultType.SUCCESS, p.getHeldItem(hand));
                        }

                        aeAmmo = inv.extractItems(aeAmmo, Actionable.MODULATE, new PlayerSource(p, null));
                        if (aeAmmo == null) {
                            return new ActionResult<>(ActionResultType.SUCCESS, p.getHeldItem(hand));
                        }

                        final LookDirection dir = Platform.getPlayerRay(p, 32);

                        final Vec3d rayFrom = dir.getA();
                        final Vec3d rayTo = dir.getB();
                        final Vec3d direction = rayTo.subtract(rayFrom);
                        direction.normalize();

                        final double d0 = rayFrom.x;
                        final double d1 = rayFrom.y;
                        final double d2 = rayFrom.z;

                        final float penetration = Api.instance().registries().matterCannon().getPenetration(ammo); // 196.96655f;
                        if (penetration <= 0) {
                            final ItemStack type = aeAmmo.asItemStackRepresentation();
                            if (type.getItem() instanceof PaintBallItem) {
                                this.shootPaintBalls(type, w, p, rayFrom, rayTo, direction, d0, d1, d2);
                            }
                            return new ActionResult<>(ActionResultType.SUCCESS, p.getHeldItem(hand));
                        } else {
                            this.standardAmmo(penetration, w, p, rayFrom, rayTo, direction, d0, d1, d2);
                        }
                    }
                } else {
                    if (Platform.isServer()) {
                        p.sendMessage(PlayerMessages.AmmoDepleted.get());
                    }
                    return new ActionResult<>(ActionResultType.SUCCESS, p.getHeldItem(hand));
                }
            }
        }
        return new ActionResult<>(ActionResultType.FAIL, p.getHeldItem(hand));
    }

    private void shootPaintBalls(final ItemStack type, final World w, final PlayerEntity p, final Vec3d Vec3d,
            final Vec3d Vec3d1, final Vec3d direction, final double d0, final double d1, final double d2) {
        final AxisAlignedBB bb = new AxisAlignedBB(Math.min(Vec3d.x, Vec3d1.x), Math.min(Vec3d.y, Vec3d1.y),
                Math.min(Vec3d.z, Vec3d1.z), Math.max(Vec3d.x, Vec3d1.x), Math.max(Vec3d.y, Vec3d1.y),
                Math.max(Vec3d.z, Vec3d1.z)).grow(16, 16, 16);

        Entity entity = null;
        Vec3d entityIntersection = null;
        final List list = w.getEntitiesWithinAABBExcludingEntity(p, bb);
        double closest = 9999999.0D;

        for (int l = 0; l < list.size(); ++l) {
            final Entity entity1 = (Entity) list.get(l);

            if (!entity1.isAlive() && entity1 != p && !(entity1 instanceof ItemEntity)) {
                if (entity1.isAlive()) {
                    // prevent killing / flying of mounts.
                    if (entity1.isRidingOrBeingRiddenBy(p)) {
                        continue;
                    }

                    final float f1 = 0.3F;

                    final AxisAlignedBB boundingBox = entity1.getBoundingBox().grow(f1, f1, f1);
                    final Vec3d intersection = boundingBox.rayTrace(Vec3d, Vec3d1).orElse(null);

                    if (intersection != null) {
                        final double nd = Vec3d.squareDistanceTo(intersection);

                        if (nd < closest) {
                            entity = entity1;
                            entityIntersection = intersection;
                            closest = nd;
                        }
                    }
                }
            }
        }

        RayTraceContext rayTraceContext = new RayTraceContext(Vec3d, Vec3d1, RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE, p);
        RayTraceResult pos = w.rayTraceBlocks(rayTraceContext);

        final Vec3d vec = new Vec3d(d0, d1, d2);
        if (entity != null && pos.getType() != RayTraceResult.Type.MISS
                && pos.getHitVec().squareDistanceTo(vec) > closest) {
            pos = new EntityRayTraceResult(entity, entityIntersection);
        } else if (entity != null && pos.getType() == RayTraceResult.Type.MISS) {
            pos = new EntityRayTraceResult(entity, entityIntersection);
        }

        try {
            AppEng.proxy.sendToAllNearExcept(null, d0, d1, d2, 128, w,
                    new MatterCannonPacket(d0, d1, d2, (float) direction.x, (float) direction.y, (float) direction.z,
                            (byte) (pos.getType() == RayTraceResult.Type.MISS ? 32
                                    : pos.getHitVec().squareDistanceTo(vec) + 1)));
        } catch (final Exception err) {
            AELog.debug(err);
        }

        if (pos.getType() != RayTraceResult.Type.MISS && type != null && type.getItem() instanceof PaintBallItem) {
            final PaintBallItem ipb = (PaintBallItem) type.getItem();

            final AEColor col = ipb.getColor();
            // boolean lit = ipb.isLumen( type );

            if (pos instanceof EntityRayTraceResult) {
                EntityRayTraceResult entityResult = (EntityRayTraceResult) pos;
                Entity entityHit = entityResult.getEntity();

                final int id = entityHit.getEntityId();
                final PlayerColor marker = new PlayerColor(id, col, 20 * 30);
                TickHandler.INSTANCE.getPlayerColors().put(id, marker);

                if (entityHit instanceof SheepEntity) {
                    final SheepEntity sh = (SheepEntity) entityHit;
                    sh.setFleeceColor(col.dye);
                }

                entityHit.attackEntityFrom(DamageSource.causePlayerDamage(p), 0);
                NetworkHandler.instance().sendToAll(marker.getPacket());
            } else if (pos instanceof BlockRayTraceResult) {
                BlockRayTraceResult blockResult = (BlockRayTraceResult) pos;
                final Direction side = blockResult.getFace();
                final BlockPos hitPos = blockResult.getPos().offset(side);

                if (!Platform.hasPermissions(new DimensionalCoord(w, hitPos), p)) {
                    return;
                }

                final BlockState whatsThere = w.getBlockState(hitPos);
                if (whatsThere.getMaterial().isReplaceable() && w.isAirBlock(hitPos)) {
                    Api.instance().definitions().blocks().paint().maybeBlock().ifPresent(paintBlock -> {
                        w.setBlockState(hitPos, paintBlock.getDefaultState(), 3);
                    });
                }

                final TileEntity te = w.getTileEntity(hitPos);
                if (te instanceof PaintSplotchesTileEntity) {
                    final Vec3d hp = pos.getHitVec().subtract(hitPos.getX(), hitPos.getY(), hitPos.getZ());
                    ((PaintSplotchesTileEntity) te).addBlot(type, side.getOpposite(), hp);
                }
            }
        }
    }

    private void standardAmmo(float penetration, final World w, final PlayerEntity p, final Vec3d Vec3d,
            final Vec3d Vec3d1, final Vec3d direction, final double d0, final double d1, final double d2) {
        boolean hasDestroyed = true;
        while (penetration > 0 && hasDestroyed) {
            hasDestroyed = false;

            final AxisAlignedBB bb = new AxisAlignedBB(Math.min(Vec3d.x, Vec3d1.x), Math.min(Vec3d.y, Vec3d1.y),
                    Math.min(Vec3d.z, Vec3d1.z), Math.max(Vec3d.x, Vec3d1.x), Math.max(Vec3d.y, Vec3d1.y),
                    Math.max(Vec3d.z, Vec3d1.z)).grow(16, 16, 16);

            Entity entity = null;
            Vec3d entityIntersection = null;
            final List list = w.getEntitiesWithinAABBExcludingEntity(p, bb);
            double closest = 9999999.0D;

            for (int l = 0; l < list.size(); ++l) {
                final Entity entity1 = (Entity) list.get(l);

                if (entity1.isAlive() && entity1 != p && !(entity1 instanceof ItemEntity)) {
                    if (entity1.isAlive()) {
                        // prevent killing / flying of mounts.
                        if (entity1.isRidingOrBeingRiddenBy(p)) {
                            continue;
                        }

                        final float f1 = 0.3F;

                        final AxisAlignedBB boundingBox = entity1.getBoundingBox().grow(f1, f1, f1);
                        final Vec3d intersection = boundingBox.rayTrace(Vec3d, Vec3d1).orElse(null);

                        if (intersection != null) {
                            final double nd = Vec3d.squareDistanceTo(intersection);

                            if (nd < closest) {
                                entity = entity1;
                                entityIntersection = intersection;
                                closest = nd;
                            }
                        }
                    }
                }
            }

            RayTraceContext rayTraceContext = new RayTraceContext(Vec3d, Vec3d1, RayTraceContext.BlockMode.COLLIDER,
                    RayTraceContext.FluidMode.NONE, p);
            final Vec3d vec = new Vec3d(d0, d1, d2);
            RayTraceResult pos = w.rayTraceBlocks(rayTraceContext);
            if (entity != null && pos.getType() != RayTraceResult.Type.MISS
                    && pos.getHitVec().squareDistanceTo(vec) > closest) {
                pos = new EntityRayTraceResult(entity, entityIntersection);
            } else if (entity != null && pos.getType() == RayTraceResult.Type.MISS) {
                pos = new EntityRayTraceResult(entity, entityIntersection);
            }

            try {
                AppEng.proxy.sendToAllNearExcept(null, d0, d1, d2, 128, w,
                        new MatterCannonPacket(d0, d1, d2, (float) direction.x, (float) direction.y,
                                (float) direction.z, (byte) (pos.getType() == RayTraceResult.Type.MISS ? 32
                                        : pos.getHitVec().squareDistanceTo(vec) + 1)));
            } catch (final Exception err) {
                AELog.debug(err);
            }

            if (pos.getType() != RayTraceResult.Type.MISS) {
                final DamageSource dmgSrc = new EntityDamageSource("matter_cannon", p);

                if (pos instanceof EntityRayTraceResult) {
                    EntityRayTraceResult entityResult = (EntityRayTraceResult) pos;
                    Entity entityHit = entityResult.getEntity();

                    final int dmg = (int) Math.ceil(penetration / 20.0f);
                    if (entityHit instanceof LivingEntity) {
                        final LivingEntity el = (LivingEntity) entityHit;
                        penetration -= dmg;
                        el.knockBack(p, 0, -direction.x, -direction.z);
                        // el.knockBack( p, 0, Vec3d.x,
                        // Vec3d.z );
                        el.attackEntityFrom(dmgSrc, dmg);
                        if (!el.isAlive()) {
                            hasDestroyed = true;
                        }
                    } else if (entityHit instanceof ItemEntity) {
                        hasDestroyed = true;
                        entityHit.remove();
                    } else if (entityHit.attackEntityFrom(dmgSrc, dmg)) {
                        hasDestroyed = true;
                    }
                } else if (pos instanceof BlockRayTraceResult) {
                    BlockRayTraceResult blockResult = (BlockRayTraceResult) pos;

                    if (!AEConfig.instance().isFeatureEnabled(AEFeature.MASS_CANNON_BLOCK_DAMAGE)) {
                        penetration = 0;
                    } else {
                        BlockPos blockPos = blockResult.getPos();
                        final BlockState bs = w.getBlockState(blockPos);

                        final float hardness = bs.getBlockHardness(w, blockPos) * 9.0f;
                        if (hardness >= 0.0) {
                            if (penetration > hardness
                                    && Platform.hasPermissions(new DimensionalCoord(w, blockPos), p)) {
                                hasDestroyed = true;
                                penetration -= hardness;
                                penetration *= 0.60;
                                w.destroyBlock(blockPos, true);
                            }
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
