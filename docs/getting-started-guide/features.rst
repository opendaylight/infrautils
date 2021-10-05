
.. contents:: Table of Contents
      :depth: 4

===================
Infrautils Features
===================

This project offers technical utilities and infrastructures for other projects to use.

The conference presentation slides linked to in the references section at the end give a good overview of the project.

Check out the JavaDoc on https://javadocs.opendaylight.org/org.opendaylight.infrautils/fluorine/.


Features
========

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

Ready Service
-------------

Infrastructure to detect when Karaf is ready.

The implementation internally uses the same Karaf API that e.g. the standard "diag" Karaf CLI command uses.
This checks both if all OSGi bundles have started as well if their blueprint initialization has been successfully fully completed.

It builds on top of the bundles-test-lib from odlparent, which is what we run as SingleFeatureTest (SFT) during all
of our builds to ensure that all projects' features can be installed without broken bundles.

The infrautils.diagstatus modules builds on top of this infrautils.ready.

What infrautils.ready adds on top of the underlying raw Karaf API is operator friendly logging,
a convenient API and a correctly implemented polling loop in a background thread with SystemReadyListener registrations and notifications,
instead of ODL applications re-implementing this. The infrautils.ready project intentionally API isolates consumers from the Karaf API.
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
but another bundle could depend on one to enforce start up order one (using regular Blueprint <reference> in XML or
@Reference annotation).

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

Diagstatus
----------

To be documented.


References
==========

[2] `ODL DDF - LA 2018 <https://docs.google.com/presentation/d/1C2jbZP8C8FwoR9yoFMrMs-kKt8Uiv8r8vtXc1bocb7c/>`__

[3] `ODL DDF 2017 <https://docs.google.com/presentation/d/1S7WBPumuQxMBiGLf9Xt8SJkUTNgvrIZmwlBNYArRHzk/>`__

[4] `infrautils JavaDoc <https://javadocs.opendaylight.org/org.opendaylight.infrautils/fluorine/>`__
