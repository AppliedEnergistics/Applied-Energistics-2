package appeng.client.guidebook;

import appeng.client.guidebook.document.block.LytDocument;
import net.minecraft.resources.ResourceLocation;

public class GuidePage {
    private final String sourcePack;
    private final ResourceLocation id;

    private LytDocument document;

    public GuidePage(String sourcePack, ResourceLocation id, LytDocument document) {
        this.sourcePack = sourcePack;
        this.id = id;
        this.document = document;
    }

    public String getSourcePack() {
        return sourcePack;
    }

    public ResourceLocation getId() {
        return id;
    }

    public LytDocument getDocument() {
        return document;
    }
}
