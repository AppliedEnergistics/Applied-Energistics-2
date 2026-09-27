package appeng.menu.guisync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.core.AppEng;

public record ClientboundInitialGuiSyncData(int containerId,
        Map<Integer, SynchronizedFieldHeader> fields) implements CustomPacketPayload {

    public static final Type<ClientboundInitialGuiSyncData> TYPE = new Type<>(AppEng.makeId("gui_sync_schema"));

    public static final StreamCodec<FriendlyByteBuf, Map<Integer, SynchronizedFieldHeader>> FIELDS_STREAM_CODEC = StreamCodec
            .of(
                    ClientboundInitialGuiSyncData::encodeFields,
                    ClientboundInitialGuiSyncData::decodeFields);

    public static final StreamCodec<FriendlyByteBuf, ClientboundInitialGuiSyncData> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.INT, ClientboundInitialGuiSyncData::containerId,
                    FIELDS_STREAM_CODEC, ClientboundInitialGuiSyncData::fields,
                    ClientboundInitialGuiSyncData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record SynchronizedFieldHeader(String className, String fieldName, byte[] initialData) {
    }

    private static void encodeFields(FriendlyByteBuf buffer, Map<Integer, SynchronizedFieldHeader> fields) {
        // Write out all classes
        List<String> classList = new ArrayList<>();
        Map<String, Integer> classIdMap = new HashMap<>();
        for (var field : fields.values()) {
            if (!classIdMap.containsKey(field.className)) {
                var classId = classList.size();
                classList.add(field.className);
                classIdMap.put(field.className, classId);
            }
        }
        buffer.writeCollection(classList, ByteBufCodecs.STRING_UTF8);

        // Now write out the fields
        buffer.writeInt(fields.size());
        for (var entry : fields.entrySet()) {
            buffer.writeInt(entry.getKey());
            var header = entry.getValue();
            buffer.writeInt(classIdMap.get(header.className));
            buffer.writeUtf(header.fieldName);
            buffer.writeByteArray(header.initialData);
        }
    }

    private static Map<Integer, SynchronizedFieldHeader> decodeFields(FriendlyByteBuf buffer) {
        var classNames = buffer.readCollection(ArrayList::new, ByteBufCodecs.STRING_UTF8);
        var fieldCount = buffer.readInt();
        var fields = new HashMap<Integer, SynchronizedFieldHeader>(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            var fieldId = buffer.readInt();
            var className = classNames.get(buffer.readInt());
            var fieldName = buffer.readUtf();
            var initialData = buffer.readByteArray();
            fields.put(fieldId, new SynchronizedFieldHeader(className, fieldName, initialData));
        }
        return fields;
    }
}
