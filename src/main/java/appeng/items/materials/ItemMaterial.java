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


import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.features.IStackSrc;
import appeng.core.features.MaterialStackSrc;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ItemMaterial extends AEBaseItem implements IStorageComponent, IUpgradeModule, ICellWorkbenchItem {
    public static ItemMaterial instance;

    private static final int KILO_SCALAR = 1024;

    private final Map<Integer, MaterialType> dmgToMaterial = new HashMap<>();

    public ItemMaterial() {
        this.setHasSubtypes(true);
        instance = this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        final MaterialType mt = this.getTypeByStack(stack);
        if (mt == null) {
            return;
        }

        if (mt == MaterialType.NAME_PRESS) {
            final NBTTagCompound c = Platform.openNbtData(stack);
            lines.add(c.getString("InscribeName"));
        }

        final Upgrades u = this.getType(stack);
        if (u != null) {
            final List<String> textList = new ArrayList<>();
            for (final Entry<ItemStack, Integer> j : u.getSupported().entrySet()) {
                String name = null;

                final int limit = j.getValue();

                if (j.getKey().getItem() instanceof IItemGroup) {
                    final IItemGroup ig = (IItemGroup) j.getKey().getItem();
                    final String str = ig.getUnlocalizedGroupName(u.getSupported().keySet(), j.getKey());
                    if (str != null) {
                        name = Platform.gui_localize(str) + (limit > 1 ? " (" + limit + ')' : "");
                    }
                }

                if (name == null) {
                    name = j.getKey().getDisplayName() + (limit > 1 ? " (" + limit + ')' : "");
                }

                if (!textList.contains(name)) {
                    textList.add(name);
                }
            }

            final Pattern p = Pattern.compile("(\\d+)[^\\d]");
            final SlightlyBetterSort s = new SlightlyBetterSort(p);
            Collections.sort(textList, s);
            lines.addAll(textList);
        }
    }

    public MaterialType getTypeByStack(final ItemStack is) {
        MaterialType type = this.dmgToMaterial.get(is.getItemDamage());
        return (type != null) ? type : MaterialType.INVALID_TYPE;
    }

    @Override
    public Upgrades getType(final ItemStack itemstack) {
        switch (this.getTypeByStack(itemstack)) {
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
            case CARD_PATTERN_EXPANSION:
                return Upgrades.PATTERN_EXPANSION;
            case CARD_MAGNET:
                return Upgrades.MAGNET;
            case CARD_QUANTUM_LINK:
                return Upgrades.QUANTUM_LINK;
            default:
                return null;
        }
    }

    public IStackSrc createMaterial(final MaterialType mat) {
        Preconditions.checkState(!mat.isRegistered(), "Cannot create the same material twice.");

        boolean enabled = true;

        for (final AEFeature f : mat.getFeature()) {
            enabled = enabled && AEConfig.instance().isFeatureEnabled(f);
        }

        mat.setStackSrc(new MaterialStackSrc(mat, enabled));

        if (enabled) {
            mat.setItemInstance(this);
            mat.markReady();
            final int newMaterialNum = mat.getDamageValue();

            if (this.dmgToMaterial.get(newMaterialNum) == null) {
                this.dmgToMaterial.put(newMaterialNum, mat);
            } else {
                throw new IllegalStateException("Meta Overlap detected.");
            }
        }

        return mat.getStackSrc();
    }

    public void registerOredicts() {
        for (final MaterialType mt : ImmutableSet.copyOf(this.dmgToMaterial.values())) {
            if (mt.getOreName() != null) {
                final String[] names = mt.getOreName().split(",");

                for (final String name : names) {
                    OreDictionary.registerOre(name, mt.stack(1));
                }
            }
        }
    }

    @Override
    public String getUnlocalizedName(final ItemStack is) {
        return "item.appliedenergistics2.material." + this.nameOf(is).toLowerCase();
    }

    @Override
    protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        final List<MaterialType> types = Arrays.asList(MaterialType.values());
        Collections.sort(types, (o1, o2) -> o1.name().compareTo(o2.name()));

        for (final MaterialType mat : types) {
            if (mat.getDamageValue() >= 0 && mat.isRegistered() && mat.getItemInstance() == this) {
                itemStacks.add(new ItemStack(this, 1, mat.getDamageValue()));
            }
        }
    }

    @Override
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (player.isSneaking()) {
            final TileEntity te = world.getTileEntity(pos);
            IItemHandler upgrades = null;

            if (te instanceof IPartHost) {
                final SelectedPart sp = ((IPartHost) te).selectPart(new Vec3d(hitX, hitY, hitZ));
                if (sp.part instanceof IUpgradeableHost) {
                    upgrades = ((ISegmentedInventory) sp.part).getInventoryByName("upgrades");
                }
            } else if (te instanceof IUpgradeableHost) {
                upgrades = ((ISegmentedInventory) te).getInventoryByName("upgrades");
            }

            if (upgrades != null && !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IUpgradeModule) {
                final IUpgradeModule um = (IUpgradeModule) player.getHeldItem(hand).getItem();
                final Upgrades u = um.getType(player.getHeldItem(hand));

                if (u != null) {
                    if (player.world.isRemote) {
                        return EnumActionResult.PASS;
                    }

                    final InventoryAdaptor ad = new AdaptorItemHandler(upgrades);
                    player.setHeldItem(hand, ad.addItems(player.getHeldItem(hand)));
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    public boolean hasCustomEntity(final ItemStack is) {
        return this.getTypeByStack(is).hasCustomEntity();
    }

    @Override
    public Entity createEntity(final World w, final Entity location, final ItemStack itemstack) {
        final Class<? extends Entity> droppedEntity = this.getTypeByStack(itemstack).getCustomEntityClass();
        final Entity eqi;

        try {
            eqi = droppedEntity.getConstructor(World.class, double.class, double.class, double.class, ItemStack.class)
                    .newInstance(w, location.posX,
                            location.posY, location.posZ, itemstack);
        } catch (final Throwable t) {
            throw new IllegalStateException(t);
        }

        eqi.motionX = location.motionX;
        eqi.motionY = location.motionY;
        eqi.motionZ = location.motionZ;

        if (location instanceof EntityItem && eqi instanceof EntityItem) {
            ((EntityItem) eqi).setDefaultPickupDelay();
        }

        return eqi;
    }

    private String nameOf(final ItemStack is) {
        if (is.isEmpty()) {
            return "null";
        }

        final MaterialType mt = this.getTypeByStack(is);
        if (mt == null) {
            return "null";
        }

        return mt.name();
    }

    @Override
    public int getBytes(final ItemStack is) {
        switch (this.getTypeByStack(is)) {
            case CELL1K_PART:
                return KILO_SCALAR;
            case CELL4K_PART:
                return KILO_SCALAR * 4;
            case CELL16K_PART:
                return KILO_SCALAR * 16;
            case CELL64K_PART:
                return KILO_SCALAR * 64;
            default:
        }
        return 0;
    }

    @Override
    public boolean isStorageComponent(final ItemStack is) {
        switch (this.getTypeByStack(is)) {
            case CELL1K_PART:
            case CELL4K_PART:
            case CELL16K_PART:
            case CELL64K_PART:
                return true;
            default:
        }
        return false;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return is.isItemEqual(AEApi.instance().definitions().materials().cardMagnet().maybeStack(1).get());
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
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

    private static class SlightlyBetterSort implements Comparator<String> {
        private final Pattern pattern;

        public SlightlyBetterSort(final Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public int compare(final String o1, final String o2) {
            try {
                final Matcher a = this.pattern.matcher(o1);
                final Matcher b = this.pattern.matcher(o2);
                if (a.find() && b.find()) {
                    final int ia = Integer.parseInt(a.group(1));
                    final int ib = Integer.parseInt(b.group(1));
                    return Integer.compare(ia, ib);
                }
            } catch (final Throwable t) {
                // ek!
            }
            return o1.compareTo(o2);
        }
    }

}
