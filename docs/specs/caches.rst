
.. contents:: Table of Contents
      :depth: 3

=================
Infrautils Caches
=================

https://git.opendaylight.org/gerrit/#/q/topic:bug/8300
https://www.youtube.com/watch?v=h4HOSRN2aFc

Infrautils Caches provide a Cache of keys to values.
The implementation of Infrautils Cache API is, typically, backed by established cache
frameworks,such as Ehcache, Infinispan, Guava's, Caffeine, ..., imcache, cache2k, ... etc.

Problem description
===================

Caches are not Maps!. Differences include that a Map persists all elements that are added to it until they are
explicitly removed. A Cache on the other hand is generally configured to evict entries automatically, in order to
constrain its memory footprint, based on some policy.  Another notable difference, enforced by this caching API, is
that caches should not be thought of as data structures that you put something in somewhere in your code to get it
out of somewhere else.  Instead, a Cache is "just a façade" to a CacheFunction's get.  This design enforces
proper encapsulation, and helps you not to screw up the content of your cache (like you easily can, and usually do,
when you use a Map as a cache).


Use Cases
---------
This feature will support following use cases:

* Use case 1: Enable creation of a brand new Cache, based on the passed configuration and policy.
* Use case 2: Enable creation of a brand new Cache, based on the passed configuration, with a default policy.
* Use case 3: Cache should allow to evict an entry based on the eviction policy.
* Use case 4: Cache should allow to put a new entry to the cache.
* Use case 5: InfraUtils Cache should expose APIs to retrieve the Cache Policies, Stats, and Fixed Configuration.
* Use case 6: InfraUtils Cache should expose APIs to set the Cache Policies, Stats, and Fixed Configuration.

Proposed change
===============

The proposed feature adds a new module in infrautils called "caches", which will
have the following functionalities:

* “InfraUtils Cache” has two variants - Cache and CheckedCache. Both are caches of key to values, with CheckedCache
  having support for cache function which may throw a checked exception.
* Cache can be configured to evict entries automatically, in order to constrain its memory footprint,
  based on some user configured policy.
* Implementation of the Cache APIs can be backed by established caching frameworks.
* A Sample Cache implementation using the Cache APIs will be available as part of the utility
  (under infrautils/caches/impl/caches-sample)
* Karaf CLIs will be made available for cache operations


YANG changes
------------
N/A

Workflow
--------

Define a Cache
^^^^^^^^^^^^^^
Applications can define their own Cache, with specified CacheFunction and Policies.
Caches can be configured to set the maximum entries and also to enable stats.

Define Cache Function
^^^^^^^^^^^^^^^^^^^^^
Cache is "just a façade" to a CacheFunction's get(). When the user defines a CacheFunction for his
cache, that will be executed whenever a get() is executed on the cache.

Decide Anchor
^^^^^^^^^^^^^^

Anchor refers to instance of the class "containing" this Cache. It is Used by CacheManagers to display to end-user.
It is also used under OSGi to close() the cache on bundle unload.

Decide Cache Id and Description
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Cache id is a short Id for this cache, and description will be a one line human readable description for the cache.

Cache Eviction
^^^^^^^^^^^^^^
Cache Eviction Policy is based on the number of entries the cache can hold, which will be set during the cache creation
time

Use Cache
^^^^^^^^^


Configuration impact
---------------------
N/A

Clustering considerations
-------------------------
* The current Cache Infra is node local
* Future enhancements can be made by providing clustered backend implementations eg:infinispan

Other Infra considerations
--------------------------
N.A.

Security considerations
-----------------------
N.A.

Scale and Performance Impact
----------------------------
This feature is aiming at improving the scale and performance of applications
by helping to define a CacheFunction for heavy operations.

Targeted Release(s)
-------------------
Carbon.

Known Limitations
-----------------

Cache Infra is not currently clusterwide.

Alternatives
------------
N/A

Usage
=====

Features to Install
-------------------
odl-infrautils-caches
odl-infrautils-caches-sample

REST API
--------
N/A

CLI
---
cache:clear
cache:list
cache:policy cacheID policyKey policyValue

JAVA API
--------
Caches provides the below APIs which can be used by other applications:

.. code-block:: bash

    CacheProvider APIs

    <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig, CachePolicy initialPolicy);
    <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig);
    <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(
                CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy initialPolicy);
    <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(CheckedCacheConfig<K, V, E> cacheConfig);

    CacheManager APIs

    BaseCacheConfig getConfig();
    CacheStats getStats();
    CachePolicy getPolicy();
    void setPolicy(CachePolicy newPolicy);
    void evictAll();




Implementation
==============

Assignee(s)
-----------
Primary assignee:
  <Michael Vorburger>

Work Items
----------
#. spec review.
#. caches module bring-up.
#. API definitions.
#. Cache Policy Implementation.
#. Cache and CheckedCache Implementation.
#. Backend Implementation
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

Developer Guide can capture the new set of APIs added by Caches as mentioned
in API section.

References
==========

* https://wiki.opendaylight.org/view/Infrastructure_Utilities:Carbon_Release_Plan
