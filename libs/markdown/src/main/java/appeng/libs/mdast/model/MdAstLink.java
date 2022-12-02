package appeng.libs.mdast.model;

import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Link includes Resource
 * <p>
 * Link (Parent) represents a hyperlink.
 * <p>
 * Link can be used where phrasing content is expected. Its content model is static phrasing content.
 * <p>
 * Link includes the mixin Resource.
 * <p>
 * For example, the following markdown:
 * <p>
 * [alpha](https://example.com "bravo")
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'link',
 * url: 'https://example.com',
 * title: 'bravo',
 * children: [{type: 'text', value: 'alpha'}]
 * }
 */
public class MdAstLink extends MdAstParent<MdAstStaticPhrasingContent> implements MdAstPhrasingContent, MdAstResource {
    public String url = "";
    public String title;

    public MdAstLink() {
        super("link");
    }

    @Override
    protected Class<MdAstStaticPhrasingContent> childClass() {
        return MdAstStaticPhrasingContent.class;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public @Nullable String title() {
        return title;
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        if (title != null) {
            writer.name("title").value(title);
        }
        writer.name("url").value(url);
        super.writeJson(writer);
    }
}
