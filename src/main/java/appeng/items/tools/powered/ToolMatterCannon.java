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


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMatterCannon;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.ItemPaintBall;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.tile.misc.TilePaint;
import appeng.util.LookDirection;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;


public class ToolMatterCannon extends AEBasePoweredItem implements IStorageCell<IAEItemStack> {

    public ToolMatterCannon() {
        super(AEConfig.instance().getMatterCannonBattery());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        final ICellInventoryHandler<IAEItemStack> cdi = AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(stack, null,
                        AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

        AEApi.instance().client().addCellInformation(cdi, lines);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final EntityPlayer p, final @Nullable EnumHand hand) {
        if (this.getAECurrentPower(p.getHeldItem(hand)) > 1600) {
            int shots = 1;

            final CellUpgrades cu = (CellUpgrades) this.getUpgradesInventory(p.getHeldItem(hand));
            if (cu != null) {
                shots += cu.getInstalledUpgrades(Upgrades.SPEED);
            }

            final ICellInventoryHandler<IAEItemStack> inv = AEApi.instance()
                    .registries()
                    .cell()
                    .getCellInventory(p.getHeldItem(hand), null,
                            AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            if (inv != null) {
                final IItemList<IAEItemStack> itemList = inv
                        .getAvailableItems(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
                IAEItemStack req = itemList.getFirstItem();
                if (req instanceof IAEItemStack) {
                    shots = Math.min(shots, (int) req.getStackSize());
                    for (int sh = 0; sh < shots; sh++) {
                        IAEItemStack aeAmmo = req.copy();
                        this.extractAEPower(p.getHeldItem(hand), 1600, Actionable.MODULATE);

                        if (Platform.isClient()) {
                            return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
                        }

                        aeAmmo.setStackSize(1);
                        final ItemStack ammo = aeAmmo.createItemStack();
                        if (ammo == null) {
                            return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
                        }

                        aeAmmo = inv.extractItems(aeAmmo, Actionable.MODULATE, new PlayerSource(p, null));
                        if (aeAmmo == null) {
                            return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
                        }

                        final LookDirection dir = Platform.getPlayerRay(p, p.getEyeHeight());

                        final Vec3d Vec3d = dir.getA();
                        final Vec3d Vec3d1 = dir.getB();
                        final Vec3d direction = Vec3d1.subtract(Vec3d);
                        direction.normalize();

                        final double d0 = Vec3d.x;
                        final double d1 = Vec3d.y;
                        final double d2 = Vec3d.z;

                        final float penetration = AEApi.instance().registries().matterCannon().getPenetration(ammo); // 196.96655f;
                        if (penetration <= 0) {
                            final ItemStack type = aeAmmo.asItemStackRepresentation();
                            if (type.getItem() instanceof ItemPaintBall) {
                                this.shootPaintBalls(type, w, p, Vec3d, Vec3d1, direction, d0, d1, d2);
                            }
                            return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
                        } else {
                            this.standardAmmo(penetration, w, p, Vec3d, Vec3d1, direction, d0, d1, d2);
                        }
                    }
                } else {
                    if (Platform.isServer()) {
                        p.sendMessage(PlayerMessages.AmmoDepleted.get());
                    }
                    return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
                }
            }
        }
        return new ActionResult<>(EnumActionResult.FAIL, p.getHeldItem(hand));
    }

    private void shootPaintBalls(final ItemStack type, final World w, final EntityPlayer p, final Vec3d Vec3d, final Vec3d Vec3d1, final Vec3d direction, final double d0, final double d1, final double d2) {
        final AxisAlignedBB bb = new AxisAlignedBB(Math.min(Vec3d.x, Vec3d1.x), Math.min(Vec3d.y, Vec3d1.y), Math.min(Vec3d.z, Vec3d1.z), Math
                .max(Vec3d.x, Vec3d1.x), Math.max(Vec3d.y, Vec3d1.y), Math.max(Vec3d.z, Vec3d1.z)).grow(16, 16, 16);

        Entity entity = null;
        final List list = w.getEntitiesWithinAABBExcludingEntity(p, bb);
        double closest = 9999999.0D;

        for (int l = 0; l < list.size(); ++l) {
            final Entity entity1 = (Entity) list.get(l);

            if (!entity1.isDead && entity1 != p && !(entity1 instanceof EntityItem)) {
                if (entity1.isEntityAlive()) {
                    // prevent killing / flying of mounts.
                    if (entity1.isRidingOrBeingRiddenBy(p)) {
                        continue;
                    }

                    final float f1 = 0.3F;

                    final AxisAlignedBB boundingBox = entity1.getEntityBoundingBox().grow(f1, f1, f1);
                    final RayTraceResult RayTraceResult = boundingBox.calculateIntercept(Vec3d, Vec3d1);

                    if (RayTraceResult != null) {
                        final double nd = Vec3d.squareDistanceTo(RayTraceResult.hitVec);

                        if (nd < closest) {
                            entity = entity1;
                            closest = nd;
                        }
                    }
                }
            }
        }

        RayTraceResult pos = w.rayTraceBlocks(Vec3d, Vec3d1, false);

        final Vec3d vec = new Vec3d(d0, d1, d2);
        if (entity != null && pos != null && pos.hitVec.squareDistanceTo(vec) > closest) {
            pos = new RayTraceResult(entity);
        } else if (entity != null && pos == null) {
            pos = new RayTraceResult(entity);
        }

        try {
            AppEng.proxy.sendToAllNearExcept(null, d0, d1, d2, 128, w,
                    new PacketMatterCannon(d0, d1, d2, (float) direction.x, (float) direction.y, (float) direction.z, (byte) (pos == null ? 32 : pos.hitVec
                            .squareDistanceTo(vec) + 1)));
        } catch (final Exception err) {
            AELog.debug(err);
        }

        if (pos != null && type != null && type.getItem() instanceof ItemPaintBall) {
            final ItemPaintBall ipb = (ItemPaintBall) type.getItem();

            final AEColor col = ipb.getColor(type);
            // boolean lit = ipb.isLumen( type );

            if (pos.typeOfHit == RayTraceResult.Type.ENTITY) {
                final int id = pos.entityHit.getEntityId();
                final PlayerColor marker = new PlayerColor(id, col, 20 * 30);
                TickHandler.INSTANCE.getPlayerColors().put(id, marker);

                if (pos.entityHit instanceof EntitySheep) {
                    final EntitySheep sh = (EntitySheep) pos.entityHit;
                    sh.setFleeceColor(col.dye);
                }

                pos.entityHit.attackEntityFrom(DamageSource.causePlayerDamage(p), 0);
                NetworkHandler.instance().sendToAll(marker.getPacket());
            } else if (pos.typeOfHit == RayTraceResult.Type.BLOCK) {
                final EnumFacing side = pos.sideHit;
                final BlockPos hitPos = pos.getBlockPos().offset(side);

                if (!Platform.hasPermissions(new DimensionalCoord(w, hitPos), p)) {
                    return;
                }

                final Block whatsThere = w.getBlockState(hitPos).getBlock();
                if (whatsThere.isReplaceable(w, hitPos) && w.isAirBlock(hitPos)) {
                    AEApi.instance().definitions().blocks().paint().maybeBlock().ifPresent(paintBlock ->
                    {
                        w.setBlockState(hitPos, paintBlock.getDefaultState(), 3);
                    });
                }

                final TileEntity te = w.getTileEntity(hitPos);
                if (te instanceof TilePaint) {
                    final Vec3d hp = pos.hitVec.subtract(hitPos.getX(), hitPos.getY(), hitPos.getZ());
                    ((TilePaint) te).addBlot(type, side.getOpposite(), hp);
                }
            }
        }
    }

    private void standardAmmo(float penetration, final World w, final EntityPlayer p, final Vec3d Vec3d, final Vec3d Vec3d1, final Vec3d direction, final double d0, final double d1, final double d2) {
        boolean hasDestroyed = true;
        while (penetration > 0 && hasDestroyed) {
            hasDestroyed = false;

            final AxisAlignedBB bb = new AxisAlignedBB(Math.min(Vec3d.x, Vec3d1.x), Math.min(Vec3d.y, Vec3d1.y), Math.min(Vec3d.z, Vec3d1.z), Math
                    .max(Vec3d.x, Vec3d1.x), Math.max(Vec3d.y, Vec3d1.y), Math.max(Vec3d.z, Vec3d1.z)).grow(16, 16, 16);

            Entity entity = null;
            final List list = w.getEntitiesWithinAABBExcludingEntity(p, bb);
            double closest = 9999999.0D;

            for (int l = 0; l < list.size(); ++l) {
                final Entity entity1 = (Entity) list.get(l);

                if (!entity1.isDead && entity1 != p && !(entity1 instanceof EntityItem)) {
                    if (entity1.isEntityAlive()) {
                        // prevent killing / flying of mounts.
                        if (entity1.isRidingOrBeingRiddenBy(p)) {
                            continue;
                        }

                        final float f1 = 0.3F;

                        final AxisAlignedBB boundingBox = entity1.getEntityBoundingBox().grow(f1, f1, f1);
                        final RayTraceResult RayTraceResult = boundingBox.calculateIntercept(Vec3d, Vec3d1);

                        if (RayTraceResult != null) {
                            final double nd = Vec3d.squareDistanceTo(RayTraceResult.hitVec);

                            if (nd < closest) {
                                entity = entity1;
                                closest = nd;
                            }
                        }
                    }
                }
            }

            final Vec3d vec = new Vec3d(d0, d1, d2);
            RayTraceResult pos = w.rayTraceBlocks(Vec3d, Vec3d1, true);
            if (entity != null && pos != null && pos.hitVec.squareDistanceTo(vec) > closest) {
                pos = new RayTraceResult(entity);
            } else if (entity != null && pos == null) {
                pos = new RayTraceResult(entity);
            }

            try {
                AppEng.proxy.sendToAllNearExcept(null, d0, d1, d2, 128, w,
                        new PacketMatterCannon(d0, d1, d2, (float) direction.x, (float) direction.y, (float) direction.z, (byte) (pos == null ? 32 : pos.hitVec
                                .squareDistanceTo(vec) + 1)));
            } catch (final Exception err) {
                AELog.debug(err);
            }

            if (pos != null) {
                final DamageSource dmgSrc = DamageSource.causePlayerDamage(p);
                dmgSrc.damageType = "matter_cannon";

                if (pos.typeOfHit == RayTraceResult.Type.ENTITY) {
                    final int dmg = (int) Math.ceil(penetration / 20.0f);
                    if (pos.entityHit instanceof EntityLivingBase) {
                        final EntityLivingBase el = (EntityLivingBase) pos.entityHit;
                        penetration -= dmg;
                        el.knockBack(p, 0, -direction.x, -direction.z);
                        // el.knockBack( p, 0, Vec3d.x,
                        // Vec3d.z );
                        el.attackEntityFrom(dmgSrc, dmg);
                        if (!el.isEntityAlive()) {
                            hasDestroyed = true;
                        }
                    } else if (pos.entityHit instanceof EntityItem) {
                        hasDestroyed = true;
                        pos.entityHit.setDead();
                    } else if (pos.entityHit.attackEntityFrom(dmgSrc, dmg)) {
                        hasDestroyed = pos.entityHit.isEntityAlive();
                    }
                } else if (pos.typeOfHit == RayTraceResult.Type.BLOCK) {
                    if (!AEConfig.instance().isFeatureEnabled(AEFeature.MASS_CANNON_BLOCK_DAMAGE)) {
                        penetration = 0;
                    } else {
                        final IBlockState bs = w.getBlockState(pos.getBlockPos());
                        // int meta = w.getBlockMetadata(
                        // pos.blockX, pos.blockY, pos.blockZ );

                        final float hardness = bs.getBlockHardness(w, pos.getBlockPos()) * 9.0f;
                        if (hardness >= 0.0) {
                            if (penetration > hardness && Platform.hasPermissions(new DimensionalCoord(w, pos.getBlockPos()), p)) {
                                hasDestroyed = true;
                                penetration -= hardness;
                                penetration *= 0.60;
                                w.destroyBlock(pos.getBlockPos(), true);
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
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
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
        final float pen = AEApi.instance().registries().matterCannon().getPenetration(requestedAddition.createItemStack());
        if (pen > 0) {
            return false;
        }

        return !(requestedAddition.getItem() instanceof ItemPaintBall);
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
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }
}
