########################################################################
# Copyright (C) 2014 VMWare, Inc.
# # All Rights Reserved
########################################################################

package VDNetLib::Workloads::DFWSectionWorkload;

use strict;
use warnings;
use Data::Dumper;

# Inherit the parent class.
use base qw(VDNetLib::Workloads::ParentWorkload);
use VDNetLib::Common::GlobalConfig qw($vdLogger);
use VDNetLib::Common::VDErrorno qw(SUCCESS FAILURE VDSetLastError VDGetLastError
                                   VDCleanErrorStack);
use VDNetLib::Common::Iterator;
use VDNetLib::Workloads::ServiceWorkload;
use VDNetLib::Workloads::NSXWorkload;
use VDNetLib::Workloads::Utils;



########################################################################
#
# new --
#      Method which returns an object of
#      VDNetLib::Workloads:: DFWSectionWorkload
#      class.
#
# Input:
#      A named parameter hash with the following keys:
#      testbed  - reference to testbed object
#      workload - reference to workload hash (supported key/values
#                 mentioned in the package description)
#
# Results:
#      Returns a VDNetLib::Workloads:: DFWSectionWorkload object, if successful;
#      "FAILURE", in case of error
#
# Side effects:
#      None
#
########################################################################

sub new {
   my $class = shift;
   my %options = @_;
   my $self;

   if (not defined $options{testbed} || not defined $options{workload}) {
      $vdLogger->Error("Testbed and/or workload not provided");
      VDSetLastError("EINVALID");
      return "FAILURE";
   }

   $self = {
      'testbed'      => $options{testbed},
      'workload'     => $options{workload},
      'targetkey'    => "testdfwsection",
      'managementkeys' => ['type', 'iterations', 'testdfwsection',
                           'expectedresult', 'sleepbetweenworkloads'],
      'componentIndex' => undef
      };

    bless ($self, $class);

   # Adding KEYSDATABASE
   $self->{keysdatabase} = $self->GetKeysTable();
    return $self;
}
1;
