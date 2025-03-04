package appeng.crafting.pattern;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ItemStackMap;

import appeng.api.crafting.PatternDetailsTooltip;
import appeng.core.network.request.RequestManager;

public final class ClientPatternCache {
    private static final Map<ItemStack, CompletableFuture<PatternDetailsTooltip>> requests = ItemStackMap
            .createTypeAndTagMap();

    private ClientPatternCache() {
    }

    public static void clearCache() {
        System.out.println();
    }

    @Nullable
    public static PatternDetailsTooltip getTooltip(ItemStack patternItem) {
        var response = requests.computeIfAbsent(patternItem, stack -> {
            return RequestManager.getInstance().requestDecodePattern(stack).thenApply(reply -> {
                if (reply.tooltip() != null) {
                    return reply.tooltip();
                } else {
                    throw new RuntimeException();
                }
            });
        });

        try {
            return response.getNow(null);
        } catch (CompletionException e) {
            return null;
        }
    }
}
