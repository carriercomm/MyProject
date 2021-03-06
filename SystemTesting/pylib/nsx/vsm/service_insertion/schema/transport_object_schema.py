import base_schema
from service_attribute_schema import ServiceAttributeSchema

class TransportObjectSchema(base_schema.BaseSchema):
    _schema_name = "transport"

    def __init__(self, py_dict=None):
        """ Constructor to create TransportObjectSchema object
        @param py_dict : python dictionary to construct this object
        """
        super(TransportObjectSchema, self).__init__()
        self.set_data_type('xml')
        self.type = None
        self.revision = None
        self.transportAttributes = [ServiceAttributeSchema()]

        if py_dict is not None:
            self.get_object_from_py_dict(py_dict)