package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.LytRect;

import java.util.Objects;
import java.util.stream.Stream;

record Line(LytRect bounds, LineElement firstElement) {
    Stream<LineElement> elements() {
        return Stream.iterate(firstElement, Objects::nonNull, el -> el.next);
    }
}
