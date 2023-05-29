package appeng.client.guidebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import appeng.client.guidebook.extensions.Extension;
import appeng.client.guidebook.extensions.ExtensionCollection;
import appeng.client.guidebook.extensions.ExtensionPoint;

class ExtensionCollectionTest {
    static class TestExtension implements Extension {
    }

    ExtensionPoint<TestExtension> EXTENSION_POINT1 = new ExtensionPoint<>(TestExtension.class);
    ExtensionPoint<TestExtension> EXTENSION_POINT2 = new ExtensionPoint<>(TestExtension.class);
    TestExtension EXTENSION_1 = new TestExtension();
    TestExtension EXTENSION_2 = new TestExtension();

    @Test
    void testGetUnregisteredExtensionPoint() {
        assertEquals(List.of(), ExtensionCollection.empty().get(EXTENSION_POINT1));
    }

    @Test
    void testEmptyCollection() {
        ExtensionCollection collection = ExtensionCollection.empty();
        assertTrue(collection.getExtensionPoints().isEmpty());
    }

    @Test
    void testAddSingleExtension() {
        ExtensionCollection collection = ExtensionCollection.builder()
                .add(EXTENSION_POINT1, EXTENSION_1)
                .build();

        assertEquals(1, collection.getExtensionPoints().size());
        assertEquals(List.of(EXTENSION_1), collection.get(EXTENSION_POINT1));
    }

    @Test
    void testAddMultipleExtensions() {
        ExtensionCollection collection = ExtensionCollection.builder()
                .add(EXTENSION_POINT1, EXTENSION_1)
                .add(EXTENSION_POINT1, EXTENSION_2)
                .build();

        assertEquals(1, collection.getExtensionPoints().size());
        assertEquals(List.of(EXTENSION_1, EXTENSION_2), collection.get(EXTENSION_POINT1));
    }

    @Test
    void testAddMultipleExtensionPoints() {
        ExtensionCollection collection = ExtensionCollection.builder()
                .add(EXTENSION_POINT1, EXTENSION_1)
                .add(EXTENSION_POINT2, EXTENSION_2)
                .build();

        assertEquals(2, collection.getExtensionPoints().size());
        assertEquals(List.of(EXTENSION_1), collection.get(EXTENSION_POINT1));
        assertEquals(List.of(EXTENSION_2), collection.get(EXTENSION_POINT2));
    }

    @Test
    void testExtensionAlreadyRegistered() {
        assertThrows(IllegalStateException.class, () -> ExtensionCollection.builder()
                .add(EXTENSION_POINT1, EXTENSION_1)
                .add(EXTENSION_POINT1, EXTENSION_1)
                .build());
    }

    @Test
    void testMergeCollections() {
        ExtensionCollection collection1 = ExtensionCollection.builder()
                .add(EXTENSION_POINT1, EXTENSION_1)
                .build();

        ExtensionCollection collection2 = ExtensionCollection.builder()
                .add(EXTENSION_POINT2, EXTENSION_2)
                .build();

        var mergedCollection = ExtensionCollection.builder()
                .addAll(collection1)
                .addAll(collection2)
                .build();

        assertEquals(2, mergedCollection.getExtensionPoints().size());
        assertEquals(List.of(EXTENSION_1), mergedCollection.get(EXTENSION_POINT1));
        assertEquals(List.of(EXTENSION_2), mergedCollection.get(EXTENSION_POINT2));
    }
}
