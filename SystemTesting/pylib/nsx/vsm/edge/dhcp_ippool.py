import vmware.common.logger as logger
import vsm_client
from edge import Edge
from vsm import VSM
from dhcp import DHCP


class DHCPIPPool(vsm_client.VSMClient):
    def __init__(self, edge=None):
        """ Constructor to create DHCP object

        @param edge object on which DHCP has to be configured
        """
        super(DHCPIPPool, self).__init__()
        self.log = logger.setup_logging(self.__class__.__name__)
        self.schema_class = 'vsm_ip_pool_schema.IPPoolSchema'
        self.set_content_type('application/xml')
        self.set_accept_type('application/xml')
        self.auth_type = "vsm"
        if edge is not None:
            self.set_connection(edge.get_connection())
        self.connection.api_header = '/api/4.0'
        self.set_create_endpoint("/edges/" + edge.id + "/dhcp/config/ippools")
        self.set_read_endpoint("/edges/" + edge.id + "/dhcp/config")
        self.id = None

if __name__ == '__main__':
    var = '''
        <ipPool>
            <ipRange>192.168.4.192-192.168.4.220</ipRange>
            <defaultGateway>192.168.4.1</defaultGateway>
            <domainName>eng.vmware.com</domainName>
            <primaryNameServer>192.168.4.1</primaryNameServer>
            <secondaryNameServer>4.2.2.4</secondaryNameServer>
            <leaseTime>3600</leaseTime>
            <autoConfigureDNS>true</autoConfigureDNS>
        </ipPool>
    '''

    log = logger.setup_logging('Gateway Services Edge DHCP - Test')
    vsm_obj = VSM("10.110.26.210", "admin", "default", "")

    edge_client = Edge(vsm_obj)

    #Create Gateway Services Edge
    py_dict = {'datacentermoid': 'datacenter-136',
               'tenant': 'default',
               'name': 'edge-1001',
               'enableaesni': True,
               'enablefips': False,
               'vseloglevel': 'info',
               'type': 'gatewayServices',
               'appliances': {'appliancesize': 'compact', 'deployappliances': False},
               'clisettings': {'remoteaccess': False, 'username': 'admin', 'password': 'default'},
               'autoconfiguration': {'enabled': True, 'rulepriority': 'high'},
               'querydaemon': {'enabled': True, 'port': 5666},
               'vnics': [{'label': 'vNic_0', 'name': 'NIC-1',
                                   'addressgroups': [{
                                                         'primaryaddress': '192.168.0.1',
                                                          'subnetmask': '255.255.255.224',
                                                          'subnetprefixlength': 27
                                                     }],
                                   'mtu': 1500, 'type': 'internal', 'isconnected': True, 'index': 0,
                                   'portgroupid': 'dvportgroup-164',
                                   'enableproxyarp': False, 'enablesendredirects': False
                                  }],
               }

    edge_schema_object = edge_client.get_schema_object(py_dict)
    result_obj_1 = edge_client.create(edge_schema_object)
    print result_obj_1.status_code

    edge_schema = edge_client.read()
    edge_schema.print_object()

    edge_client.get_ip()

    #Configure DHCP
    dhcp_py_dict = {
        'enabled': True,
        'logging': {'loglevel': 'info', 'enable': False},
        'ippools': [
                   {
                       'autoconfiguredns': True,
                       'defaultGateway': '192.168.0.3',
                       'domainname': 'eng.vmware.com',
                       'primarynameserver': '192.168.0.4',
                       'secondarynameserver': '4.2.2.4',
                       'leasetime': 360000,
                       'iprange': '192.168.0.10-192.168.0.20',
                   }
        ],
    }
    dhcp_client = DHCP(edge_client)
    dhcp_schema_object = dhcp_client.get_schema_object(dhcp_py_dict)
    result_obj_1 = dhcp_client.create(dhcp_schema_object)
    print result_obj_1.status_code

    dhcp_schema = dhcp_client.read()
    dhcp_schema.print_object()

    #Append IPPool to DHCP
    ippool_py_dict = {
                       'autoconfiguredns': True,
                       'defaultGateway': '192.168.0.3',
                       'domainname': 'eng.vmware.com',
                       'primarynameserver': '192.168.0.4',
                       'secondarynameserver': '4.2.2.4',
                       'leasetime': 360000,
                       'iprange': '192.168.0.21-192.168.0.25',
    }
    ippool_client = DHCPIPPool(edge_client)
    ippool_schema_object = ippool_client.get_schema_object(ippool_py_dict)
    result_obj_1 = ippool_client.create(ippool_schema_object)
    print result_obj_1.status_code

    dhcp_schema = dhcp_client.read()
    dhcp_schema.print_object()

    #Delete Gateway Services Edge
    response_status = edge_client.delete()
    print response_status.status_code