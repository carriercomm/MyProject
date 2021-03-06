import vmware.base.node as node
import vmware.nsx.manager.manager_client as manager_client


class TransportZoneAPIClient(node.Node, manager_client.NSXManagerAPIClient):
    def __init__(self, parent=None, id_=None):
        super(TransportZoneAPIClient, self).__init__(parent=parent)
        self.id_ = id_
