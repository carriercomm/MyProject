import vmware.nsx.manager.bridge_cluster.bridge_cluster as bridge_cluster
import vmware.nsx.manager.manager_client as manager_client


class BridgeClusterCLIClient(bridge_cluster.BridgeCluster,
                             manager_client.NSXManagerCLIClient):
    pass
