package appeng.core.network.clientbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.client.render.crafting.AssemblerAnimationStatus;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

public record AssemblerAnimationPacket(BlockPos pos, byte rate, AEKey what) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, AssemblerAnimationPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    AssemblerAnimationPacket::write,
                    AssemblerAnimationPacket::decode);

    public static final Type<AssemblerAnimationPacket> TYPE = CustomAppEngPayload.createType("assembler_animation");

    @Override
    public Type<AssemblerAnimationPacket> type() {
        return TYPE;
    }

    public static AssemblerAnimationPacket decode(RegistryFriendlyByteBuf data) {
        var pos = data.readBlockPos();
        var rate = data.readByte();
        var what = AEKey.readKey(data);
        return new AssemblerAnimationPacket(pos, rate, what);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBlockPos(pos);
        data.writeByte(rate);
        AEKey.writeKey(data, what);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        BlockEntity te = player.getCommandSenderWorld().getBlockEntity(pos);
        if (te instanceof MolecularAssemblerBlockEntity ma) {
            ma.setAnimationStatus(new AssemblerAnimationStatus(rate, what.wrapForDisplayOrFilter()));
        }
    }
}
