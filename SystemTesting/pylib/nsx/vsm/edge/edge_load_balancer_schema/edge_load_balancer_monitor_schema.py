import base_schema


class LoadBalancerMonitorSchema(base_schema.BaseSchema):
    _schema_name = "monitor"

    def __init__(self, py_dict=None):
        """ Constructor to create LoadBalancerMonitorSchema object

        @param py_dict : python dictionary to construct this object
        """
        super(LoadBalancerMonitorSchema, self).__init__()
        self.set_data_type('xml')
        self.monitorId = None
        self.type = None
        self.interval = None
        self.timeout = None
        self.maxRetries = None
        self.method = None
        self.url = None
        self.name = None

        if py_dict is not None:
            self.get_object_from_py_dict(py_dict)