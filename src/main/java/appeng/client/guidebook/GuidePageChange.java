package appeng.client.guidebook;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.compiler.ParsedGuidePage;

public record GuidePageChange(
        ResourceLocation pageId,
        @Nullable ParsedGuidePage oldPage,
        @Nullable ParsedGuidePage newPage) {
}
