########################################################################
# Copyright (C) 2013 VMWare, Inc.
# # All Rights Reserved
########################################################################

###############################################################################
#
# package VDNetLib::Workloads::GroupingObjectWorkload;
# This package is used to run Grouping Objects workload that involves
#  -- Creating and deleting IPSet, Service, ServiceGroup and IPPool
#         grouping objects
#
# The grouping objects new() is implemented
#
# This package takes vdNet's testbed hash and workload hash.
# The VDNetLib::Neutron::IPSet, VDNetLib::Neutron::Service
#     VDNetLib::Neutron::ServiceGroup and VDNetLib::Neutron::IPPool
#     objects will be created in new function
#
# In this way, all the Grouping Objects workloads can be run parallelly with no
# re-entrant issue.
#
###############################################################################

package VDNetLib::Workloads::GroupingObjectWorkload;

use strict;
use warnings;
use Data::Dumper;

# Inherit the parent class.
use base qw(VDNetLib::Workloads::ParentWorkload);

use VDNetLib::Common::GlobalConfig qw($vdLogger);
use VDNetLib::Common::VDErrorno qw(SUCCESS FAILURE VDSetLastError VDGetLastError
                                   VDCleanErrorStack);
use VDNetLib::Common::Utilities;




########################################################################
#
# new --
#      Method which returns an object of
#      VDNetLib::Workloads::GroupingObjectWorkload
#      class.
#
# Input:
#      A named parameter hash with the following keys:
#      testbed  - reference to testbed object
#      workload - reference to workload hash (supported key/values
#                 mentioned in the package description)
#
# Results:
#      Returns a VDNetLib::Workloads::GroupingObjectWorkload object, if successful;
#      "FAILURE", in case of error
#
# Side effects:
#      None
#
########################################################################

sub new
{
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
      'targetkey'    => "testgroupingobject",
      'managementkeys' => ['type', 'iterations','testgroupingobject','expectedresult','sleepbetweencombos'],
      'componentIndex' => undef
      };

    bless ($self, $class);

   # Adding KEYSDATABASE
   $self->{keysdatabase} = $self->GetKeysTable();

   return $self;
}


########################################################################
#
#  PickRandomNumberFromARange
#       This method does what the method name says if user gives a range
#       Else just returns the value as it is.
#
# Input:
#       value can be number or range
#       E.g. 5 will return 5
#       5-10 will return any random number between this range
#
# Results:
#      Returns the same value or a random element from the range
#
# Side effetcs:
#       None
#
########################################################################

sub PickRandomNumberFromARange
{
   my $self             = shift;
   my $value            = shift;
   return VDNetLib::Common::Utilities::PickRandomNumberFromARange($value);
}
1;
