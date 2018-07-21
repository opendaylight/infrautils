
.. contents:: Table of Contents
      :depth: 3

===================
Infrautils Features
===================

Welcome to the Infrastructure Utilities project page!
This project offers technical utilities and infrastructures for other projects to use.

We currently consider infrautils a low-level offset 0 project.
As is now, it only depends on odlparent, and does not have dependencies to e.g. controller & mdsal or yangtools.
This is intentional, as this would allow such projects to use utilities from infrautils
(without causing circular dependencies among offset 0 projects).


Should there be a need to build utilities requiring such dependencies in the future (for example Cache layer for mdsal, YANG RPC for ready, etc.),
we should discuss whether to create a new infrautils2 (?) project with dependencies to infrautils and other offset 0
projects, or put such utilities into existing projects such as e.g. genius.

Features
========

The presentations given at the references section gives goodoverview of the project.

@Inject DI
----------
See https://wiki.opendaylight.org/view/BestPractices/DI_Guidelines

Utils incl. org.opendaylight.infrautils.utils.concurrent
--------------------------------------------------------

Bunch of small (non test related) low level general utility classes à la Apache (Lang) Commons or Guava and similar incl. utils.concurrent:

* ListenableFutures toCompletionStage & addErrorLogging
* CompletableFutures completedExceptionally
* CompletionStages completedExceptionally
* LoggingRejectedExecutionHandler, LoggingThreadUncaughtExceptionHandler, ThreadFactoryProvider, Executors newSingleThreadExecutor
* also includes the (as of 2018.09 WIP) ExecutionOrigin & Co. in infrautils.utils.mdc.

Test Utilities
--------------

