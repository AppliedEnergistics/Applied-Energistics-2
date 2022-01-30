package appeng.menu.me.interaction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

public class ContainerItemContext {
    // slight generic abuse
    private final ContainerItemStrategy<AEKey, Object> strategy;
    private final Object context;

    protected ContainerItemContext(ContainerItemStrategy<AEKey, Object> strategy, Object context) {
        this.strategy = strategy;
        this.context = context;
    }

    public @Nullable GenericStack getExtractableContent() {
        return strategy.getExtractableContent(context);
    }

    public long insert(AEKey key, long amount, Actionable mode) {
        return strategy.insert(context, key, amount, mode);
    }

    public long extract(AEKey key, long amount, Actionable mode) {
        return strategy.extract(context, key, amount, mode);
    }

    public void playFillSound(Player player, AEKey what) {
        strategy.playFillSound(player, what);
    }

    public void playEmptySound(Player player, AEKey what) {
        strategy.playEmptySound(player, what);
    }
}
