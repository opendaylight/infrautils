
.. contents:: Table of Contents
   :depth: 3

=========================
Clustered Metrics Service
=========================

https://git.opendaylight.org/gerrit/#/q/topic:clustered-metrics-service

Clustered Metrics Service is a feature which

* Enables applications to use codahale metrics framework.
* It allows aggregation of metrics such as counters as used by various
  applications in a centralized module.
* The reporting of metrics via JMX which can be retrieved from across clustered
  nodes using standard protocols such as RMI or JMX over REST.
* The framework also enables exposure of metrics aggregated from clustered nodes
  via unified REST interface for consumption by external metering components like
  Ceilometer in the standard OpenStack environment.


Problem description
===================

For various requirements such as troubleshooting and diagnostics, counters, gauges
and meters would be of good value in ODL context. Currently, in ODL, a unified means
of enabling these runtime metrics are not available.

Key requirements for such a component are

* Allow applications to flexibly incorporate metric artifacts such as counters,
  gauges, meters.
* Provide a means for aggregating application-reported metrics from across the
  cluster nodes.
* Enable standard end-points (eg. REST API) for external applications/scripts/tools
  to retrieve the aggregated metrics for feeding to higher level tools.

Scope of the proposed feature
=============================

This feature attempts to meet above requirements by

* Leveraging a matured framework like Codahale metrics as a core component.
* Using JMX remoting mechanism to aggregate metrics from across the clustered nodes
* Implementing a reference REST end-point consumable by Ceilometer as demonstration
  of how external integration can be made feasible.

From instrumentation perspective, the feature shall initially demonstrate the usage
of gauges for OpenFlow metrics and implement end-to-end flow from instrumentation
till providing a consumable REST endpoint for the aggregated metrics.


Use Cases
---------

This feature will support following use cases:

* Use case 1: Applications request the framework component to create a dedicated
  metrics registry based on unique application identifier.
* Use case 2: Each application updates the metrics in the registry choosing the
  metric type it wants.
* Use case 3: Framework maintains all the metrics registry and its rendering
  information to decode each application metric.
* Use case 4: Reporting of statistics via JMX. Also, in codahale provides different
  reporting mechanism, depending on requirement it could be configured.
* Use case 5: Framework does cluster aggregation of the statistics across all
  controller nodes for each application and gets the aggregated results.
* Use case 6: The aggregated statistics are then rendered and exposed via REST API.

Following use cases will not be supported:

Applications should take care of deleting the metrics. Example, when a port get
deleted from a switch, the application should take care of deleting its port
statistics.

Proposed change
===============

Clustered Metrics Service framework composed of two parts and introduces two modules.

**MetricRegistryProvider**

* This module creates metrics registry for the application by exposing the following API.
   a) **public MetricRegistry createRegistry(String applicationRegistryNameId);**
   b) **public MetricRegistry deleteRegistry(String applicationRegistryNameId);**
* It maintains the list if all application registry and report the metrics via JMX.
* Each application should create its own metric registry by calling the API with
  unique application identifier. In case of single registry for all application,
  statistics aggregator have to scan the whole registry for any requirement (say,
  group counters for a vpn, group counters for a switch). This approach makes the
  statistics aggregator lessen the read cost. This could also make an application
  to create multiple registries, which may lead to performance impact. To avoid
  this, the framework will have an upper bound restriction for registry creation and
  to throw an exception upon failure to the application.

**MetricAggregator**

* This module aggregates all the statistics exposed via JMX
  across the cluster nodes using standard protocols such as RMI or JMX over REST.
* Statistics rendering for each metric(for ex. Switch
  port statistics - switch id, port id, port uuid, tenant id are to be rendered).
  The rendered information is then exposed via rest API(JAX-RS jersey).
* The statistics aggregation shall be done in a regular polling interval.

The counters are ephemeral and are not persisted at nodal or cluster level.

Pipeline changes
----------------
N.A

Yang changes
------------
N.A

Configuration impact
--------------------
This change doesn't add or modify any configuration parameters.

Clustering considerations
-------------------------
Metric Aggregator takes care in aggregating the statistics across cluster.

Other Infra considerations
--------------------------
N.A.

Security considerations
-----------------------
N.A.

Scale and Performance Impact
----------------------------
N/A as it is a new feature which does not impact any current functionality.

Targeted Release
----------------
Nitrogen.

Alternatives
------------
Alternatives considered and why they were not selected.

Usage
=====
Counter Applications are residing in individual feature bundles. To get all the
statistics, it has to be ensured those features are installed.

Features to Install
-------------------
This shall be part of infrautils feature.

Following counter application features are to be installed to get

* BGP statistics - "odl-netvirt-openstack".
* Switch and port statistics - "odl-genius".
* Controller-switch mapping counters - "odl-genius-fcaps-application".

