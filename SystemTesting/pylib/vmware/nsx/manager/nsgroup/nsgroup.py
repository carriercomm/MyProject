import vmware.common.versions as versions
import vmware.nsx.manager.nsxbase as nsxbase
import vmware.base.group as group


class NSGroup(group.Group, nsxbase.NSXBase):

    DEFAULT_IMPLEMENTATION_VERSION = versions.NSXTransformers.AVALANCHE
