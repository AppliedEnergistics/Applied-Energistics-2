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

package appeng.items.materials;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.hooks.AECustomEntityItem;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorFixedInv;

public final class MaterialItem extends AEBaseItem
        implements IStorageComponent, IUpgradeModule, AEToolItem, AECustomEntityItem {

    /**
     * NBT property used by the name press to store the name to be inscribed.
     */
    public static final String TAG_INSCRIBE_NAME = "InscribeName";

    private static final int KILO_SCALAR = 1024;

    private final MaterialType materialType;

    public MaterialItem(Properties properties, MaterialType materialType) {
        super(properties);
        this.materialType = materialType;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);

        if (materialType == MaterialType.NAME_PRESS) {
            final CompoundNBT c = stack.getOrCreateTag();
            if (c.contains(TAG_INSCRIBE_NAME)) {
                lines.add(new StringTextComponent(c.getString(TAG_INSCRIBE_NAME)));
            }
        }

        final Upgrades u = this.getType(stack);
        if (u != null) {
            lines.addAll(u.getTooltipLines());
        }
    }

    @Override
    public Upgrades getType(final ItemStack itemstack) {
        switch (materialType) {
            case CARD_CAPACITY:
                return Upgrades.CAPACITY;
            case CARD_FUZZY:
                return Upgrades.FUZZY;
            case CARD_REDSTONE:
                return Upgrades.REDSTONE;
            case CARD_SPEED:
                return Upgrades.SPEED;
            case CARD_INVERTER:
                return Upgrades.INVERTER;
            case CARD_CRAFTING:
                return Upgrades.CRAFTING;
            default:
                return null;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity user, Hand hand) {
        return super.onItemRightClick(world, user, hand);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        if (player.isCrouching()) {
            final TileEntity te = context.getWorld().getTileEntity(context.getPos());
            FixedItemInv upgrades = null;

            if (te instanceof IPartHost) {
                final SelectedPart sp = ((IPartHost) te).selectPart(context.getHitVec());
                if (sp.part instanceof IUpgradeableHost) {
                    upgrades = ((ISegmentedInventory) sp.part).getInventoryByName("upgrades");
                }
            } else if (te instanceof IUpgradeableHost) {
                upgrades = ((ISegmentedInventory) te).getInventoryByName("upgrades");
            }

            if (upgrades != null && !player.getHeldItem(hand).isEmpty()
                    && player.getHeldItem(hand).getItem() instanceof IUpgradeModule) {
                final IUpgradeModule um = (IUpgradeModule) player.getHeldItem(hand).getItem();
                final Upgrades u = um.getType(player.getHeldItem(hand));

                if (u != null) {
                    if (player.world.isRemote) {
                        return ActionResultType.PASS;
                    }

                    final InventoryAdaptor ad = new AdaptorFixedInv(upgrades);
                    player.setHeldItem(hand, ad.addItems(player.getHeldItem(hand)));
                    return ActionResultType.SUCCESS;
                }
            }
        }

        return ActionResultType.PASS;
    }

    @Override
    public Entity replaceItemEntity(ServerWorld world, ItemEntity itemEntity, ItemStack itemStack) {
        if (!materialType.hasCustomEntity()) {
            return itemEntity;
        }

        final Class<? extends Entity> droppedEntity = materialType.getCustomEntityClass();
        final Entity eqi;

        try {
            eqi = droppedEntity.getConstructor(World.class, double.class, double.class, double.class, ItemStack.class)
                    .newInstance(world, itemEntity.getPosX(), itemEntity.getPosY(), itemEntity.getPosZ(), itemStack);
        } catch (final Throwable t) {
            throw new IllegalStateException(t);
        }

        eqi.setMotion(itemEntity.getMotion());

        if (eqi instanceof ItemEntity) {
            ((ItemEntity) eqi).setDefaultPickupDelay();
        }

        return eqi;
    }

    @Override
    public int getBytes(final ItemStack is) {
        switch (materialType) {
            case ITEM_1K_CELL_COMPONENT:
                return KILO_SCALAR;
            case ITEM_4K_CELL_COMPONENT:
                return KILO_SCALAR * 4;
            case ITEM_16K_CELL_COMPONENT:
                return KILO_SCALAR * 16;
            case ITEM_64K_CELL_COMPONENT:
                return KILO_SCALAR * 64;
            default:
        }
        return 0;
    }

    @Override
    public boolean isStorageComponent(final ItemStack is) {
        switch (materialType) {
            case ITEM_1K_CELL_COMPONENT:
            case ITEM_4K_CELL_COMPONENT:
            case ITEM_16K_CELL_COMPONENT:
            case ITEM_64K_CELL_COMPONENT:
                return true;
            default:
        }
        return false;
    }

}
