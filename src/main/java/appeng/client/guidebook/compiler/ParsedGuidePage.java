package appeng.client.guidebook.compiler;

import appeng.libs.mdast.model.MdAstRoot;
import net.minecraft.resources.ResourceLocation;

public class ParsedGuidePage {
    final String sourcePack;
    final ResourceLocation id;
    final String source;
    final MdAstRoot astRoot;
    final Frontmatter frontmatter;

    public ParsedGuidePage(String sourcePack, ResourceLocation id, String source, MdAstRoot astRoot, Frontmatter frontmatter) {
        this.sourcePack = sourcePack;
        this.id = id;
        this.source = source;
        this.astRoot = astRoot;
        this.frontmatter = frontmatter;
    }

    public String getSourcePack() {
        return sourcePack;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Frontmatter getFrontmatter() {
        return frontmatter;
    }
}
