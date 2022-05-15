package appeng.api.config;

public enum ShowPatternProviders {
    /**
     * Show pattern providers that are not hidden in pattern access terminal.
     */
    VISIBLE,
    /**
     * Show pattern providers that are not hidden in pattern access terminal, and that were not full when the terminal
     * was opened / the setting was set.
     */
    NOT_FULL,
    /**
     * Show all pattern providers.
     */
    ALL
}
