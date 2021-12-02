package appeng.parts.automation;

import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.storage.data.AEKey;

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
    public long push(StackTransferContext context, AEKey what, long maxAmount, Actionable mode) {
        for (var strategy : strategies) {
            var result = strategy.push(context, what, maxAmount, mode);
            if (result > 0) {
                return result;
            }
        }
        return 0;
    }
}
