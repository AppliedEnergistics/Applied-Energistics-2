package appeng.menu.guisync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;

import java.lang.reflect.InvocationTargetException;

/**
 * Implement on classes to signal they can be synchronized to the client using {@link GuiSync}. For this to work fully,
 * the class also needs to have a public constructor that takes a {@link FriendlyByteBuf} argument.
 */
public interface PacketWritable {
    void writeToPacket(RegistryFriendlyByteBuf data);

    static <T extends PacketWritable> StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec(Class<T> subclass) {
        StreamDecoder<RegistryFriendlyByteBuf, T> decoder;

        try {
            var constructor = subclass.getConstructor(RegistryFriendlyByteBuf.class);
            decoder = (RegistryFriendlyByteBuf buffer) -> {
                try {
                    return constructor.newInstance(buffer);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to deserialize " + subclass, e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No constructor taking RegistryFriendlyByteBuf on " + subclass);
        }

        return StreamCodec.ofMember(PacketWritable::writeToPacket, decoder);
    }
}
