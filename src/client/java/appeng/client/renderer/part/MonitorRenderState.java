package appeng.client.renderer.part;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import appeng.api.orientation.BlockOrientation;
import appeng.client.api.renderer.parts.PartRenderState;

public class MonitorRenderState extends PartRenderState {
    public ItemStackRenderState item = new ItemStackRenderState();
    public BlockOrientation orientation;
    public boolean canCraft;
    public long amount;
    public int textColor;
}
