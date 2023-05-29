package appeng.client.guidebook.extensions;

/**
 * An extension point is offered by the guidebook to plug in additional functionality. Each extension point defines an
 * interface or base-class that needs to be implemented (or extended) by an extension.
 */
public record ExtensionPoint<T extends Extension> (Class<T> extensionPointClass) {
}
