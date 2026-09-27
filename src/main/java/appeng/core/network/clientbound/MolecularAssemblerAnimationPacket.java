package appeng.core.network.clientbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import appeng.api.stacks.AEKey;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

public record MolecularAssemblerAnimationPacket(BlockPos pos, byte rate, AEKey what) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, MolecularAssemblerAnimationPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    MolecularAssemblerAnimationPacket::write,
                    MolecularAssemblerAnimationPacket::decode);

    public static final Type<MolecularAssemblerAnimationPacket> TYPE = CustomAppEngPayload
            .createType("assembler_animation");

    @Override
    public Type<MolecularAssemblerAnimationPacket> type() {
        return TYPE;
    }

    public static MolecularAssemblerAnimationPacket decode(RegistryFriendlyByteBuf data) {
        var pos = data.readBlockPos();
        var rate = data.readByte();
        var what = AEKey.readKey(data);
        return new MolecularAssemblerAnimationPacket(pos, rate, what);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBlockPos(pos);
        data.writeByte(rate);
        AEKey.writeKey(data, what);
    }
}
