package appeng.core.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import appeng.core.AppEng;
import appeng.core.network.bidirectional.ConfigValuePacket;
import appeng.core.network.clientbound.BlockTransitionEffectPacket;
import appeng.core.network.clientbound.ClearPatternAccessTerminalPacket;
import appeng.core.network.clientbound.CompassResponsePacket;
import appeng.core.network.clientbound.CraftConfirmPlanPacket;
import appeng.core.network.clientbound.CraftingJobStatusPacket;
import appeng.core.network.clientbound.CraftingStatusPacket;
import appeng.core.network.clientbound.ExportedGridContent;
import appeng.core.network.clientbound.GuiDataSyncPacket;
import appeng.core.network.clientbound.ItemTransitionEffectPacket;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.core.network.clientbound.MatterCannonPacket;
import appeng.core.network.clientbound.MockExplosionPacket;
import appeng.core.network.clientbound.MolecularAssemblerAnimationPacket;
import appeng.core.network.clientbound.NetworkStatusPacket;
import appeng.core.network.clientbound.PatternAccessTerminalPacket;
import appeng.core.network.clientbound.SetLinkStatusPacket;
import appeng.core.network.serverbound.ColorApplicatorSelectColorPacket;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.core.network.serverbound.ConfirmAutoCraftPacket;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.core.network.serverbound.GuiActionPacket;
import appeng.core.network.serverbound.HotkeyPacket;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.core.network.serverbound.MEInteractionPacket;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.core.network.serverbound.PartLeftClickPacket;
import appeng.core.network.serverbound.QuickMovePatternPacket;
import appeng.core.network.serverbound.RequestClosestMeteoritePacket;
import appeng.core.network.serverbound.SelectKeyTypePacket;
import appeng.core.network.serverbound.SwapSlotsPacket;
import appeng.core.network.serverbound.SwitchGuisPacket;
import appeng.core.network.serverbound.UpdateHoldingCtrlPacket;

public class InitNetwork {
    public static void init(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(AppEng.MOD_ID);

        // Clientbound
        registrar.playToClient(MolecularAssemblerAnimationPacket.TYPE, MolecularAssemblerAnimationPacket.STREAM_CODEC);
        registrar.playToClient(BlockTransitionEffectPacket.TYPE, BlockTransitionEffectPacket.STREAM_CODEC);
        registrar.playToClient(ClearPatternAccessTerminalPacket.TYPE, ClearPatternAccessTerminalPacket.STREAM_CODEC);
        registrar.playToClient(CompassResponsePacket.TYPE, CompassResponsePacket.STREAM_CODEC);
        registrar.playToClient(CraftConfirmPlanPacket.TYPE, CraftConfirmPlanPacket.STREAM_CODEC);
        registrar.playToClient(CraftingJobStatusPacket.TYPE, CraftingJobStatusPacket.STREAM_CODEC);
        registrar.playToClient(CraftingStatusPacket.TYPE, CraftingStatusPacket.STREAM_CODEC);
        registrar.playToClient(GuiDataSyncPacket.TYPE, GuiDataSyncPacket.STREAM_CODEC);
        registrar.playToClient(ItemTransitionEffectPacket.TYPE, ItemTransitionEffectPacket.STREAM_CODEC);
        registrar.playToClient(MatterCannonPacket.TYPE, MatterCannonPacket.STREAM_CODEC);
        registrar.playToClient(MEInventoryUpdatePacket.TYPE, MEInventoryUpdatePacket.STREAM_CODEC);
        registrar.playToClient(MockExplosionPacket.TYPE, MockExplosionPacket.STREAM_CODEC);
        registrar.playToClient(NetworkStatusPacket.TYPE, NetworkStatusPacket.STREAM_CODEC);
        registrar.playToClient(PatternAccessTerminalPacket.TYPE, PatternAccessTerminalPacket.STREAM_CODEC);
        registrar.playToClient(SetLinkStatusPacket.TYPE, SetLinkStatusPacket.STREAM_CODEC);
        registrar.playToClient(ExportedGridContent.TYPE, ExportedGridContent.STREAM_CODEC);

        // Serverbound
        serverbound(registrar, ColorApplicatorSelectColorPacket.TYPE, ColorApplicatorSelectColorPacket.STREAM_CODEC);
        serverbound(registrar, RequestClosestMeteoritePacket.TYPE, RequestClosestMeteoritePacket.STREAM_CODEC);
        serverbound(registrar, ConfigButtonPacket.TYPE, ConfigButtonPacket.STREAM_CODEC);
        serverbound(registrar, ConfirmAutoCraftPacket.TYPE, ConfirmAutoCraftPacket.STREAM_CODEC);
        serverbound(registrar, FillCraftingGridFromRecipePacket.TYPE, FillCraftingGridFromRecipePacket.STREAM_CODEC);
        serverbound(registrar, GuiActionPacket.TYPE, GuiActionPacket.STREAM_CODEC);
        serverbound(registrar, HotkeyPacket.TYPE, HotkeyPacket.STREAM_CODEC);
        serverbound(registrar, InventoryActionPacket.TYPE, InventoryActionPacket.STREAM_CODEC);
        serverbound(registrar, MEInteractionPacket.TYPE, MEInteractionPacket.STREAM_CODEC);
        serverbound(registrar, MouseWheelPacket.TYPE, MouseWheelPacket.STREAM_CODEC);
        serverbound(registrar, PartLeftClickPacket.TYPE, PartLeftClickPacket.STREAM_CODEC);
        serverbound(registrar, QuickMovePatternPacket.TYPE, QuickMovePatternPacket.STREAM_CODEC);
        serverbound(registrar, SelectKeyTypePacket.TYPE, SelectKeyTypePacket.STREAM_CODEC);
        serverbound(registrar, SwapSlotsPacket.TYPE, SwapSlotsPacket.STREAM_CODEC);
        serverbound(registrar, SwitchGuisPacket.TYPE, SwitchGuisPacket.STREAM_CODEC);
        serverbound(registrar, UpdateHoldingCtrlPacket.TYPE, UpdateHoldingCtrlPacket.STREAM_CODEC);

        // Bidirectional
        bidirectional(registrar, ConfigValuePacket.TYPE, ConfigValuePacket.STREAM_CODEC);
    }

    private static <T extends ServerboundPacket> void serverbound(PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.playToServer(type, codec, ServerboundPacket::handleOnServer);
    }

    private static <T extends ServerboundPacket & ClientboundPacket> void bidirectional(PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.playBidirectional(type, codec, ServerboundPacket::handleOnServer);
    }
}
