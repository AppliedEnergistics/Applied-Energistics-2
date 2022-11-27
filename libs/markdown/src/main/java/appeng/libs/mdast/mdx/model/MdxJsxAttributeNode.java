package appeng.libs.mdast.mdx.model;

import appeng.libs.unist.UnistNode;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Potential attributes of {@link MdxJsxElementFields}
 */
public interface MdxJsxAttributeNode extends UnistNode {
    void toJson(JsonWriter writer) throws IOException;
}
