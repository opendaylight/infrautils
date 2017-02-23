
.. contents:: Table of Contents
      :depth: 3

================================
Status And Diagnostics Framework
================================

https://git.opendaylight.org/gerrit/#/q/topic:s-n-d

Status reporting is an important part of any system. This document explores and
describes various implementation options for achieving the feature.

Problem description
===================

Today ODL does not have a centralized mechanism to do status and diagnostics of
the various service modules, and have predictable system initialization. This leads
to a lot of confusions on when a particular service should start acting upon the
various incoming system events, because in many cases(like restarts) services
end up doing premature service handling.

The feature aims at developing a status and diagnostics framework for ODL, which
can :

* Orchestrate predictable system initialization, by enabling external interfaces,
  including northbound and southbound interfaces depending on a set of selected
  services declaring their availability. This in turn can prevent the system from
  processing northbound (eg: OpenStack), or southbound (eg: OVSDB or OpenFlow)
  events prematurely before all services are ready.

* Perform continuous monitoring of registered modules or internal services to
  ensure overall health of the system. This can additionally trigger alarms, or
  node reboots when individual services fail.

Use Cases
---------
This feature will support following use cases:

* Use case 1: status-and-diag module exposes a config file which user can update.
  This file will include a set of core networking services, that are necessary to
  declare the system as UP.
* Use case 2: Core services can include existing netvirt and genius services like
  ELAN, L3VPN, ITM, interface-manager, and additional services may be ACL, QoS etc
  as needed. Applications can take necessary actions based on the aggregate system status,
  for eg: OpenFlow port open, OVSDB port open, and S&D status
  update(for consumption by other NBs such as ODL Mechanism Driver)
* Use case 3: Registered Service Modules should expose their status to status-and-diag
  module which inturn will use this information to expose the service status to others.
* Use case 4: All southbound plugins should leverage the status provided by status-and-diag
  module, as well as config file settings, to block or unblock the southbound interface
* Use case 5: status-and-diag module should monitor the health of all dependant
  services on a regular basis using JMX.
* Use case 6: status-and-diag module should raise traps whenever health check on a
  module fails.
* Use case 7 : status-and-diag module should develop the capability to do a node/cluster
  reboot in future for scenarios mentioned in usecase 6.
* Use case 8 : status-and-diag module should leverage on the counters support provided
  by infrautils to expose some debug and diagnostics counters.


Proposed change
===============

The proposed feature adds a new module in infrautils called "diagstatus",
which allows CLI or alternative suitable interface to query the status of the services running
in context of the controller (interface like Openflow, OVSDB, ELAN,ITM etc.). This also allows
individual services to push status-changes to this centralized module via suitable API-based notification.
There shall be a generic set of events which application can report to the central monitoring module/service
which shall be used by the service to update the latest/current status of the services.

Service startup Requirements
----------------------------
* Since the statuses are stored local to the node and represents the states of individual
  services instances of the node, there is no data-sync-up requirements for this service
* When the service starts-up, required local map for managing service-wise status entries
  shall be initialized
* It must be ensured that the status-monitoring-service starts-up fast as service
  whenever is started/re-booted.

Service API requirements
------------------------
Status model object encapsulating the metadata of status such as :
* Node-name – may be this could be populated internally by framework if the node-name is available
  from within the framework with lesser / no external dependencies
* Module-name – populated by status-reporting module
* Service-name – populated by status-reporting module
* Service-status – populated by status-reporting module
* Current timestamp – internally populated
* Status Description – Any specific textual content which service can add to aid better troubleshooting
  of reported status


Service Internal Functionality Requirements
-------------------------------------------
* Data for current status of the changes alone must be maintained. Later we can improve it to maintain
  history of statuses for a given service
* Since the statuses of services are dynamic there is no persistence requirement to store the statuses
* Status entry of given service shall be updated based on the metadata of provided by services
* Entries for service statuses shall be created lazily - if they are not already present,
  as and when first API invocation is made by the application-module towards the status/health monitoring service
* Monitoring-Service shall internally store entries of service-statuses with URI style representation as following.
  This allows fair level of flattening of hierarchical data so that lookup for a specific key to be handled is made easier
          /<cluster-node-name>/<module-name>:<service-name>
* Read APIs of Monitoring-Service expose the service statuses on per cluster-node basis only. A separate
  module shall be developed as part of “cluster-services” user-story which can combine cross-cluster status collation
* All output of the read-APIs shall return results as Map with URI as key and current service-status
  and last-update timestamp combined as value
* In order to check the status of registered services, Status-Monitoring Service shall use standard scheduled
  timer service to invoke status-check callback on registered services
* Scheduled probe timer interval shall be configurable in config.ini. Any changes to this
  configuration shall require the system restart


Service Shutdown Requirements
-----------------------------

* Currently no specific requirements around this area as restarting or node moving to quiescent state
  results in loss of all local data

