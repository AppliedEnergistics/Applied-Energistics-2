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

package appeng.items.parts;


import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import appeng.core.features.ActivityState;
import appeng.core.features.ItemStackSrc;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;


public final class ItemPart extends AEBaseItem implements IPartItem, IItemGroup {
    private static final int INITIAL_REGISTERED_CAPACITY = PartType.values().length;
    private static final Comparator<Entry<Integer, PartTypeWithVariant>> REGISTERED_COMPARATOR = new RegisteredComparator();

    public static ItemPart instance;
    private final Map<Integer, PartTypeWithVariant> registered;

    public ItemPart() {
        this.registered = new HashMap<>(INITIAL_REGISTERED_CAPACITY);

        this.setHasSubtypes(true);

        instance = this;
    }

    @Nonnull
    public final ItemStackSrc createPart(final PartType mat) {
        Preconditions.checkNotNull(mat);

        return this.createPart(mat, 0);
    }

    @Nonnull
    public ItemStackSrc createPart(final PartType mat, final AEColor color) {
        Preconditions.checkNotNull(mat);
        Preconditions.checkNotNull(color);

        final int varID = color.ordinal();

        return this.createPart(mat, varID);
    }

    @Nonnull
    private ItemStackSrc createPart(final PartType mat, final int varID) {
        assert mat != null;
        assert varID >= 0;

        // verify
        for (final PartTypeWithVariant p : this.registered.values()) {
            if (p.part == mat && p.variant == varID) {
                throw new IllegalStateException("Cannot create the same material twice...");
            }
        }

        boolean enabled = mat.isEnabled();

        final int partDamage = mat.getBaseDamage() + varID;
        final ActivityState state = ActivityState.from(enabled);
        final ItemStackSrc output = new ItemStackSrc(this, partDamage, state);

        final PartTypeWithVariant pti = new PartTypeWithVariant(mat, varID);

        this.processMetaOverlap(enabled, partDamage, mat, pti);

        return output;
    }

    private void processMetaOverlap(final boolean enabled, final int partDamage, final PartType mat, final PartTypeWithVariant pti) {
        assert partDamage >= 0;
        assert mat != null;
        assert pti != null;

        final PartTypeWithVariant registeredPartType = this.registered.get(partDamage);
        if (registeredPartType != null) {
            throw new IllegalStateException("Meta Overlap detected with type " + mat + " and damage " + partDamage + ". Found " + registeredPartType + " there already.");
        }

        if (enabled) {
            this.registered.put(partDamage, pti);
        }
    }

