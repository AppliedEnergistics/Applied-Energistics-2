package appeng.client.guidebook.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.PageCollection;
import appeng.client.guidebook.extensions.ExtensionCollection;

@MockitoSettings
class LinkParserTest {
    private final List<PageAnchor> anchors = new ArrayList<>();
    private final List<URI> external = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private final LinkParser.Visitor visitor = new LinkParser.Visitor() {
        @Override
        public void handlePage(PageAnchor page) {
            anchors.add(page);
        }

        @Override
        public void handleExternal(URI uri) {
            external.add(uri);
        }

        @Override
        public void handleError(String error) {
            errors.add(error);
        }
    };
    private PageCompiler compiler;

    @BeforeEach
    void setUp() {
        var pageCollection = mock(PageCollection.class, withSettings().strictness(Strictness.LENIENT));
        when(pageCollection.pageExists(new ResourceLocation("ns:other/page.md"))).thenReturn(true);
        when(pageCollection.pageExists(new ResourceLocation("ns2:abc/def.md"))).thenReturn(true);
        compiler = new PageCompiler(pageCollection, ExtensionCollection.empty(), "pack",
                new ResourceLocation("ns:subfolder/page.md"), "");
    }

    @Test
    void testRelativeLink() {
        LinkParser.parseLink(compiler, "../other/page.md", visitor);
        assertThat(external).containsExactly();
        assertThat(errors).containsExactly();
        assertThat(anchors).containsExactly(PageAnchor.page(new ResourceLocation("ns:other/page.md")));
    }

    @Test
    void testRelativeLinkWithFragment() {
        LinkParser.parseLink(compiler, "../other/page.md#fragment", visitor);
        assertThat(external).containsExactly();
        assertThat(errors).containsExactly();
        assertThat(anchors).containsExactly(new PageAnchor(new ResourceLocation("ns:other/page.md"), "fragment"));
    }

    @Test
    void testAbsoluteLink() {
        LinkParser.parseLink(compiler, "/other/page.md", visitor);
        assertThat(external).containsExactly();
        assertThat(errors).containsExactly();
        assertThat(anchors).containsExactly(PageAnchor.page(new ResourceLocation("ns:other/page.md")));
    }

    @Test
    void testAbsoluteLinkWithFragment() {
        LinkParser.parseLink(compiler, "/other/page.md#fragment", visitor);
        assertThat(external).containsExactly();
        assertThat(errors).containsExactly();
        assertThat(anchors).containsExactly(new PageAnchor(new ResourceLocation("ns:other/page.md"), "fragment"));
    }

    @Test
    void testLinkToOtherNamespace() {
        LinkParser.parseLink(compiler, "ns2:abc/def.md", visitor);
        assertThat(external).containsExactly();
        assertThat(errors).containsExactly();
        assertThat(anchors).containsExactly(PageAnchor.page(new ResourceLocation("ns2:abc/def.md")));
    }

    @Test
    void testLinkToOtherNamespaceWithFragment() {
        LinkParser.parseLink(compiler, "ns2:abc/def.md#fragment", visitor);
        assertThat(external).containsExactly();
        assertThat(errors).containsExactly();
        assertThat(anchors).containsExactly(new PageAnchor(new ResourceLocation("ns2:abc/def.md"), "fragment"));
    }
}
