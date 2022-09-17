package appeng.container.implementations;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.container.AEBaseContainer;
import appeng.helpers.ICustomNameObject;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerRenamer extends AEBaseContainer {
    private final ICustomNameObject namedObject;

    @SideOnly(Side.CLIENT)
    private MEGuiTextField textField;

    public ContainerRenamer(InventoryPlayer ip, ICustomNameObject obj) {
        super(ip, obj instanceof TileEntity ? (TileEntity) obj : null, obj instanceof IPart ? (IPart) obj : null);
        namedObject = obj;
    }

    @SideOnly(Side.CLIENT)
    public void setTextField(final MEGuiTextField name) {
        this.textField = name;
        if (getCustomName() != null) textField.setText(getCustomName());
    }

    public void setNewName(String newValue) {
        this.namedObject.setCustomName(newValue);

    }

    @Override
    public void setCustomName(final String customName) {
        super.setCustomName(customName);
        if (!Platform.isServer() && customName != null) textField.setText(customName);
    }

    @Override
    public void detectAndSendChanges() {
        verifyPermissions(SecurityPermissions.BUILD, false);
        super.detectAndSendChanges();
        if (!Platform.isServer() && getCustomName() != null) textField.setText(getCustomName());
    }
}
