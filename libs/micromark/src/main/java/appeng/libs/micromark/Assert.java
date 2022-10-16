package appeng.libs.micromark;

public final class Assert {
    private Assert() {
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
