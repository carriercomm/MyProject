########################################################################
# Copyright (C) 2015 VMWare, Inc.
# All Rights Reserved
########################################################################
package VDNetLib::NSXManager::MACSet;

use strict;
use warnings;
use VDNetLib::Common::GlobalConfig qw($vdLogger);
use VDNetLib::Common::VDErrorno qw(VDSetLastError VDGetLastError FAILURE SUCCESS
                                   VDCleanErrorStack);
use base 'VDNetLib::Root::Root';
use VDNetLib::InlinePython::VDNetInterface qw(CreateInlinePythonObject
                                              ConfigureLogger);

########################################################################
#
# new --
#     Contructor to create an instance of this class
#     VDNetLib::NSXManager::MACSet
#
# Input:
#     named hash parameter
#
# Results:
#     Blessed hash reference to an instance of
#     VDNetLib::NSXManager::MACSet;
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
   if (not defined $args{parentObj}) {
     $vdLogger->Error("Parent object not passed to create macset");
     VDSetLastError("ENOTDEF");
     return FAILURE;
   }

   $self->{_pyIdName} = 'id_';
   $self->{_pyclass}  = 'vmware.nsx.manager.macset.' .
                        'macset_facade.MACSetFacade';
   bless $self;
   return $self;
}
1;
