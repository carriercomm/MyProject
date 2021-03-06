########################################################################
# Copyright (C) 2015 VMware, Inc.
# All Rights Reserved
########################################################################

package VDNetLib::VSM::TOR::TORBinding;
#
# This package allows to perform various operations on TOR Binding
#

use base qw(VDNetLib::InlinePython::AbstractInlinePythonClass);
use strict;
use vars qw{$AUTOLOAD};
use Data::Dumper;
use VDNetLib::InlinePython::VDNetInterface qw(CreateInlinePythonObject
                                              LoadInlinePythonModule
                                              Boolean
                                              ConfigureLogger);
use VDNetLib::Common::VDErrorno qw(VDSetLastError VDGetLastError FAILURE SUCCESS
                                   VDCleanErrorStack);
use VDNetLib::Common::GlobalConfig qw($vdLogger);
use VDNetLib::Common::EsxUtils;

use constant attributemapping => {
   'tor_switch_name' => {
      'payload' => 'switchname',
      'attribute' => 'switchid'
   },
   'tor_port_name' => {
      'payload' => 'portname',
      'attribute' => 'portid'
   },
   'switch_id' => {
      'payload' => 'virtualwire',
      'attribute' => "id"
   },
   'tor_id' => {
      'payload' => 'hardwaregatewayid',
      'attribute' => "id"
   },
   'vlan' => {
      'payload' => 'vlan',
      'attribute' => undef
   },
};

########################################################################
#
# new --
#      Constructor/entry point to create an object of this package
#
# Input:
#      controllerIP : IP address of the VSM (Required)
#
# Results:
#      An object of VDNetLib::VSM::TOR
#
# Side effects:
#      None
#
########################################################################

sub new
{
   my $class = shift;
   my %args  = @_;
   my $self;
   $self->{id} = $args{id};
   $self->{vsm} = $args{vsm};
   $self->{type} = "vsm";
   bless $self, $class;
   return $self;
}


########################################################################
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
########################################################################

sub GetInlinePyObject
{
   my $self = shift;
   my $inlinePyVSMObj = $self->{vsm}->GetInlinePyObject();
   my $inlinePyObj = CreateInlinePythonObject('tor_binding.TORBinding',
                                               $inlinePyVSMObj,
                                             );
   $inlinePyObj->{id} = $self->{id};
   if (!$inlinePyObj) {
      $vdLogger->Error("Failed to create inline object");
      VDSetLastError("EINLINE");
      return FAILURE;
   }
   return $inlinePyObj;
}

1;
