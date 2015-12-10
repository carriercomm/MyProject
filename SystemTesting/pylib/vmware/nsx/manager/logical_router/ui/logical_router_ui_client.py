import vmware.nsx.manager.logical_router.logical_router as logical_router
import vmware.nsx.manager.manager_client as manager_client


class LogicalRouterUIClient(logical_router.LogicalRouter,
                            manager_client.NSXManagerUIClient):
    pass
