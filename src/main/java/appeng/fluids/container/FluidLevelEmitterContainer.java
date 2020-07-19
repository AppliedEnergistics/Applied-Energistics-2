package appeng.fluids.container;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.client.gui.implementations.NumberEntryWidget;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerHelper;
import appeng.fluids.parts.FluidLevelEmitterPart;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidLevelEmitterContainer extends FluidConfigurableContainer {
    public static ContainerType<FluidLevelEmitterContainer> TYPE;

    private static final ContainerHelper<FluidLevelEmitterContainer, FluidLevelEmitterPart> helper = new ContainerHelper<>(
            FluidLevelEmitterContainer::new, FluidLevelEmitterPart.class, SecurityPermissions.BUILD);

    public static FluidLevelEmitterContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final FluidLevelEmitterPart lvlEmitter;

    @OnlyIn(Dist.CLIENT)
    private NumberEntryWidget textField;
    @GuiSync(3)
    public long EmitterValue = -1;

    public FluidLevelEmitterContainer(int id, final PlayerInventory ip, final FluidLevelEmitterPart te) {
        super(TYPE, id, ip, te);
        this.lvlEmitter = te;
    }

    @OnlyIn(Dist.CLIENT)
    public void setTextField(final NumberEntryWidget level) {
        this.textField = level;
        this.textField.setValue(this.EmitterValue);
    }

    public void setLevel(final long l, final PlayerEntity player) {
        this.lvlEmitter.setReportingValue(l);
        this.EmitterValue = l;
    }

    @Override
    protected void setupConfig() {
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {

        return 0;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.EmitterValue = this.lvlEmitter.getReportingValue();
            this.setRedStoneMode(
                    (RedstoneMode) this.getUpgradeable().getConfigManager().getSetting(Settings.REDSTONE_EMITTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("EmitterValue")) {
            if (this.textField != null) {
                this.textField.setValue(this.EmitterValue);
            }
        }
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.lvlEmitter.getConfig();
    }
}
