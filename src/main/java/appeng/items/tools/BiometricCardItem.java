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

package appeng.items.tools;

import java.util.EnumSet;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.security.ISecurityRegistry;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class BiometricCardItem extends AEBaseItem implements IBiometricCard {
    public BiometricCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity p, final Hand hand) {
        if (Platform.isEntityHoldingShift(p)) {
            this.encode(p.getHeldItem(hand), p);
            p.swingArm(hand);
            return ActionResult.resultSuccess(p.getHeldItem(hand));
        }

        return ActionResult.resultPass(p.getHeldItem(hand));
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack is, final PlayerEntity player, final LivingEntity target,
            final Hand hand) {
        if (target instanceof PlayerEntity && !Platform.isEntityHoldingShift(player)) {
            if (player.isCreative()) {
                is = player.getHeldItem(hand);
            }
            this.encode(is, (PlayerEntity) target);
            player.swingArm(hand);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public ITextComponent getDisplayName(final ItemStack is) {
        final GameProfile username = this.getProfile(is);
        return username != null ? super.getDisplayName(is).deepCopy().appendString(" - " + username.getName())
                : super.getDisplayName(is);
    }

    private void encode(final ItemStack is, final PlayerEntity p) {
        final GameProfile username = this.getProfile(is);

        if (username != null && username.equals(p.getGameProfile())) {
            this.setProfile(is, null);
        } else {
            this.setProfile(is, p.getGameProfile());
        }
    }

    @Override
    public void setProfile(final ItemStack itemStack, final GameProfile profile) {
        final CompoundNBT tag = itemStack.getOrCreateTag();

        if (profile != null) {
            final CompoundNBT pNBT = new CompoundNBT();
            NBTUtil.writeGameProfile(pNBT, profile);
            tag.put("profile", pNBT);
        } else {
            tag.remove("profile");
        }
    }

    @Override
    public GameProfile getProfile(final ItemStack is) {
        final CompoundNBT tag = is.getOrCreateTag();
        if (tag.contains("profile")) {
            return NBTUtil.readGameProfile(tag.getCompound("profile"));
        }
        return null;
    }

    @Override
    public EnumSet<SecurityPermissions> getPermissions(final ItemStack is) {
        final CompoundNBT tag = is.getOrCreateTag();
        final EnumSet<SecurityPermissions> result = EnumSet.noneOf(SecurityPermissions.class);

        for (final SecurityPermissions sp : SecurityPermissions.values()) {
            if (tag.getBoolean(sp.name())) {
                result.add(sp);
            }
        }

        return result;
    }

    @Override
    public boolean hasPermission(final ItemStack is, final SecurityPermissions permission) {
        final CompoundNBT tag = is.getOrCreateTag();
        return tag.getBoolean(permission.name());
    }

    @Override
    public void removePermission(final ItemStack itemStack, final SecurityPermissions permission) {
        final CompoundNBT tag = itemStack.getOrCreateTag();
        if (tag.contains(permission.name())) {
            tag.remove(permission.name());
        }
    }

    @Override
    public void addPermission(final ItemStack itemStack, final SecurityPermissions permission) {
        final CompoundNBT tag = itemStack.getOrCreateTag();
        tag.putBoolean(permission.name(), true);
    }

    @Override
    public void registerPermissions(final ISecurityRegistry register, final IPlayerRegistry pr, final ItemStack is) {
        register.addPlayer(pr.getID(this.getProfile(is)), this.getPermissions(is));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        final EnumSet<SecurityPermissions> perms = this.getPermissions(stack);
        if (perms.isEmpty()) {
            lines.add(new TranslationTextComponent(GuiText.NoPermissions.getLocal()));
        } else {
            ITextComponent msg = null;

            for (final SecurityPermissions sp : perms) {
                if (msg == null) {
                    msg = new TranslationTextComponent(sp.getTranslatedName());
                } else {
                    msg = msg.deepCopy().appendString(", ")
                            .append(new TranslationTextComponent(sp.getTranslatedName()));
                }
            }
            lines.add(msg);
        }
    }
}
