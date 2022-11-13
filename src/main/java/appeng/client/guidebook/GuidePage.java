package appeng.client.guidebook;

import appeng.client.guidebook.document.LytDocument;

public class GuidePage {
    private final String sourcePack;

    private LytDocument document;

    public GuidePage(String sourcePack, LytDocument document) {
        this.sourcePack = sourcePack;
        this.document = document;
    }

    public String getSourcePack() {
        return sourcePack;
    }

    public LytDocument getDocument() {
        return document;
    }
}
