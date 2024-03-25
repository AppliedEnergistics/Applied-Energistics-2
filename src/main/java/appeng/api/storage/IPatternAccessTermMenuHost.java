package appeng.api.storage;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGridNode;
import appeng.api.util.IConfigurableObject;

public interface IPatternAccessTermMenuHost extends IConfigurableObject {
    @Nullable
    IGridNode getGridNode();

    ILinkStatus getLinkStatus();
}
