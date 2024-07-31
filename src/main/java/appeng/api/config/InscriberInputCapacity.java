package appeng.api.config;

public enum InscriberInputCapacity {
    ONE(1),
    FOUR(4),
    SIXTY_FOUR(64);

    public final int capacity;

    InscriberInputCapacity(int capacity) {
        this.capacity = capacity;
    }
}
