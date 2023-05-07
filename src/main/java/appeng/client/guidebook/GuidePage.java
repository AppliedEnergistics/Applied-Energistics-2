package appeng.client.guidebook;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.document.block.LytDocument;

public record GuidePage(String sourcePack, ResourceLocation id, LytDocument document) {
}
