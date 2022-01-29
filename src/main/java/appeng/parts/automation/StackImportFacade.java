package appeng.parts.automation;

import java.util.List;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;

/**
 * Simply iterates over a list of {@link StackImportStrategy} and exposes them as a single strategy. First come, first
 * serve.
 */
public class StackImportFacade implements StackImportStrategy {
    private final List<StackImportStrategy> strategies;

    public StackImportFacade(List<StackImportStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        for (var strategy : strategies) {
            if (strategy.transfer(context)) {
                return true;
            }
        }
        return true;
    }
}