Instrumentation Requirements
----------------------------
Applications must invoke status-reporting APIs as required across the lifecycle of the services in start-up,
operational and graceful shutdown phases
In order to emulate a simpler state-machine, we can have services report following statuses
* STARTING – at the start of onSessionInitiated() on instrumented service
* OPERATIONAL – at the end of onSessionInitiated() on instrumented service
* ERROR – during onSessionInitiated() of service if any exceptions are caught, then ERROR status is reported

YANG changes
------------
N/A

Workflow
--------

Define Configuration file
^^^^^^^^^^^^^^^^^^^^^^^^^
diagstatusservice.properties file will be added which will list down all the
mbean names which services are exposing. Sample format based on the mbeans to be
exposed by Genius - ITM and interfacemanager modules can be as below:

ITM=org.opendaylight.genius.itm.status:type=SvcItmService
INTERFACE_SERVICE=org.opendaylight.genius.interfacemanager.status:type=SvcInterfaceService

There is an implicit assumption that the content of the file is correct, if at all
is not correct, the corresponding service will be shown in ERROR state.

Load Configuration file on startup
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Whenever the diagstatus bundle comes up, diagstatus.properties configuration file
will be loaded and the properties will be maintained in an internal data structure.
All the Mbeans read will be registered one by one.

Read Service Status
^^^^^^^^^^^^^^^^^^^

Whenever applications/CLI try to fetch the service status, diagstatus module will query the
status through the respective mbeans(both local and remote),and an aggregated result is provided
as response.

Configuration impact
---------------------
The configuration file provided by diagstatus needs to be updated by user, so that
their service will be tracked for status.

Clustering considerations
-------------------------
* The CLIs/APIs provided by diagstatus module will be cluster wide.
* Every node shall expose a Status Check MBean for querying the current status which is local to
  the node being queried.
* Every node shall also expose a Clusterwide Status Check MBean for querying the clusterwide
  Status of services.
* For local status CLI shall query local MBean.
* For clusterwide status CLI shall query local MBean AS WELL AS and remote MBean instances across
  all current members of the cluster by accessing respective PlatformMBeanServer locally and remotely.
* It is assumed that IP Addresses of the current nodes of cluster and standard JMX Port details are available for clusterwide MBeans
* CLI local to any of the cluster members shall invoke clusterwide MBean on ANY ONE of current set of cluster nodes
* Every node of cluster shall query all peer nodes using the JMX interface and consolidate the
  statuses reported by each node of cluster and return combined node-wise statuses across the cluster


Other Infra considerations
--------------------------
N.A.

Security considerations
-----------------------
N.A.

Scale and Performance Impact
----------------------------
N/A as it is a new feature which does not impact any current functionality.

Targeted Release(s)
-------------------
Carbon.

Known Limitations
-----------------
The initial feature will not have the health check functionality.
The initial feature will not have integration to infrautils counter framework
for dispalying diag-counters.

Alternatives
------------
N/A

Usage
=====

Features to Install
-------------------
This feature doesn't add any new karaf feature.

REST API
--------
Following are the service APIs which must be supported by the Framework :
* Accept Service-status from services which invoke the framework
* Get the current statuses of all services of a given cluster-node
* A registration API to allow monitored service to register the callback
* An interface which is to be implemented by monitored module which could be periodically
  invoked by Status-Monitoring framework on each target module to check status
* Each service implements their own logic to check the local-health status using the
  interface and report the status


CLI
---
Following CLIs will be supported as part of this feature:

* showstatus - get all service status
* showSvcStatus - get remote service status

Implementation
==============

Assignee(s)
-----------
Primary assignee:
  <Faseela K>

Other contributors:
  <Vacancies available>


Work Items
----------
#. spec review
#. diagstatus module bring-up
#. API definitions
#. Addition of Configuration file
#. initialize status monitoring service by loading the config file
#. initialize services by registering mbeans
#. Reading the status of Mbeans specified in config file
#. Aggregate the status of services from each node
#. Add CLI.
#. Add UTs.
#. Add Documentation

Dependencies
============
This is a new module and requires the below libraries:

* org.apache.httpcomponents
* com.google.code.gson
* com.google.guava

This change is backwards compatible, so no impact on dependent projects.
Projects can choose to start using this when they want.

Following projects currently depend on InfraUtils:

* Netvirt
* Genius

Testing
=======

Unit Tests
----------
Appropriate UTs will be added for the new code coming in once framework is in place.

Integration Tests
-----------------
Since Component Style unit tests will be added for the feature, no need for ITs

CSIT
----
N/A

Documentation Impact
====================
This will require changes to User Guide and Developer Guide.

User Guide will need to add information on how to use status-and-diag APIs
and CLIs

Developer Guide will need to capture how to use the APIs of status-and-diag
module to derive service specific actions. Also, the documentation needs to
capture how services can expose their status via Mbean and integrate the same
to status-and-diag module

References
==========

* https://wiki.opendaylight.org/view/Infrastructure_Utilities:Carbon_Release_Plan
