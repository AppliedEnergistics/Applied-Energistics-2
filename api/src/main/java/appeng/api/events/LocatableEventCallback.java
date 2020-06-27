package appeng.api.events;

@FunctionalInterface
public interface LocatableEventCallback {

    void onLocatable(LocatableEventAnnounce.LocatableEvent evt);

}
