
package appeng.core.network.serverbound;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;

public record SwitchGuisPacket(
        @Nullable MenuType<? extends ISubMenu> newGui) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SwitchGuisPacket> STREAM_CODEC = StreamCodec.ofMember(
            SwitchGuisPacket::write,
            SwitchGuisPacket::decode);

    public static final Type<SwitchGuisPacket> TYPE = CustomAppEngPayload.createType("switch_guis");

    @Override
    public Type<SwitchGuisPacket> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    public static SwitchGuisPacket decode(RegistryFriendlyByteBuf stream) {
        MenuType<? extends ISubMenu> newGui = null;
        if (stream.readBoolean()) {
            newGui = (MenuType<? extends ISubMenu>) BuiltInRegistries.MENU.get(stream.readResourceLocation());
        }
        return new SwitchGuisPacket(newGui);
    }

    public void write(RegistryFriendlyByteBuf data) {
        if (newGui != null) {
            data.writeBoolean(true);
            data.writeResourceLocation(BuiltInRegistries.MENU.getKey(newGui));
        } else {
            data.writeBoolean(false);
        }
    }

    /**
     * Opens a sub-menu for the current menu, which will allow the player to return to the previous menu.
     */
    public static SwitchGuisPacket openSubMenu(MenuType<? extends ISubMenu> menuType) {
        return new SwitchGuisPacket(menuType);
    }

    /**
     * Creates a packet that instructs the server to return to the parent menu for the currently opened sub-menu.
     */
    public static SwitchGuisPacket returnToParentMenu() {
        return new SwitchGuisPacket((MenuType<? extends ISubMenu>) null);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (this.newGui != null) {
            doOpenSubMenu(player);
        } else {
            doReturnToParentMenu(player);
        }
    }

    private void doOpenSubMenu(ServerPlayer player) {
        if (player.containerMenu instanceof AEBaseMenu bc) {
            var locator = bc.getLocator();
            if (locator != null) {
                MenuOpener.open(newGui, player, locator);
            }
        }
    }

    private void doReturnToParentMenu(ServerPlayer player) {
        if (player.containerMenu instanceof ISubMenu subMenu) {
            subMenu.getHost().returnToMainMenu(player, subMenu);
        }
    }
}