REST API(Exposed by MetricsAggregator)
--------------------------------------

flow-capable-switches statistics
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

GET : **URL:**  controller/statistics/flow-capable-switches

**Sample JSON data**

.. code-block:: json
   :emphasize-lines: 43

    {
       "flow_capable_switches" : [ {
          "admin_tenant_id" : "7rt3rf3aj-0610-7a3z-cz21-ae87876hun9f",
          "packet_in_messages_received" : 300,
          "packet_out_messages_sent" : 0,
          "ports" : 0,
          "flow_datapath_id" : 2
       }, {
          "admin_tenant_id" : "5ffe6trf-23-21e5-c191-eeff819dcd9f",
          "packet_in_messages_received" : 501,
          "packet_out_messages_sent" : 300,
          "ports" : 3,
          "flow_datapath_id" : 1,
          "switch_port_counters" : [{
             "bytes_received" : 9800,
             "bytes_sent" : 6540,
             "duration" : 0,
             "tenant_id": "9u33df3a-7376-21e5-c191-eeff819dcd9f",
             "packets_received_on_tunnel" : 0,
             "packets_sent_on_tunnel" : 7650,
             "packets_received" : 0,
             "packets_received_drop" : 0,
             "packets_received_error" : 0,
             "packets_sent" : 0,
             "port_id" : 2,
             "port_uuid" : "87fwdf3a-7621-8ut5-u781-ddii900dcd8g"
          }, {
             "bytes_received" : 9800,
             "bytes_sent" : 840,
             "duration" : 7800,
             "tenant_id": "6c53df3a-3456-11e5-a151-feff819cdc9f",
             "packets_internal_received" : 984,
             "packets_internal_sent" : 7950,
             "packets_received" : 9900,
             "packets_received_drop" : 1500,
             "packets_received_error" : 1000,
             "packets_sent" : 7890,
             "port_id" : 1,
             "port_uuid" : "6ef7gh3b-8909-3ec6-j4j3-efgf765dbe8g"
          } ],
          "table_counters" : [ {
             "flow_count" : 90,
             "table_id" : 96
          }, {
             "flow_count" : 80,
             "table_id" : 44
          } ]
       } ]
    }

BGP statistics
^^^^^^^^^^^^^^

GET : **URL:** controller/statistics/bgp

**Sample JSON data**

.. code-block:: json
   :emphasize-lines: 23

   {
       "bgp" : {
          "bgp_neighbor_counters" : [ {
             "autonomous_system_number" : 100,
             "neighbor_ip" : "1.1.1.1",
             "packets_received" : 5654,
             "packets_sent" : 987
          }, {
             "autonomous_system_number" : 200,
             "neighbor_ip" : "2.2.2.2",
             "packets_received" : 765,
             "packets_sent" : 678
          } ],
          "bgp_route_counters" : [ {
             "route_distinguisher" : 123,
             "routes" : 98
          }, {
             "route_distinguisher" : 333,
             "routes" : 100
          } ],
          "total_routes" : 198
       }
    }

Controller-switch-mappings statistics
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

GET : **URL:** controller/statistics/controller-switch-mappings

**Sample JSON data**

.. code-block:: json
   :emphasize-lines: 9

   {
       "controller_switch_mappings" : [ {
          "connected_flow_capable_switches" : 2,
          "controller_host_name" : "host-3"
       }, {
          "connected_flow_capable_switches" : 1,
          "controller_host_name" : "host-4"
       } ]
    }

CLI
---
N.A.


Implementation
==============

Assignee(s)
-----------
Primary assignee:
  <Viji J>

Other contributors:
  <Vacancies available>


Work Items
----------
#. Spec definition
#. Blueprint Module creation
#. API definitions
#. Metric Registry Provider implementation
#. Metric Aggregation implementation
#. Rendering aggregated statistics
#. Rest api implementation
#. Add UTs.
#. Add Documentation

Dependencies
============
No dependencies.

Testing
=======
Capture details of testing that will need to be added.

Unit Tests
----------
Appropriate UTs will be added once counter framework module is in place.

Integration Tests
-----------------
Integration tests will be added will be added once counter framework module is in place.

CSIT
----
TestCases:

* Verification of flow-capable switch, bgp and controller-switch mappings
  statistics in rest API.
* Verification of counters upon switch reboot.
* Verification of counters upon controller reboot.

Documentation Impact
====================
This will require changes to User Guide and Developer Guide.

User Guide will need to add information on how OpenDaylight can
be used to retrieve aggregated statistics.

Developer Guide will capture the implementation sketch of how
aggregated statistics is retrieved.

References
==========

* `OpenDaylight Documentation Guide <http://docs.opendaylight.org/en/latest/documentation.html>`