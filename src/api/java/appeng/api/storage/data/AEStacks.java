package appeng.api.storage.data;

public final class AEStacks {

    private AEStacks() {
    }

    public static <T extends IAEStack> T decStackSize(T stack, long count) {
        stack.decStackSize(count);
        return null;
    }
}
