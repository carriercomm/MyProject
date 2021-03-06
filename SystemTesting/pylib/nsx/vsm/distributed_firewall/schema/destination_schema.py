import base_schema

class DestinationSchema(base_schema.BaseSchema):
    _schema_name = "destination"

    def __init__(self, py_dict=None):
        """ Constructor to create DestinationSchema object
        @param py_dict : python dictionary to construct this object
        """
        super(DestinationSchema, self).__init__()
        self.set_data_type("xml")
        self.isValid = None
        self.type = None
        self.name = None
        self.value = None


        if py_dict is not None:
            self.get_object_from_py_dict(py_dict)
