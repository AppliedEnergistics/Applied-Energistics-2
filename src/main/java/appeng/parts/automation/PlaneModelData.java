package appeng.parts.automation;

import appeng.client.render.model.AEInternalModelData;

public class PlaneModelData extends AEInternalModelData {

    private final PlaneConnections connections;

    public PlaneModelData(PlaneConnections connections) {
        this.connections = connections;
    }

    public PlaneConnections getConnections() {
        return connections;
    }

}
