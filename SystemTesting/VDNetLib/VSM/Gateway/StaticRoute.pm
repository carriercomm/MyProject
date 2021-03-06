########################################################################
# Copyright (C) 2014 VMWare, Inc.
# All Rights Reserved
########################################################################
package VDNetLib::VSM::Gateway::StaticRoute;
use base 'VDNetLib::InlinePython::AbstractInlinePythonClass';
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
use Inline::Python qw(eval_python
                     py_bind_class
                     py_eval
                     py_study_package
                     py_call_function
                     py_call_method
                     py_is_tuple);
use constant attributemapping => {
   'staticroutes' => {
       'payload' => 'staticroutes',
       'attribute' => undef,
                     },
   'network' => {
       'payload' =>  'network',
       'attribute' => undef,
                },
   'nexthop' => {
       'payload' => 'nexthop',
       'attribute' => undef,
                 },
};
########################################################################
#
# new --
#     Contructor to create an instance of this class
#     VDNetLib::VSM::Gateway::StaticRoute
#
# Input:
#        None
# Results:
#     Blessed hash reference to an instance of
#     VDNetLib::VSM::Gateway::StaticRoute
#
# Side effects:
#     None
#
########################################################################
sub new
{
   my $class = shift;
   my %args  = @_;
   my $self;
   $self->{id} = $args{id};
   $self->{gateway} = $args{gateway};
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
   my $inlinePyEdgeObj = $self->{gateway}->GetInlinePyObject();
   my $inlinePyObj = CreateInlinePythonObject('static_routing.StaticRouting',
                                               $inlinePyEdgeObj,
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


