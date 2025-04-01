
package appeng.core.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

public record MockExplosionPacket(double x, double y, double z) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, MockExplosionPacket> STREAM_CODEC = StreamCodec.ofMember(
            MockExplosionPacket::write,
            MockExplosionPacket::decode);

    public static final Type<MockExplosionPacket> TYPE = CustomAppEngPayload.createType("mock_explosion");

    @Override
    public Type<MockExplosionPacket> type() {
        return TYPE;
    }

    public static MockExplosionPacket decode(RegistryFriendlyByteBuf data) {
        var x = data.readDouble();
        var y = data.readDouble();
        var z = data.readDouble();
        return new MockExplosionPacket(x, y, z);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeDouble(x);
        data.writeDouble(y);
        data.writeDouble(z);
    }
}
