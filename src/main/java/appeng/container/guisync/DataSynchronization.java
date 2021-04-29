package appeng.container.guisync;

import appeng.core.AELog;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for synchronizing fields from server-side containers to client-side containers.
 * Fields need to be annotated with {@link GuiSync} and given a unique key within the class
 * hierarchy.
 */
public class DataSynchronization {

    private final Map<Short, SynchronizedField<?>> fields = new HashMap<>();

    public DataSynchronization(Object host) {
        collectFields(host, host.getClass());
    }

    private void collectFields(Object host, Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(GuiSync.class)) {
                final GuiSync annotation = f.getAnnotation(GuiSync.class);
                short key = annotation.value();
                if (this.fields.containsKey(key)) {
                    throw new IllegalStateException("Class " + host.getClass() + " declares the same sync id twice: " + key);
                }
                this.fields.put(key, SynchronizedField.create(host, f));
            }
        }

        // Recurse upwards through the class hierarchy
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != Container.class && superclass != Object.class) {
            collectFields(host, superclass);
        }
    }

    public boolean hasChanges() {
        for (SynchronizedField<?> value : fields.values()) {
            if (value.hasChanges()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Write the data for all fields to the given buffer,
     * and marks all fields as unchanged.
     */
    public void writeFull(PacketBuffer data) {
        writeFields(data, true);
    }

    /**
     * Write the data for changed fields to the given buffer,
     * and marks all fields as unchanged.
     */
    public void writeUpdate(PacketBuffer data) {
        writeFields(data, false);
    }

    private void writeFields(PacketBuffer data, boolean includeUnchanged) {
        for (Map.Entry<Short, SynchronizedField<?>> entry : fields.entrySet()) {
            if (includeUnchanged || entry.getValue().hasChanges()) {
                data.writeShort(entry.getKey());
                entry.getValue().write(data);
            }
        }

        // Terminator
        data.writeVarInt(-1);
    }

    public void readUpdate(PacketBuffer data) {
        for (short key = data.readShort(); key != -1; key = data.readShort()) {
            SynchronizedField<?> field = fields.get(key);
            if (field == null) {
                AELog.warn("Server sent update for GUI field %d, which we don't know.", key);
                continue;
            }

            field.read(data);
        }
    }

    /**
     * @return True if any synchronized fields exist.
     */
    public boolean hasFields() {
        return !fields.isEmpty();
    }
}
