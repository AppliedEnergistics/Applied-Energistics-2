package appeng.core.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
        clientbound(registrar, AssemblerAnimationPacket.TYPE, AssemblerAnimationPacket.STREAM_CODEC);
        clientbound(registrar, BlockTransitionEffectPacket.TYPE, BlockTransitionEffectPacket.STREAM_CODEC);
        clientbound(registrar, ClearPatternAccessTerminalPacket.TYPE, ClearPatternAccessTerminalPacket.STREAM_CODEC);
        clientbound(registrar, CompassResponsePacket.TYPE, CompassResponsePacket.STREAM_CODEC);
        clientbound(registrar, CraftConfirmPlanPacket.TYPE, CraftConfirmPlanPacket.STREAM_CODEC);
        clientbound(registrar, CraftingJobStatusPacket.TYPE, CraftingJobStatusPacket.STREAM_CODEC);
        clientbound(registrar, CraftingStatusPacket.TYPE, CraftingStatusPacket.STREAM_CODEC);
        clientbound(registrar, GuiDataSyncPacket.TYPE, GuiDataSyncPacket.STREAM_CODEC);
        clientbound(registrar, ItemTransitionEffectPacket.TYPE, ItemTransitionEffectPacket.STREAM_CODEC);
        clientbound(registrar, LightningPacket.TYPE, LightningPacket.STREAM_CODEC);
        clientbound(registrar, MatterCannonPacket.TYPE, MatterCannonPacket.STREAM_CODEC);
        clientbound(registrar, MEInventoryUpdatePacket.TYPE, MEInventoryUpdatePacket.STREAM_CODEC);
        clientbound(registrar, MockExplosionPacket.TYPE, MockExplosionPacket.STREAM_CODEC);
        clientbound(registrar, NetworkStatusPacket.TYPE, NetworkStatusPacket.STREAM_CODEC);
        clientbound(registrar, PatternAccessTerminalPacket.TYPE, PatternAccessTerminalPacket.STREAM_CODEC);
        clientbound(registrar, SetLinkStatusPacket.TYPE, SetLinkStatusPacket.STREAM_CODEC);

        // Serverbound
        serverbound(registrar, ColorApplicatorSelectColorPacket.TYPE, ColorApplicatorSelectColorPacket.STREAM_CODEC);
        serverbound(registrar, CompassRequestPacket.TYPE, CompassRequestPacket.STREAM_CODEC);
        serverbound(registrar, ConfigButtonPacket.TYPE, ConfigButtonPacket.STREAM_CODEC);
        serverbound(registrar, ConfirmAutoCraftPacket.TYPE, ConfirmAutoCraftPacket.STREAM_CODEC);
        serverbound(registrar, FillCraftingGridFromRecipePacket.TYPE, FillCraftingGridFromRecipePacket.STREAM_CODEC);
        serverbound(registrar, GuiActionPacket.TYPE, GuiActionPacket.STREAM_CODEC);
        serverbound(registrar, HotkeyPacket.TYPE, HotkeyPacket.STREAM_CODEC);
        serverbound(registrar, InventoryActionPacket.TYPE, InventoryActionPacket.STREAM_CODEC);
        serverbound(registrar, MEInteractionPacket.TYPE, MEInteractionPacket.STREAM_CODEC);
        serverbound(registrar, MouseWheelPacket.TYPE, MouseWheelPacket.STREAM_CODEC);
        serverbound(registrar, PartLeftClickPacket.TYPE, PartLeftClickPacket.STREAM_CODEC);
        serverbound(registrar, SelectKeyTypePacket.TYPE, SelectKeyTypePacket.STREAM_CODEC);
        serverbound(registrar, SwapSlotsPacket.TYPE, SwapSlotsPacket.STREAM_CODEC);
        serverbound(registrar, SwitchGuisPacket.TYPE, SwitchGuisPacket.STREAM_CODEC);

        // Bidirectional
        bidirectional(registrar, ConfigValuePacket.TYPE, ConfigValuePacket.STREAM_CODEC);
    }

    private static <T extends ClientboundPacket> void clientbound(IPayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.play(type, codec, builder -> builder.client(ClientboundPacket::handleOnClient));
    }

    private static <T extends ServerboundPacket> void serverbound(IPayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.play(type, codec, builder -> builder.server(ServerboundPacket::handleOnServer));
    }

    private static <T extends ServerboundPacket & ClientboundPacket> void bidirectional(IPayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.play(type, codec, builder -> {
            builder.client(ClientboundPacket::handleOnClient);
            builder.server(ServerboundPacket::handleOnServer);
        });
    }
}
