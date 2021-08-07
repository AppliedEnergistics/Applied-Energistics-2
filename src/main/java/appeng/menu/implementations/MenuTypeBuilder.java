/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.menu.implementations;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.util.Platform;

/**
 * Builder that allows creation of menu types which can be opened from multiple types of hosts.
 *
 * @param <M>
 */
public final class MenuTypeBuilder<M extends AEBaseMenu, I> {

    private final Class<I> hostInterface;

    private final MenuFactory<M, I> factory;

    private Function<I, Component> menuTitleStrategy = this::getDefaultMenuTitle;

    @Nullable
    private SecurityPermissions requiredPermission;

    @Nullable
    private InitialDataSerializer<I> initialDataSerializer;

    @Nullable
    private InitialDataDeserializer<M, I> initialDataDeserializer;

    private MenuType<M> menuType;

    private MenuTypeBuilder(Class<I> hostInterface, TypedMenuFactory<M, I> typedFactory) {
        this.hostInterface = hostInterface;
        this.factory = (windowId, playerInv, accessObj) -> typedFactory.create(menuType, windowId, playerInv,
                accessObj);
    }

    private MenuTypeBuilder(Class<I> hostInterface, MenuFactory<M, I> factory) {
        this.hostInterface = hostInterface;
        this.factory = factory;
    }

    public static <C extends AEBaseMenu, I> MenuTypeBuilder<C, I> create(MenuFactory<C, I> factory,
            Class<I> hostInterface) {
        return new MenuTypeBuilder<>(hostInterface, factory);
    }

    public static <C extends AEBaseMenu, I> MenuTypeBuilder<C, I> create(TypedMenuFactory<C, I> factory,
            Class<I> hostInterface) {
        return new MenuTypeBuilder<>(hostInterface, factory);
    }

    /**
     * Requires that the player has a certain permission on the block entity to open the menu.
     */
    public MenuTypeBuilder<M, I> requirePermission(SecurityPermissions permission) {
        this.requiredPermission = permission;
        return this;
    }

    /**
     * Specifies a custom strategy for obtaining a custom menu name.
     * <p>
     * The stratgy should return {@link TextComponent#EMPTY} if there's no custom name.
     */
    public MenuTypeBuilder<M, I> withMenuTitle(Function<I, Component> menuTitleStrategy) {
        this.menuTitleStrategy = menuTitleStrategy;
        return this;
    }

    /**
     * Sets a serializer and deserializer for additional data that should be transmitted from server->client when the
     * menu is being first opened.
     */
    public MenuTypeBuilder<M, I> withInitialData(InitialDataSerializer<I> initialDataSerializer,
            InitialDataDeserializer<M, I> initialDataDeserializer) {
        this.initialDataSerializer = initialDataSerializer;
        this.initialDataDeserializer = initialDataDeserializer;
        return this;
    }

    /**
     * Opens a menu that is based around a single block entity. The block entity's position is encoded in the packet
     * buffer.
     */
    private M fromNetwork(int windowId, Inventory inv, FriendlyByteBuf packetBuf) {
        I host = getHostFromLocator(inv.player, MenuLocator.read(packetBuf));
        if (host != null) {
            M menu = factory.create(windowId, inv, host);
            if (initialDataDeserializer != null) {
                initialDataDeserializer.deserializeInitialData(host, menu, packetBuf);
            }
            return menu;
        }
        return null;
    }

    private boolean open(Player player, MenuLocator locator) {
        if (!(player instanceof ServerPlayer)) {
            // Cannot open menus on the client or for non-players
            // FIXME logging?
            return false;
        }

        I accessInterface = getHostFromLocator(player, locator);

        if (accessInterface == null) {
            return false;
        }

        if (!checkPermission(player, accessInterface)) {
            return false;
        }

        Component title = menuTitleStrategy.apply(accessInterface);

        MenuProvider menu = new SimpleMenuProvider((wnd, p, pl) -> {
            M m = factory.create(wnd, p, accessInterface);
            // Set the original locator on the opened server-side menu for it to more
            // easily remember how to re-open after being closed.
            m.setLocator(locator);
            return m;
        }, title);
        NetworkHooks.openGui((ServerPlayer) player, menu, buffer -> {
            locator.write(buffer);
            if (initialDataSerializer != null) {
                initialDataSerializer.serializeInitialData(accessInterface, buffer);
            }
        });

        return true;
    }

