from vsm_address_group_schema import AddressGroupSchema
import base_schema


class InterfaceSchema(base_schema.BaseSchema):
    """"""
    _schema_name = "interface"
    def __init__(self, py_dict=None):
        """ Constructor to create InterfaceSchema object

        @param py_dict : python dictionary to construct this object
        """
        super(InterfaceSchema, self).__init__()
        self.type = None
        self.index = None
        self.label = None
        self.connectedToName = None
        self.name = None
        self.mtu = None
        self.isConnected = None
        self.connectedToId = None
        self.addressGroups = [AddressGroupSchema()]
        self.set_data_type("xml")

        if py_dict is not None:
            self.get_object_from_py_dict(py_dict)
