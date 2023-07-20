package appeng.client.guidebook.indices;

import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonWriter;

import appeng.client.guidebook.GuidePageChange;
import appeng.client.guidebook.compiler.ParsedGuidePage;

/**
 * A page index is an index over all guidebook pages that will be automatically built when the guidebook is reloaded,
 * and when individual pages change.
 */
public interface PageIndex {
    String getName();

    /**
     * @return True if this index supports incremental updates via the {@link #update} method.
     */
    boolean supportsUpdate();

    /**
     * Fully rebuilds this index.
     */
    void rebuild(List<ParsedGuidePage> pages);

    /**
     * Applies an incremental update to this index.
     */
    void update(List<ParsedGuidePage> allPages,
            List<GuidePageChange> changes);

    /**
     * Serialize the index to JSON for export to the website.
     */
    void export(JsonWriter writer) throws IOException;

    @FunctionalInterface
    interface JsonSerializer<T> {
        void write(JsonWriter writer, T value) throws IOException;
    }
}
