########################################################################
# Copyright (C) 2014 VMWare, Inc.
# All Rights Reserved
########################################################################
package VDNetLib::NSXManager::CryptographicAlgorithm;

use base  qw(VDNetLib::Root::Root VDNetLib::Root::GlobalObject);
use VDNetLib::InlinePython::VDNetInterface qw(CreateInlinePythonObject);
use VDNetLib::Common::GlobalConfig qw($vdLogger $sessionSTAFPort);
use VDNetLib::Common::VDErrorno qw(VDSetLastError VDGetLastError FAILURE SUCCESS
                                   SKIP VDCleanErrorStack);

use strict;
use warnings;

########################################################################
#
# new --
#     Contructor to create an instance of this class
#     VDNetLib::NSXManager::CryptographicAlgorithm
#
# Input:
#     named hash parameter
#
# Results:
#     Blessed hash reference to an instance of
#     VDNetLib::NSXManager::CryptographicAlgorithm;
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
   $self->{_pyIdName} = 'id_';
   $self->{_pyclass} = 'vmware.nsx.manager.cryptographic_algorithm.' .
   'cryptographic_algorithm_facade.CryptographicAlgorithmFacade';
   bless $self;
   return $self;
}

######################################################################
#
# GetInlinePyObject --
#     Methd to get Python equivalent object of this class
#
# Input:
#     None
#
# Results:
#     Reference to Inline Python object of this class
#
# Side effects:
#     None
#
#######################################################################

sub GetInlinePyObject
{
   my $self = shift;
   my $parentObj = shift;
   my $inlinePyObj;
   eval {
      $inlinePyObj = CreateInlinePythonObject($self->{_pyclass}, $parentObj,
                                              $self->{id});
   };
   if ($@) {
      $vdLogger->Error("Exception thrown while creating " .
                       "inline component of $self->{_pyclass}:\n". $@);
      return FAILURE;
   }
   return $inlinePyObj;
}

1;
