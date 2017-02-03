
.. contents:: Table of Contents
      :depth: 3

=========================
Datastore Job Coordinator
=========================

https://git.opendaylight.org/gerrit/#/q/topic:dsjc

Datastore Job Coordinator is a framework for managing set of updates to 
MD-SAL config/operational datastores in an efficient manner.

Problem description
===================

The concept of datastore jobcordinator was derived from the following pattern seen
in many ODL project implementations :

* The Business Logic for the configuration/state handling is performed in the Actor Thread itself.
  This will cause the Actor’s mailbox to get filled up and may start causing unnecessary back-pressure.
* Actions that can be executed independently will get unnecessarily serialized.
  Can cause other unrelated applications starve for chance to execute.
* Available CPU power may not be utilized fully. (for instance, if 1000 interfaces
  are created on different ports, all 1000 interfaces creation will happen one after the other.)
* May depend on external applications to distribute the load across the actors.


Use Cases
---------
This feature will support following use cases:

* Use case 1: DSJC framework should enable applications to enqueue their jobs based on a job key.
* Use case 2: DSJC framework should run jobs queued on same key sequentially, and different keys
  parallelly.
* Use case 3: DSJC framework should provide a framework for retry mechanism in case the jobs fail.
* Use case 4: DSJC framework should provide a framework for rollback in case the jobs fail permanently.
* Use case 3: DSJC should provide applications the flexibility to input the number of retries
  on a need basis.

Proposed change
===============

The proposed feature adds a new module in infrautils called "datastorejobcoordinator", which will
have the following functionalities:

* “Datastore Job” is a set of updates to the Config/Operational Datastore.
* Dependent Jobs [eg. Operations on interfaces on same port] that need to be run
  one after the other will continue to be run in sequence.
* Independent Jobs [eg. Operations on interfaces across different Ports] will be allowed to run parallel.
* Makes use of ForkJoin Pools that allows for work-stealing across threads. ThreadPool executor
  flavor is also available… But would be deprecating that soon.
* Jobs are enqueued and dequeued to/from a two-level Hash structure that ensures point 1 & 2 above are
  satisfied and are executed using the ForkJoinPool mentioned in point 3.
* The jobs are enqueued by the application along with an application job-key (type: string). The Coordinator
  dequeues and schedules the job for execution as appropriate. All jobs enqueued with the same job-key will
  be executed sequentially.
* Datastore Job Coordination function gets the list of listenable futures returned from each job.
* The Job is deemed complete only when the onSuccess callback is invoked and the next enqueued job for that
  job-key will be dequeued and executed.
* On Failure, based on application input, retries and/or rollback will be performed. Rollback failures are
  considered as double-fault and system bails out with error message and moves on to the next job with that Job-Key.


YANG changes
------------
N/A

Workflow
--------

Define Job Workers
^^^^^^^^^^^^^^^^^^
Applications can define their own worker threads for their job.
A job is defined as a piece of code that can be independently executed.

Define Rollback Workers
^^^^^^^^^^^^^^^^^^^^^^^
Applications should define a rollback worker, which will have the code to be executed
in case the main job fails permanently. In usual scenarios, this will be the code to clean up
all partially completed transactions by the main worker.

Decide Job Key
^^^^^^^^^^^^^^

Applications should carefully choose the job-key for their job worker. All jobs based on the
same job-key will be executed sequentially, and all jobs on different keys will be executed parallelly 
depending on the available threadpool size.

Enqueue Job
^^^^^^^^^^^
Applications can enqueue their job worker to DSJC framework for execution.DSJC has a two level hash structure
to handle the execution of the tasks sequentially/parallelly. Whenever a job is enqueued, DSJC creates
a Job Entry for the particular job. A Job Entry is characterized by - job-key, the main worker, the rollback
worker and the number of retries. This JobEntry will be added to the corresponding JobQueue in a JobEntryMap.
There will be on JobQueue per Job-key in the JobEntryMap structure.

Job Queue Handling
^^^^^^^^^^^^^^^^^^
There is a JobQueueHandler task which runs periodically, which will poll each of the JobQueues
to execute the main task of the corresponding JobEntry. Within a JobQueue, execution will be synchronized.

Retries in case of failure
^^^^^^^^^^^^^^^^^^^^^^^^^^
The list of listenable futures for the transactions from the application main worker will be available to DSJC,
and if at all the transaction fails, the main worker will be retried the 'max-retries' number of times which is
application specified. If all the retries fail, dsjc will bail out and the rollback worker will be executed.

Configuration impact
---------------------
N/A

Clustering considerations
-------------------------
* Datastore Job Coordinator synchronization is not cluster-wide
* This will still work in a clustered mode by handling optimistic lock exceptions and retrying of the job.
* Future scope can be : Cluster-Wide Datastore & Switch Job Coordination in:
* Fully replicated Followers also listening Mode.
* Distributed system where no. of replicas is less than the no. of nodes in the cluster.

Other Infra considerations
--------------------------
N.A.

Security considerations
-----------------------
N.A.

Scale and Performance Impact
----------------------------
This feature is aiming at improving the scale and performance of applications
by providing the cabability to execute their functions parallelly wherever it can be done.

Targeted Release(s)
-------------------
Carbon.

Known Limitations
-----------------

DSJC synchronization is not currently clusterwide.

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
N/A

CLI
---
N/A

Implementation
==============

Assignee(s)
-----------
Primary assignee:
  <Periyasamy Palanisamy>

Other contributors:
  <Yakir Dorani>
  <Faseela K>

Work Items
----------
#. spec review.
#. datastorejobccordinator module bring-up.
#. API definitions.
#. Enqueue Job Implementation.
#. Job Queue Handler Implementation.
#. Job Callback Implementation including retry and rollback
#. Add CLI.
#. Add UTs.
#. Add Documentation.

Dependencies
============

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
N/A

CSIT
----
N/A

Documentation Impact
====================
This will require changes to Developer Guide.

Developer Guide will need to capture how to use the APIs of datastorejobccordinator
module to achieve parallelism of jobs, and how to do reties and rollbacks.

References
==========

* https://wiki.opendaylight.org/view/Infrastructure_Utilities:Carbon_Release_Plan
