package appeng.api.behaviors;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;

public class ContainerItemContext {
    // slight generic abuse
    private final ContainerItemStrategy<AEKey, Object> strategy;
    private final Object context;
    // used for sanity checking
    private final AEKeyType type;

    protected ContainerItemContext(ContainerItemStrategy<AEKey, Object> strategy, Object context, AEKeyType type) {
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
