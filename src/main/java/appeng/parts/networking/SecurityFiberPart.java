package appeng.parts.networking;

import java.util.EnumSet;
import java.util.Map;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.Locatables;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.parts.IPartItem;
import appeng.blockentity.misc.SecurityStationBlockEntity;
import appeng.me.service.SecurityService;

public class SecurityFiberPart extends QuartzFiberPart {
    public SecurityFiberPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        connectSecurity(player.getLevel());
    }

    private void connectSecurity(Level level) {
        connectSecurity(getMainNode(), outerNode, level);
        connectSecurity(outerNode, getMainNode(), level);
    }

    private static void connectSecurity(IManagedGridNode from, IManagedGridNode to, Level level) {
        to.addService(ISecurityProvider.class, new SecurityBridge(from, level));
    }

    private static class SecurityBridge implements ISecurityProvider {

        private final IManagedGridNode node;
        private final Level level;

        public SecurityBridge(IManagedGridNode from, Level level) {
            node = from;
            this.level = level;
        }

        public SecurityStationBlockEntity getSecurityStation() {
            var securityService = getSecurityService();
            if (securityService == null)
                return null;
            return (SecurityStationBlockEntity) Locatables.securityStations().get(level,
                    securityService.getSecurityKey());
        }

        private SecurityService getSecurityService() {
            if (node.getGrid() == null)
                return null;
            if (!(node.getGrid().getSecurityService() instanceof SecurityService securityService))
                return null;
            return securityService;
        }

        @Override
        public long getSecurityKey() {
            var securityStation = getSecurityStation();
            if (securityStation == null)
                return 0;
            return securityStation.getSecurityKey();
        }

        @Override
        public void readPermissions(Map<Integer, EnumSet<SecurityPermissions>> playerPerms) {
        }

        @Override
        public boolean isSecurityEnabled() {
            var securityStation = getSecurityStation();
            if (securityStation == null)
                return false;
            return securityStation.isSecurityEnabled();
        }

        @Override
        public int getOwner() {
            var securityStation = getSecurityStation();
            if (securityStation == null)
                return -1;
            return securityStation.getOwner();
        }
    }
}