    public int getDamageByType(final PartType t) {
        Preconditions.checkNotNull(t);

        for (final Entry<Integer, PartTypeWithVariant> pt : this.registered.entrySet()) {
            if (pt.getValue().part == t) {
                return pt.getKey();
            }
        }
        return -1;
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World w, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (this.getTypeByStack(player.getHeldItem(hand)) == PartType.INVALID_TYPE) {
            return EnumActionResult.FAIL;
        }

        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, w);
    }

    @Override
    public String getUnlocalizedName(final ItemStack is) {
        Preconditions.checkNotNull(is);
        return "item.appliedenergistics2.multi_part." + this.getTypeByStack(is).getUnlocalizedName().toLowerCase();
    }

    @Override
    public String getItemStackDisplayName(final ItemStack is) {
        final PartType pt = this.getTypeByStack(is);

        if (pt.isCable()) {
            final AEColor[] variants = AEColor.values();

            final int itemDamage = is.getItemDamage();
            final PartTypeWithVariant registeredPartType = this.registered.get(itemDamage);
            if (registeredPartType != null) {
                return super.getItemStackDisplayName(is) + " - " + variants[registeredPartType.variant].toString();
            }
        }

        if (pt.getExtraName() != null) {
            return super.getItemStackDisplayName(is) + " - " + pt.getExtraName().getLocal();
        }

        return super.getItemStackDisplayName(is);
    }

    @Override
    protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        final List<Entry<Integer, PartTypeWithVariant>> types = new ArrayList<>(this.registered.entrySet());
        Collections.sort(types, REGISTERED_COMPARATOR);

        for (final Entry<Integer, PartTypeWithVariant> part : types) {
            itemStacks.add(new ItemStack(this, 1, part.getKey()));
        }
    }

    @Nonnull
    public PartType getTypeByStack(final ItemStack is) {
        Preconditions.checkNotNull(is);

        final PartTypeWithVariant pt = this.registered.get(is.getItemDamage());
        if (pt != null) {
            return pt.part;
        }

        return PartType.INVALID_TYPE;
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(final ItemStack is) {
        final PartType type = this.getTypeByStack(is);
        final Class<? extends IPart> part = type.getPart();
        if (part == null) {
            return null;
        }

        try {
            if (type.getConstructor() == null) {
                type.setConstructor(part.getConstructor(ItemStack.class));
            }

            return type.getConstructor().newInstance(is);
        } catch (final InstantiationException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Unable to construct IBusPart from IBusItem : " + part
                    .getName() + " ; Possibly didn't have correct constructor( ItemStack )", e);
        }
    }

    public int variantOf(final int itemDamage) {
        final PartTypeWithVariant registeredPartType = this.registered.get(itemDamage);
        if (registeredPartType != null) {
            return registeredPartType.variant;
        }

        return 0;
    }

    @Nullable
    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> others, final ItemStack is) {
        boolean importBus = false;
        boolean importBusFluids = false;
        boolean exportBus = false;
        boolean exportBusFluids = false;
        boolean group = false;

        final PartType u = this.getTypeByStack(is);

        for (final ItemStack stack : others) {
            if (stack.getItem() == this) {
                final PartType pt = this.getTypeByStack(stack);
                switch (pt) {
                    case IMPORT_BUS:
                        importBus = true;
                        if (u == pt) {
                            group = true;
                        }
                        break;
                    case FLUID_IMPORT_BUS:
                        importBusFluids = true;
                        if (u == pt) {
                            group = true;
                        }
                        break;
                    case EXPORT_BUS:
                        exportBus = true;
                        if (u == pt) {
                            group = true;
                        }
                        break;
                    case FLUID_EXPORT_BUS:
                        exportBusFluids = true;
                        if (u == pt) {
                            group = true;
                        }
                        break;
                    default:
                }
            }
        }

        if (group && importBus && exportBus && (u == PartType.IMPORT_BUS || u == PartType.EXPORT_BUS)) {
            return GuiText.IOBuses.getUnlocalized();
        }
        if (group && importBusFluids && exportBusFluids && (u == PartType.FLUID_IMPORT_BUS || u == PartType.FLUID_EXPORT_BUS)) {
            return GuiText.IOBusesFluids.getUnlocalized();
        }

        return null;
    }

    private static final class PartTypeWithVariant {
        private final PartType part;
        private final int variant;

        private PartTypeWithVariant(final PartType part, final int variant) {
            assert part != null;
            assert variant >= 0;

            this.part = part;
            this.variant = variant;
        }

        @Override
        public String toString() {
            return "PartTypeWithVariant{" + "part=" + this.part + ", variant=" + this.variant + '}';
        }
    }

    private static final class RegisteredComparator implements Comparator<Entry<Integer, PartTypeWithVariant>> {
        @Override
        public int compare(final Entry<Integer, PartTypeWithVariant> o1, final Entry<Integer, PartTypeWithVariant> o2) {
            final String string1 = o1.getValue().part.name();
            final String string2 = o2.getValue().part.name();
            final int comparedString = string1.compareTo(string2);

            if (comparedString == 0) {
                return Integer.compare(o1.getKey(), o2.getKey());
            }

            return comparedString;
        }
    }

    public void registerOreDicts() {
        for (final PartTypeWithVariant mt : ImmutableSet.copyOf(this.registered.values())) {
            if (mt.part.getOreName() != null) {
                final String[] names = mt.part.getOreName().split(",");

                for (final String name : names) {
                    OreDictionary.registerOre(name, new ItemStack(this, 1, mt.part.getBaseDamage() + mt.variant));
                }
            }
        }
    }

}
