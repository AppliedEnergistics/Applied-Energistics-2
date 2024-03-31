package appeng.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import appeng.core.AppEng;
import appeng.core.network.bidirectional.ConfigValuePacket;
import appeng.core.network.clientbound.AssemblerAnimationPacket;
import appeng.core.network.clientbound.BlockTransitionEffectPacket;
import appeng.core.network.clientbound.ClearPatternAccessTerminalPacket;
import appeng.core.network.clientbound.CompassResponsePacket;
import appeng.core.network.clientbound.CraftConfirmPlanPacket;
import appeng.core.network.clientbound.CraftingJobStatusPacket;
import appeng.core.network.clientbound.CraftingStatusPacket;
import appeng.core.network.clientbound.ExportedGridContent;
import appeng.core.network.clientbound.GuiDataSyncPacket;
import appeng.core.network.clientbound.ItemTransitionEffectPacket;
import appeng.core.network.clientbound.LightningPacket;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.core.network.clientbound.MatterCannonPacket;
import appeng.core.network.clientbound.MockExplosionPacket;
import appeng.core.network.clientbound.NetworkStatusPacket;
import appeng.core.network.clientbound.PatternAccessTerminalPacket;
import appeng.core.network.clientbound.SetLinkStatusPacket;
import appeng.core.network.serverbound.ColorApplicatorSelectColorPacket;
import appeng.core.network.serverbound.CompassRequestPacket;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.core.network.serverbound.ConfirmAutoCraftPacket;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.core.network.serverbound.GuiActionPacket;
import appeng.core.network.serverbound.HotkeyPacket;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.core.network.serverbound.MEInteractionPacket;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.core.network.serverbound.PartLeftClickPacket;
import appeng.core.network.serverbound.SelectKeyTypePacket;
import appeng.core.network.serverbound.SwapSlotsPacket;
import appeng.core.network.serverbound.SwitchGuisPacket;

public class InitNetwork {
    public static void init(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar(AppEng.MOD_ID);

        // Clientbound
        clientbound(registrar, AssemblerAnimationPacket.class, AssemblerAnimationPacket::decode);
        clientbound(registrar, BlockTransitionEffectPacket.class, BlockTransitionEffectPacket::decode);
        clientbound(registrar, ClearPatternAccessTerminalPacket.class, ClearPatternAccessTerminalPacket::decode);
        clientbound(registrar, CompassResponsePacket.class, CompassResponsePacket::decode);
        clientbound(registrar, CraftConfirmPlanPacket.class, CraftConfirmPlanPacket::decode);
        clientbound(registrar, CraftingJobStatusPacket.class, CraftingJobStatusPacket::decode);
        clientbound(registrar, CraftingStatusPacket.class, CraftingStatusPacket::decode);
        clientbound(registrar, GuiDataSyncPacket.class, GuiDataSyncPacket::decode);
        clientbound(registrar, ItemTransitionEffectPacket.class, ItemTransitionEffectPacket::decode);
        clientbound(registrar, LightningPacket.class, LightningPacket::decode);
        clientbound(registrar, MatterCannonPacket.class, MatterCannonPacket::decode);
        clientbound(registrar, MEInventoryUpdatePacket.class, MEInventoryUpdatePacket::decode);
        clientbound(registrar, MockExplosionPacket.class, MockExplosionPacket::decode);
        clientbound(registrar, NetworkStatusPacket.class, NetworkStatusPacket::decode);
        clientbound(registrar, PatternAccessTerminalPacket.class, PatternAccessTerminalPacket::decode);
        clientbound(registrar, SetLinkStatusPacket.class, SetLinkStatusPacket::decode);
        clientbound(registrar, ExportedGridContent.class, ExportedGridContent::decode);

        // Serverbound
        serverbound(registrar, ColorApplicatorSelectColorPacket.class, ColorApplicatorSelectColorPacket::decode);
        serverbound(registrar, CompassRequestPacket.class, CompassRequestPacket::decode);
        serverbound(registrar, ConfigButtonPacket.class, ConfigButtonPacket::decode);
        serverbound(registrar, ConfirmAutoCraftPacket.class, ConfirmAutoCraftPacket::decode);
        serverbound(registrar, FillCraftingGridFromRecipePacket.class, FillCraftingGridFromRecipePacket::decode);
        serverbound(registrar, GuiActionPacket.class, GuiActionPacket::decode);
        serverbound(registrar, HotkeyPacket.class, HotkeyPacket::decode);
        serverbound(registrar, InventoryActionPacket.class, InventoryActionPacket::decode);
        serverbound(registrar, MEInteractionPacket.class, MEInteractionPacket::decode);
        serverbound(registrar, MouseWheelPacket.class, MouseWheelPacket::decode);
        serverbound(registrar, PartLeftClickPacket.class, PartLeftClickPacket::decode);
        serverbound(registrar, SelectKeyTypePacket.class, SelectKeyTypePacket::decode);
        serverbound(registrar, SwapSlotsPacket.class, SwapSlotsPacket::decode);
        serverbound(registrar, SwitchGuisPacket.class, SwitchGuisPacket::decode);

        // Bidirectional
        bidirectional(registrar, ConfigValuePacket.class, ConfigValuePacket::decode);
    }

    private static <T extends ClientboundPacket> void clientbound(IPayloadRegistrar registrar, Class<T> packetClass,
            FriendlyByteBuf.Reader<T> reader) {
        var id = CustomAppEngPayload.makeId(packetClass);
        registrar.play(id, reader, builder -> builder.client(ClientboundPacket::handleOnClient));
    }

    private static <T extends ServerboundPacket> void serverbound(IPayloadRegistrar registrar, Class<T> packetClass,
            FriendlyByteBuf.Reader<T> reader) {
        var id = CustomAppEngPayload.makeId(packetClass);
        registrar.play(id, reader, builder -> builder.server(ServerboundPacket::handleOnServer));
    }

    private static <T extends ServerboundPacket & ClientboundPacket> void bidirectional(IPayloadRegistrar registrar,
            Class<T> packetClass, FriendlyByteBuf.Reader<T> reader) {
        var id = CustomAppEngPayload.makeId(packetClass);
        registrar.play(id, reader, builder -> {
            builder.client(ClientboundPacket::handleOnClient);
            builder.server(ServerboundPacket::handleOnServer);
        });
    }
}
