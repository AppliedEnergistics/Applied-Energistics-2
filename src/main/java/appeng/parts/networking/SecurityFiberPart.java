package appeng.parts.networking;

import java.util.EnumSet;
import java.util.Map;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.parts.IPartItem;
import appeng.me.service.SecurityService;

public class SecurityFiberPart extends QuartzFiberPart {
    public SecurityFiberPart(IPartItem<?> partItem) {
        super(partItem);
        if (isSecurityServiceAbsent(outerNode))
            outerNode.addService(ISecurityProvider.class, new SecurityBridge(getMainNode()));
        else if (isSecurityServiceAbsent(getMainNode()))
            getMainNode().addService(ISecurityProvider.class, new SecurityBridge(outerNode));
    }

    private static SecurityService getSecurityService(IManagedGridNode node) {
        if (node.getGrid() == null)
            return null;
        if (!(node.getGrid().getSecurityService() instanceof SecurityService securityService))
            return null;
        return securityService;
    }

    private static boolean isSecurityServiceAbsent(IManagedGridNode node) {
        SecurityService securityService = getSecurityService(node);
        if (securityService == null)
            return true;
        return !securityService.isAvailable();
    }

    private static class SecurityBridge implements ISecurityProvider {

        private final IManagedGridNode node;

        public SecurityBridge(IManagedGridNode from) {
            node = from;
        }

        private SecurityService getMainSecurityService() {
            return getSecurityService(node);
        }

        @Override
        public long getSecurityKey() {
            if (getMainSecurityService() == null)
                return 0;
            return getMainSecurityService().getSecurityKey();
        }

        @Override
        public void readPermissions(Map<Integer, EnumSet<SecurityPermissions>> playerPerms) {
        }

        @Override
        public boolean isSecurityEnabled() {
            if (getMainSecurityService() == null)
                return false;
            return getMainSecurityService().isAvailable();
        }

        @Override
        public int getOwner() {
            if (getMainSecurityService() == null)
                return 0;
            return getMainSecurityService().getOwner();
        }
    }
}
