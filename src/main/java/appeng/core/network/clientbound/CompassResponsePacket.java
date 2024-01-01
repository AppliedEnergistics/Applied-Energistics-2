
package appeng.core.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.core.network.ClientboundPacket;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;

public record CompassResponsePacket(long attunement,
        int cx,
        int cz,
        int cdy,
        CompassResult cr) implements ClientboundPacket {

    public static CompassResponsePacket decode(FriendlyByteBuf stream) {
        var attunement = stream.readLong();
        var cx = stream.readInt();
        var cz = stream.readInt();
        var cdy = stream.readInt();

        var cr = new CompassResult(stream.readBoolean(), stream.readBoolean(), stream.readDouble());
        return new CompassResponsePacket(attunement, cx, cz, cdy, cr);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeLong(attunement);
        data.writeInt(cx);
        data.writeInt(cz);
        data.writeInt(cdy);

        data.writeBoolean(cr.isValidResult());
        data.writeBoolean(cr.isSpin());
        data.writeDouble(cr.getRad());
    }

    @Override
    public void handleOnClient(Player player) {
        CompassManager.INSTANCE.postResult(this.attunement, this.cx << 4, this.cdy << 5, this.cz << 4, this.cr);
    }
}
