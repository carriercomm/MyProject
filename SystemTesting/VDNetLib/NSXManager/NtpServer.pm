########################################################################
# Copyright (C) 2014 VMWare, Inc.
# All Rights Reserved
########################################################################
package VDNetLib::NSXManager::NtpServer;

use strict;
use warnings;

use base  qw(VDNetLib::Root::Root VDNetLib::Root::GlobalObject);

########################################################################
#
# new --
#     Contructor to create an instance of this class
#     VDNetLib::NSXManager::NtpServer
#
# Input:
#     named hash parameter
#
# Results:
#     Blessed hash reference to an instance of
#     VDNetLib::NSXManager::NtpServer;
#
# Side effects:
#     None
#
########################################################################

sub new
{
   my $class = shift;
   my %args = @_;
   my $self = {};
   $self->{parentObj} = $args{parentObj};
   $self->{_pyclass} = 'vmware.nsx.manager.appliancemanagement.ntp.'.
   'ntp_facade.NtpFacade';
   $self->{_pyIdName} = 'id_';
   bless $self;
   return $self;
}

1;
