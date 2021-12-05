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

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.stacks.AEItemKey;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;

public class BiometricCardItem extends AEBaseItem implements IBiometricCard {
    public BiometricCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player p, final InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            this.encode(p.getItemInHand(hand), p);
            p.swing(hand);
            return InteractionResultHolder.success(p.getItemInHand(hand));
        }

        return InteractionResultHolder.pass(p.getItemInHand(hand));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack is, final Player player, final LivingEntity target,
            final InteractionHand hand) {
        if (target instanceof Player && !InteractionUtil.isInAlternateUseMode(player)) {
            if (player.isCreative()) {
                is = player.getItemInHand(hand);
            }
            this.encode(is, (Player) target);
            player.swing(hand);
            return InteractionResult.sidedSuccess(player.getCommandSenderWorld().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName(final ItemStack is) {
        var profile = this.getProfile(is);
        return profile != null ? super.getName(is).copy().append(" - " + profile.getName()) : super.getName(is);
    }

    private void encode(final ItemStack is, final Player p) {
        var profile = this.getProfile(is);

        if (profile != null && profile.equals(p.getGameProfile())) {
            this.setProfile(is, null);
        } else {
            this.setProfile(is, p.getGameProfile());
        }
    }

    @Override
    public void setProfile(final ItemStack itemStack, final GameProfile profile) {
        final CompoundTag tag = itemStack.getOrCreateTag();

        if (profile != null) {
            final CompoundTag pNBT = new CompoundTag();
            NbtUtils.writeGameProfile(pNBT, profile);
            tag.put("profile", pNBT);
        } else {
            tag.remove("profile");
        }
    }

    @Override
    public GameProfile getProfile(ItemStack is) {
        return getProfile(is.getTag());
    }

    @Nullable
    public GameProfile getProfile(AEItemKey key) {
        return getProfile(key.getTag());
    }

    private GameProfile getProfile(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("profile")) {
            return NbtUtils.readGameProfile(tag.getCompound("profile"));
        }
        return null;
    }

    @Override
    public EnumSet<SecurityPermissions> getPermissions(ItemStack is) {
        return getPermissions(is.getTag());
    }

    public EnumSet<SecurityPermissions> getPermissions(@Nullable CompoundTag tag) {
        var result = EnumSet.noneOf(SecurityPermissions.class);
        if (tag != null) {
            for (var sp : SecurityPermissions.values()) {
                if (tag.getBoolean(sp.name())) {
                    result.add(sp);
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasPermission(final ItemStack is, final SecurityPermissions permission) {
        final CompoundTag tag = is.getOrCreateTag();
        return tag.getBoolean(permission.name());
    }

    @Override
    public void removePermission(final ItemStack itemStack, final SecurityPermissions permission) {
        final CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(permission.name())) {
            tag.remove(permission.name());
        }
    }

    @Override
    public void addPermission(final ItemStack itemStack, final SecurityPermissions permission) {
        var tag = itemStack.getOrCreateTag();
        tag.putBoolean(permission.name(), true);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        final EnumSet<SecurityPermissions> perms = this.getPermissions(stack);
        if (perms.isEmpty()) {
            lines.add(new TranslatableComponent(GuiText.NoPermissions.getLocal()));
        } else {
            Component msg = null;

            for (final SecurityPermissions sp : perms) {
                if (msg == null) {
                    msg = sp.getDisplayName();
                } else {
                    msg = msg.copy().append(", ").append(sp.getDisplayName());
                }
            }
            lines.add(msg);
        }
    }
}
