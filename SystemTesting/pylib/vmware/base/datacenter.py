import vmware.base.base as base
import vmware.common.base_facade as base_facade
import vmware.interfaces.labels as labels

auto_resolve = base_facade.auto_resolve


class Datacenter(base.Base):

    def __init__(self, name=None, parent=None):
        super(Datacenter, self).__init__()
        self.name = name
        self.parent = parent

    @auto_resolve(labels.CLUSTER)
    def add_host(self, execution_type=None, **kwargs):
        pass

    @auto_resolve(labels.CRUD)
    def create(self, execution_type=None, **kwargs):
        pass

    @auto_resolve(labels.CRUD)
    def delete(self, execution_type=None, **kwargs):
        pass

    @auto_resolve(labels.DATACENTER)
    def query_dc_exists(self, execution_type=None, **kwargs):
        pass