* LogRule which logs (using slf4j-api) the start and stop of each @Test method
* LogCaptureRule which can fail a test if something logs any ERROR from anywhere (This work even if the LOG.error()
  is happening in a background thread, not the test's main thread... which can be particularly interesting.)
* RunUntilFailureRule which allows to keep running tests indefinitely; for local usage to debug "flaky"
  (sometimes passing, sometimes failing) tests until they fail
* ClasspathHellDuplicatesCheckRule verifies, and guarantees future non-regression, against JAR hell due
  to duplicate classpath entries. Tests with this JUnit Rule will fail if their classpath contains duplicate class.
  This could be caused e.g. by changes to upstream transitive dependencies. See also http://jhades.github.io (which this internally uses, not exposed).
  see https://github.com/opendaylight/infrautils/blob/master/testutils/src/test/java/org/opendaylight/infrautils/testutils/tests/ExampleTest.java
* Also some low level general utility classes helpful for unit testing concurrency related things in infrautils.testutils.concurrent:

  - AwaitableExecutorService
  - SlowExecutor
  - CompletionStageTestAwaiter, see CompletionStageAwaitExampleTest

Job Coordinator
---------------

JobCoordinator service which enables executing jobs in a parallel/sequential fashion based on their keys.
Originally in genius project, but moved to infrautils to be more widely available to other projects as well,
as it was deemed to be a technical infrastructure not related to genius' project network abstraction.
Subsequently improved within infrautils.

Ready Service
-------------

This was originally built for Daexim Import on Boot for Upgrade-ability.
It is now also used by infrautils.diagstatus, openflowplugin and ovsdb which builds on top of infrautils.ready.
The implementation internally ultimately uses the same Karaf API that e.g. the standard "diag" Karaf CLI command uses.
This checks both if all OSGi bundles have started as well if their blueprint initialization has been successfully fully completed.
It builds on top of the bundles-test-lib from odlparent, which is what we run as SingleFeatureTest (SFT) during all
of our builds to ensure that all projects' features can be installed without broken bundles.

What infrautils.ready adds on top of the underlying raw Karaf API is operator friendly logging,
a convenient API and a correctly implemented polling loop in a background thread with SystemReadyListener registrations and notifications,
instead of ODL applications re-implementing this. The infrautils.read intentionally API isolates consumers from the Karaf API.
We encourage all ODL projects to use this infrautils.ready API instead of trying to reinvent the wheel and directly depending on the Karaf API,
so that application code could be used outside of OSGi, in environment such as unit and component tests, or something such as honeycomb.

Applications can use this SystemReadyMonitor registerListener(SystemReadyListener) in a constructor to register a listener
for and get notified when all bundles are "ready" in the technical sense (have been started in the OSGi sense and have completed
their blueprint initialization), and could on that event do any further initialization it had to delay in the original blueprint initialization.

This cannot directly be used to express functional dependencies BETWEEN bundles (because that would deadlock infrautils.ready;
it would stay in BOOTING forever and never reach SystemState ACTIVE). The natural way to make one bundle await another is to
use Blueprint OSGi service dependency. If there is no technical service dependency but only a logical functional one,
then in infrautils.ready.order there is a convenience sugar utility to publish "marker" FunctionalityReady interfaces
to the OSGi service registry; unlike real services, these have no implementing code,
but another bundle could depend on one to enforce start up order one (using regular bluepring <reference> in XML or
@OsgiService annotation).

A known limitation of the current implementation of infrautils.ready is that its "wait until ready" loop runs only once,
after installation of infrautils.ready (by boot feature usage, or initial single line feature:install).
So SystemState will go from BOOTING to ACTIVE or FAILURE, once. So if you do more feature:install after a time gap,
there won’t be any further state change notification; the currently implementation won't "go back" from ACTIVE to BOOTING.
(It would be absolutely possible to extend SystemReadyListener onSystemBootReady() with an onSystemIsChanging()
and onSystemReadyAgain(), but the original author has no need for this; as "hot" installing additional ODL application
features during operational uptime was not a real world requirement to the original author. If this is important to you,
then your contributions for extending this would certainly be welcome.)

infrautils' ready, like other infrautils APIs, is available as a separate Karaf feature.
Downstream projects using infrautils.ready will therefore NOT pull in other bundles for other infrautils functionalities.

Integration Test Utilities (itestutils)
---------------------------------------

See https://bugs.opendaylight.org/show_bug.cgi?id=8438 and https://git.opendaylight.org/gerrit/#/c/56898/

Used for non-regression self-testing of features in this project (and available to others).

Caches
------

See https://www.youtube.com/watch?v=h4HOSRN2aFc and play with the example in infrautils/caches/sample installed
by odl-infrautils-caches-sample; history in https://git.opendaylight.org/gerrit/#/c/48920/
and https://bugs.opendaylight.org/show_bug.cgi?id=8300.

Diagstatus
----------

Metrics
-------

infrautils.metrics offers a simple back-end neutral API for all ODL applications to report technical as well as functional metrics.
There are different implementations of this API allowing operators to exploit metrics in the usual ways - aggregate, query, alerts, etc.
The odl-infrautils-metrics Karaf feature includes the API and the local Dropwizard implementation.
Application code uses the org.opendaylight.infrautils.metrics.MetricProvider API, typically looked up from the
OSGi service registry using e.g. Blueprint annotations @Inject @OsgiService, to register new Meters
(to "tick/mark events" and measure their rate), Counters (for things that go up and down again), and Timers (to stop watch durations).
Support for "Gauges" is to be added; contributions welcome.Each metric can be labeled, possibly along more than one dimension.
The org.opendaylight.infrautils.metrics.testimpl.TestMetricProviderImpl is a suitable implementation of the MetricProvider for tests.

Dropwizard Implementation
-------------------------

Based on Dropwizard Metrics (Coda Hale, Yammer), see http://metrics.dropwizard.io, exposes metrics to JMX and
can regularly dump stats into simple local files; background slide https://codahale.com/codeconf-2011-04-09-metrics-metrics-everywhere.pdf
This implementation is "embedded" and requires no additional external systems.
It is configured via the local configuration file at etc/org.opendaylight.infrautils.metrics.cfg.
This includes a threads deadlock detection and maximum number of threads warning feature.

Prometheus Implementation
-------------------------

Implementation based on Linux Foundation Cloud Native Computing Foundation Prometheus, see https://prometheus.io
This implementation exposes metrics by HTTP on /metrics/prometheus from the local ODL to an external Prometheus set up to scrape that.
This presentation given at the OpenDaylight Fluorine Developer Design Forum in March 2018 at ONS in LA
gives a good overview about the infrautils.metrics.prometheus implementation.
This implementation requires operators to separatly install Prometheus, which is not a Java OSGi application that
can be feature:install into Karaf, but an external application (via Docker, RPM, tar.gz etc.).
Prometheus is then configured with the URL of ODL nodes, and "scrapes" metrics from ODL in configurable regular intervals.
Prometheus is extensibly configurable for typical metrics use cases, including alerting, and has existing integrations with other related systems.
The odl-infrautils-metrics-prometheus Karaf feature install this. It has to be installed by feature:install or featuresBoot,
BEFORE any ODL application feature which depends on the odl-infrautils-metrics feature (similarly to e.g. odl-mdsal-trace)

Planned Features
----------------

* Logging with ExecutionOrigin ID
* web API with OSGi and standalone implementation for (non-IT) e2e component
* tests and simple distribution
* Linux Foundation (LF) Cloud Native Computing Foundation (CNCF) Open Tracing for OpenDaylight and its north- and southbound!



References
==========

[1] `Infrautils Metrics Prometheus Implementation <https://docs.google.com/presentation/d/1143hvgpFqqhQ-AcpC61AuW9-yV6B6iQUvCH7M5F1POs>`__

[2] `ODL DDF - LA 2018 <https://docs.google.com/presentation/d/1C2jbZP8C8FwoR9yoFMrMs-kKt8Uiv8r8vtXc1bocb7c/>`__

[3] `ODL DDF 2017 <https://docs.google.com/presentation/d/1S7WBPumuQxMBiGLf9Xt8SJkUTNgvrIZmwlBNYArRHzk/>`__