    private I getHostFromLocator(Player player, MenuLocator locator) {
        if (locator.hasItemIndex()) {
            return getHostFromPlayerInventory(player, locator);
        }

        if (!locator.hasBlockPos()) {
            return null; // No block was clicked
        }

        BlockEntity blockEntity = player.level.getBlockEntity(locator.getBlockPos());

        // The block entity itself can host a terminal (i.e. Chest!)
        if (hostInterface.isInstance(blockEntity)) {
            return hostInterface.cast(blockEntity);
        }

        if (!locator.hasSide()) {
            return null;
        }

        if (blockEntity instanceof IPartHost) {
            // But it could also be a part attached to the block entity
            IPartHost partHost = (IPartHost) blockEntity;
            IPart part = partHost.getPart(locator.getSide());
            if (part == null) {
                return null;
            }

            if (hostInterface.isInstance(part)) {
                return hostInterface.cast(part);
            } else {
                AELog.debug("Trying to open a menu @ %s for a %s, but the menu requires %s", locator,
                        part.getClass(), hostInterface);
                return null;
            }
        } else {
            // FIXME: Logging? Dont know how to obtain the terminal host
            return null;
        }
    }

    private I getHostFromPlayerInventory(Player player, MenuLocator locator) {

        ItemStack it = player.getInventory().getItem(locator.getItemIndex());

        if (it.isEmpty()) {
            AELog.debug("Cannot open menu for player %s since they no longer hold the item in slot %d", player,
                    locator.hasItemIndex());
            return null;
        }

        if (it.getItem() instanceof IGuiItem) {
            IGuiItem guiItem = (IGuiItem) it.getItem();
            // Optionally contains the block the item was used on to open the menu
            BlockPos blockPos = locator.hasBlockPos() ? locator.getBlockPos() : null;
            IGuiItemObject guiObject = guiItem.getGuiObject(it, locator.getItemIndex(), player.level, blockPos);
            if (hostInterface.isInstance(guiObject)) {
                return hostInterface.cast(guiObject);
            }
        }

        if (hostInterface.isAssignableFrom(WirelessTerminalGuiObject.class)) {
            final IWirelessTermHandler wh = Api.instance().registries().wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return hostInterface.cast(new WirelessTerminalGuiObject(wh, it, player, locator.getItemIndex()));
            }
        }

        return null;
    }

    /**
     * Creates a menu type that uses this helper as a factory and network deserializer.
     */
    public MenuType<M> build(String id) {
        Preconditions.checkState(menuType == null, "build was already called");

        menuType = IForgeContainerType.create(this::fromNetwork);
        menuType.setRegistryName(AppEng.MOD_ID, id);
        MenuOpener.addOpener(menuType, this::open);
        return menuType;
    }

    @FunctionalInterface
    public interface MenuFactory<C, I> {
        C create(int windowId, Inventory playerInv, I accessObj);
    }

    @FunctionalInterface
    public interface TypedMenuFactory<C extends AbstractContainerMenu, I> {
        C create(MenuType<C> type, int windowId, Inventory playerInv, I accessObj);
    }

    /**
     * Strategy used to serialize initial data for opening the menu on the client-side into the packet that is sent to
     * the client.
     */
    @FunctionalInterface
    public interface InitialDataSerializer<I> {
        void serializeInitialData(I host, FriendlyByteBuf buffer);
    }

    /**
     * Strategy used to deserialize initial data for opening the menu on the client-side from the packet received by the
     * server.
     */
    @FunctionalInterface
    public interface InitialDataDeserializer<C, I> {
        void deserializeInitialData(I host, C menu, FriendlyByteBuf buffer);
    }

    private boolean checkPermission(Player player, Object accessInterface) {

        if (requiredPermission != null) {
            return Platform.checkPermissions(player, accessInterface, requiredPermission, true);
        }

        return true;

    }

    private Component getDefaultMenuTitle(I accessInterface) {
        if (accessInterface instanceof ICustomNameObject) {
            ICustomNameObject customNameObject = (ICustomNameObject) accessInterface;
            if (customNameObject.hasCustomInventoryName()) {
                return customNameObject.getCustomInventoryName();
            }
        }

        return TextComponent.EMPTY;
    }

}
