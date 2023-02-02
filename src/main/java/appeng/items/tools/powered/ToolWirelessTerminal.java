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
import appeng.api.config.*;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.materials.ItemMaterial;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")

public class ToolWirelessTerminal extends AEBasePoweredItem implements IWirelessTermHandler, IBauble {

    int magnetTick;

    public ToolWirelessTerminal() {
        super(AEConfig.instance().getWirelessTerminalBattery());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final EntityPlayer player, final EnumHand hand) {
        AEApi.instance().registries().wireless().openWirelessTerminalGui(player.getHeldItem(hand), w, player);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        if (stack.hasTagCompound()) {
            final NBTTagCompound tag = Platform.openNbtData(stack);
            if (tag != null) {
                final String encKey = tag.getString("encryptionKey");

                if (encKey == null || encKey.isEmpty()) {
                    lines.add(GuiText.Unlinked.getLocal());
                } else {
                    lines.add(GuiText.Linked.getLocal());
                }
            }
        } else {
            lines.add(I18n.translateToLocal("AppEng.GuiITooltip.Unlinked"));
        }
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return AEApi.instance().definitions().items().wirelessTerminal().isSameAs(is);
    }

    @Override
    public boolean usePower(final EntityPlayer player, final double amount, final ItemStack is) {
        return this.extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    @Override
    public boolean hasPower(final EntityPlayer player, final double amt, final ItemStack is) {
        return this.getAECurrentPower(is) >= amt;
    }

    @Override
    public IConfigManager getConfigManager(final ItemStack target) {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) ->
        {
            final NBTTagCompound data = Platform.openNbtData(target);
            manager.writeToNBT(data);
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(Platform.openNbtData(target).copy());
        return out;
    }

    @Override
    public String getEncryptionKey(final ItemStack item) {
        final NBTTagCompound tag = Platform.openNbtData(item);
        return tag.getString("encryptionKey");
    }

    @Override
    public void setEncryptionKey(final ItemStack item, final String encKey, final String name) {
        final NBTTagCompound tag = Platform.openNbtData(item);
        tag.setString("encryptionKey", encKey);
        tag.setString("name", name);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
        if (Platform.isServer()) {
            magnetLogic(stack, worldIn, entityIn);
        }
    }

    @Override
    public IGuiHandler getGuiHandler(ItemStack is) {
        return GuiBridge.GUI_WIRELESS_TERM;
    }

    @Optional.Method(modid = "baubles")
    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (Platform.isServer()) {
            magnetLogic(itemstack, player.world, player);
        }
    }

    public void magnetLogic(ItemStack stack, World worldIn, Entity entityIn) {
        if (entityIn instanceof EntityPlayer) {
            this.magnetTick++;
            if (magnetTick % 5 != 0) {
                return;
            }
            magnetTick = 0;
            if (!entityIn.isSneaking()) {
                NBTTagCompound upgradeNBT = Platform.openNbtData(stack).getCompoundTag("upgrades");
                ItemStackHandler siu = new ItemStackHandler(0);
                siu.deserializeNBT(upgradeNBT);
                for (int s = 0; s < siu.getSlots(); s++) {
                    ItemStack is = siu.getStackInSlot(s);
                    if (AEApi.instance().definitions().materials().cardMagnet().isSameAs(is)) {
                        ItemMaterial im = (ItemMaterial) is.getItem();
                        CellConfig c = (CellConfig) im.getConfigInventory(is);
                        CellUpgrades u = (CellUpgrades) im.getUpgradesInventory(is);
                        FuzzyMode fz = null;
                        boolean isFuzzy = u.getInstalledUpgrades(Upgrades.FUZZY) == 1;
                        if (isFuzzy) {
                            fz = im.getFuzzyMode(is);
                        }
                        boolean inverted = u.getInstalledUpgrades(Upgrades.INVERTER) == 1;

                        List<EntityItem> ei = worldIn.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
                                new Vec3d(entityIn.posX + 5, entityIn.posY + 5, entityIn.posZ + 5),
                                new Vec3d(entityIn.posX - 5, entityIn.posY - 5, entityIn.posZ - 5)));
                        boolean emptyFilter = true;
                        for (EntityItem i : ei) {
                            if (i.isDead) {
                                continue;
                            }

                            NBTTagCompound itemTag = i.getEntityData();
                            if (itemTag.hasKey("PreventRemoteMovement")) {
                                continue;
                            }

                            if (i.getThrower().equals(entityIn.getName()) && i.cannotPickup()) {
                                continue;
                            }

                            boolean matched = false;
                            for (int ss = 0; ss < c.getSlots(); ss++) {
                                ItemStack filter = c.getStackInSlot(ss);
                                if (filter.isEmpty()) continue;
                                emptyFilter = false;
                                if (isFuzzy) {
                                    if (Platform.itemComparisons().isFuzzyEqualItem(filter, i.getItem(), fz)) {
                                        matched = true;
                                        break;
                                    }
                                } else {
                                    if (Platform.itemComparisons().isSameItem(filter, i.getItem())) {
                                        matched = true;
                                        break;
                                    }
                                }
                            }
                            if (emptyFilter) {
                                teleportItem(i,entityIn);
                            } else if (matched && !inverted) {
                                teleportItem(i,entityIn);
                            } else if (!matched && inverted) {
                                teleportItem(i,entityIn);
                            }
                        }
                    }
                }
            }
        }
    }

    private void teleportItem(EntityItem i, Entity entityIn){
        i.motionX = i.motionY = i.motionZ = 0;
        i.setPosition(entityIn.posX, entityIn.posY, entityIn.posZ);
    }
}
