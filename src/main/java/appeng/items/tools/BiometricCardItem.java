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

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.security.ISecurityRegistry;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;

public class BiometricCardItem extends AEBaseItem implements IBiometricCard {
    public BiometricCardItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(final World w, final PlayerEntity p, final Hand hand) {
        if (p.isInSneakingPose()) {
            this.encode(p.getStackInHand(hand), p);
            p.swingHand(hand);
            return TypedActionResult.success(p.getStackInHand(hand));
        }

        return TypedActionResult.pass(p.getStackInHand(hand));
    }

    // FIXME FABRIC: Validate that this actually works about as well as the forge hook does
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity target, Hand hand) {
        if (target instanceof PlayerEntity && !user.isInSneakingPose()) {
            if (user.isCreative()) {
                stack = user.getStackInHand(hand);
            }
            this.encode(stack, (PlayerEntity) target);
            user.swingHand(hand);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public Text getName(final ItemStack is) {
        final GameProfile username = this.getProfile(is);
        return username != null ? super.getName(is).copy().append(" - " + username.getName())
                : super.getName(is);
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
        final CompoundTag tag = itemStack.getOrCreateTag();

        if (profile != null) {
            final CompoundTag pNBT = new CompoundTag();
            NbtHelper.fromGameProfile(pNBT, profile);
            tag.put("profile", pNBT);
        } else {
            tag.remove("profile");
        }
    }

    @Override
    public GameProfile getProfile(final ItemStack is) {
        final CompoundTag tag = is.getOrCreateTag();
        if (tag.contains("profile")) {
            return NbtHelper.toGameProfile(tag.getCompound("profile"));
        }
        return null;
    }

    @Override
    public EnumSet<SecurityPermissions> getPermissions(final ItemStack is) {
        final CompoundTag tag = is.getOrCreateTag();
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
        final CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean(permission.name(), true);
    }

    @Override
    public void registerPermissions(final ISecurityRegistry register, final IPlayerRegistry pr, final ItemStack is) {
        register.addPlayer(pr.getID(this.getProfile(is)), this.getPermissions(is));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
        final EnumSet<SecurityPermissions> perms = this.getPermissions(stack);
        if (perms.isEmpty()) {
            lines.add(new TranslatableText(GuiText.NoPermissions.getLocal()));
        } else {
            MutableText msg = null;

            for (final SecurityPermissions sp : perms) {
                if (msg == null) {
                    msg = new TranslatableText(sp.getTranslatedName());
                } else {
                    msg = msg.append(", ").append(new TranslatableText(sp.getTranslatedName()));
                }
            }
            lines.add(msg);
        }
    }
}
