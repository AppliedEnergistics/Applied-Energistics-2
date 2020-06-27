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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import alexiil.mc.lib.attributes.item.ItemTransferable;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorItemHandler;

public final class MaterialItem extends AEBaseItem implements IStorageComponent, IUpgradeModule {

    /**
     * NBT property used by the name press to store the name to be inscribed.
     */
    public static final String TAG_INSCRIBE_NAME = "InscribeName";

    private static final int KILO_SCALAR = 1024;

    private final MaterialType materialType;

    public MaterialItem(Settings properties, MaterialType materialType) {
        super(properties);
        this.materialType = materialType;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
        super.appendTooltip(stack, world, lines, advancedTooltips);

        if (materialType == MaterialType.NAME_PRESS) {
            final CompoundTag c = stack.getOrCreateTag();
            if (c.contains(TAG_INSCRIBE_NAME)) {
                lines.add(new LiteralText(c.getString(TAG_INSCRIBE_NAME)));
            }
        }

        final Upgrades u = this.getType(stack);
        if (u != null) {
            final List<Text> textList = new ArrayList<>();
            for (final Entry<ItemStack, Integer> j : u.getSupported().entrySet()) {
                Text name = null;

                final int limit = j.getValue();

                if (j.getKey().getItem() instanceof IItemGroup) {
                    final IItemGroup ig = (IItemGroup) j.getKey().getItem();
                    final String str = ig.getUnlocalizedGroupName(u.getSupported().keySet(), j.getKey());
                    if (str != null) {
                        name = new TranslatableText(str).append(limit > 1 ? " (" + limit + ')' : "");
                    }
                }

                if (name == null) {
                    name = j.getKey().getName().append((limit > 1 ? " (" + limit + ')' : ""));
                }

                if (!textList.contains(name)) {
                    textList.add(name);
                }
            }

            final Pattern p = Pattern.compile("(\\d+)[^\\d]");
            // FIXME This comparison is not great...
            final SlightlyBetterSort s = new SlightlyBetterSort(p);
            textList.sort(s);
            lines.addAll(textList);
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
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        if (player.isInSneakingPose()) {
            final BlockEntity te = context.getWorld().getBlockEntity(context.getBlockPos());
            ItemTransferable upgrades = null;

            if (te instanceof IPartHost) {
                final SelectedPart sp = ((IPartHost) te).selectPart(context.getHitVec());
                if (sp.part instanceof IUpgradeableHost) {
                    upgrades = ((ISegmentedInventory) sp.part).getInventoryByName("upgrades");
                }
            } else if (te instanceof IUpgradeableHost) {
                upgrades = ((ISegmentedInventory) te).getInventoryByName("upgrades");
            }

            if (upgrades != null && !player.getStackInHand(hand).isEmpty()
                    && player.getStackInHand(hand).getItem() instanceof IUpgradeModule) {
                final IUpgradeModule um = (IUpgradeModule) player.getStackInHand(hand).getItem();
                final Upgrades u = um.getType(player.getStackInHand(hand));

                if (u != null) {
                    if (player.world.isClient) {
                        return ActionResult.PASS;
                    }

                    final InventoryAdaptor ad = new AdaptorItemHandler(upgrades);
                    player.setHeldItem(hand, ad.addItems(player.getStackInHand(hand)));
                    return ActionResult.SUCCESS;
                }
            }
        }

        return super.onItemUseFirst(stack, context);
    }

    @Override
    public boolean hasCustomEntity(final ItemStack is) {
        return materialType.hasCustomEntity();
    }

    @Override
    public Entity createEntity(final World w, final Entity location, final ItemStack itemstack) {
        final Class<? extends Entity> droppedEntity = materialType.getCustomEntityClass();
        final Entity eqi;

        try {
            eqi = droppedEntity.getConstructor(World.class, double.class, double.class, double.class, ItemStack.class)
                    .newInstance(w, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack);
        } catch (final Throwable t) {
            throw new IllegalStateException(t);
        }

        eqi.setMotion(location.getMotion());

        if (location instanceof ItemEntity && eqi instanceof ItemEntity) {
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

    private static class SlightlyBetterSort implements Comparator<Text> {
        private final Pattern pattern;

        public SlightlyBetterSort(final Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public int compare(final Text o1, final Text o2) {
            try {
                final Matcher a = this.pattern.matcher(o1.getString());
                final Matcher b = this.pattern.matcher(o2.getString());
                if (a.find() && b.find()) {
                    final int ia = Integer.parseInt(a.group(1));
                    final int ib = Integer.parseInt(b.group(1));
                    return Integer.compare(ia, ib);
                }
            } catch (final Throwable t) {
                // ek!
            }
            return o1.getString().compareTo(o2.getString());
        }
    }
}
