package appeng.util;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;

import appeng.api.networking.IGridNode;

/**
 * Interface for objects that allow themselves to be exported to a debug export.
 */
public interface IDebugExportable {
    void debugExport(JsonWriter writer, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds)
            throws IOException;
}
