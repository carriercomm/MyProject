########################################################################
#  Copyright (C) 2014 VMware, Inc.
#  All Rights Reserved
########################################################################

package VDNetLib::NSXManager::UplinkProfile;

use strict;
use warnings;
use base  qw(VDNetLib::Root::Root VDNetLib::Root::GlobalObject);

########################################################################
#
# new --
#     Constructor to create an instance of this class
#
# Input:
#     None
#
# Results:
#     bless hash reference to instance of this class
#
# Side effects:
#     None
#
########################################################################

sub new
{
   my $class      = shift;
   my %args       = @_;
   my $self = {};
   $self->{parentObj} = $args{parentObj};
   $self->{_pyIdName} = 'id_';
   $self->{_pyclass} = 'vmware.nsx.manager.' .
                       'uplink_profile.uplink_profile_facade.' .
                       'UplinkProfileFacade';
   bless $self;
   return $self;
}
1;
