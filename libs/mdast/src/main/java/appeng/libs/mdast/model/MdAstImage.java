package appeng.libs.mdast.model;

import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Image (Node) represents an image.
 * <p>
 * Image can be used where phrasing content is expected. It has no content model, but is described by its alt field.
 * <p>
 * Image includes the mixins Resource and Alternative.
 * <p>
 * For example, the following markdown:
 * <p>
 * ![alpha](https://example.com/favicon.ico "bravo")
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'image',
 * url: 'https://example.com/favicon.ico',
 * title: 'bravo',
 * alt: 'alpha'
 * }
 */
public class MdAstImage extends MdAstNode implements MdAstResource, MdAstAlternative, MdAstStaticPhrasingContent {
    public String alt;
    public String url = "";
    public String title;

    public MdAstImage() {
        super("image");
    }

    @Override
    public @Nullable String alt() {
        return alt;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public @Nullable String title() {
        return title;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void toText(StringBuilder buffer) {
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        if (title != null) {
            writer.name("title").value(title);
        }
        writer.name("url").value(url);
        if (alt != null) {
            writer.name("alt").value(alt);
        }
        super.writeJson(writer);
    }
}
