package appeng.parts.automation;

import java.util.List;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

/**
 * Simply iterates over a list of {@link StackExportStrategy} and exposes them as a single strategy. First come, first
 * serve.
 */
public class StackExportFacade implements StackExportStrategy {
    private final List<StackExportStrategy> strategies;

    public StackExportFacade(List<StackExportStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long maxAmount) {
        for (var strategy : strategies) {
            var result = strategy.transfer(context, what, maxAmount);
            if (result > 0) {
                return result;
            }
        }
        return 0;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        for (var strategy : strategies) {
            var result = strategy.push(what, amount, mode);
            if (result > 0) {
                return result;
            }
        }
        return 0;
    }
}
