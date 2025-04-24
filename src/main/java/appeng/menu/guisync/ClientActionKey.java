package appeng.menu.guisync;

public record ClientActionKey<T>(String name) {
    @Override
    public String toString() {
        return name;
    }
}
