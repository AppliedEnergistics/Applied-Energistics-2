package appeng.api.behaviors;

import java.util.Map;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;

public final class ContainerItemContext {
    private final Map<AEKeyType, Entry<?>> entries;

    ContainerItemContext(Map<AEKeyType, Entry<?>> entries) {
        this.entries = entries;
    }

    public @Nullable GenericStack getExtractableContent() {
        for (var entry : entries.entrySet()) {
            var content = entry.getValue().getExtractableContent();
            if (content != null) {
                return content;
            }
        }
        return null;
    }

    private Entry<?> getEntry(AEKey key) {
        var keyType = key.getType();
        Preconditions.checkArgument(entries.containsKey(keyType), "Internal logic error: mismatched key and type");
        return entries.get(keyType);
    }

    public long insert(AEKey key, long amount, Actionable mode) {
        return getEntry(key).insert(key, amount, mode);
    }

    public long extract(AEKey key, long amount, Actionable mode) {
        return getEntry(key).extract(key, amount, mode);
    }

    public void playFillSound(Player player, AEKey key) {
        getEntry(key).playFillSound(player, key);
    }

    public void playEmptySound(Player player, AEKey key) {
        getEntry(key).playEmptySound(player, key);
    }

    static class Entry<C> {
        // slight generic abuse
        private final ContainerItemStrategy<AEKey, C> strategy;
        private final C context;
        // used for sanity checking
        private final AEKeyType type;

        Entry(ContainerItemStrategy<AEKey, C> strategy, C context, AEKeyType type) {
            this.strategy = strategy;
            this.context = context;
            this.type = type;
        }

        public @Nullable GenericStack getExtractableContent() {
            return strategy.getExtractableContent(context);
        }

        public long insert(AEKey key, long amount, Actionable mode) {
            Preconditions.checkArgument(type.contains(key), "Internal logic error: mismatched key and type");
            return strategy.insert(context, key, amount, mode);
        }

        public long extract(AEKey key, long amount, Actionable mode) {
            Preconditions.checkArgument(type.contains(key), "Internal logic error: mismatched key and type");
            return strategy.extract(context, key, amount, mode);
        }

        public void playFillSound(Player player, AEKey what) {
            strategy.playFillSound(player, what);
        }

        public void playEmptySound(Player player, AEKey what) {
            strategy.playEmptySound(player, what);
        }

    }
}
