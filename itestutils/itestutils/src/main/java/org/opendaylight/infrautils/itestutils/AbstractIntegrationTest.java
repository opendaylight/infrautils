/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.itestutils;

import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption.Platform.NIX;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption.Platform.WINDOWS;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.io.File;
import org.junit.runner.RunWith;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.options.UrlReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration Test base class.
 *
 * @author Michael Vorburger.ch
 */
@RunWith(PaxExam.class)
public abstract class AbstractIntegrationTest {

    // TODO compare if anything to take from org.opendaylight.controller.config.it.base.AbstractConfigTestBase?

    // TODO integrate this with infra.ready/bundle[4]-test to ensure all bundles have finished wiring before test start
    //        and/or use @Inject protected [private?] org.apache.karaf.features.BootFinished bootFinished; ?

    // TODO Jacoco support, for (only?) build on Sonar, see Netvirt IT
    // TODO EclEmma support for coverage view in Eclipse

    // TODO look into and fix the ugly JMX errors (on Karaf 4)

    // TODO util to customize logging with a NICE and SIMPLE API.. Class<?> or String

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    // NB: The sys.prop. named "maven.repo.local" is the Maven standard one
    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";
    private static final String KARAF_DEBUG_PROP = "karaf.debug";
    private static final String KARAF_DEBUG_PORT = "5005";
    private static final String ETC_ORG_OPS4J_PAX_URL_MVN_CFG = "etc/org.ops4j.pax.url.mvn.cfg";

    @Configuration
    public final Option[] config() {
        // check that the Karaf distribution is not on the classpath:
        try {
            getClass().getClassLoader().loadClass("org.apache.karaf.main.Main");
            fail("""
                Found Karaf's main class on the test's classpath; remove <dependency> to Karaf JARs in the POM!\
                 (just the zip/tar.gz distribution, with excludes, is fine however,\
                 as that doesn't pollute the classpath)""");
        } catch (ClassNotFoundException e) {
            // Good!
        }

        File targetPaxExam = new File("target/paxexam/");
        FileUtils.delete(targetPaxExam);

        String karafVersion = getKarafVersion();
        MavenUrlReference karafURL = getKarafURL();
        // TODO https://ops4j1.jira.com/browse/PAXEXAM-813
        // String? karafURL = [url(]"link:classpath:" + karafArtifactId + ".link";
        LOG.info("Karaf v{} used: {}", karafVersion, karafURL.toString());

        Option[] defaultOptions = new Option[] {
            // Make sure karaf's default repository is consulted before anything else
            editKarafConfigurationFile(MAVEN_REPO_LOCAL, ETC_ORG_OPS4J_PAX_URL_MVN_CFG,
                "org.ops4j.pax.url.mvn.defaultRepositories",
                "file:${karaf.home}/${karaf.default.repository}@id=system.repository"),
            editKarafConfigurationFile(MAVEN_REPO_LOCAL, ETC_ORG_OPS4J_PAX_URL_MVN_CFG,
                "org.ops4j.pax.url.mvn.repositories", "https://repo1.maven.org/maven2@id=central "),


            // We need this, for the moment, because the feature repo is read from the local Maven repo
            // TODO remove this, and make all ITs use a Karaf dist which has the feature already
            //      so there would be no need to access repo; that would lead to better isolation
            editKarafConfigurationFile(MAVEN_REPO_LOCAL, ETC_ORG_OPS4J_PAX_URL_MVN_CFG,
                    "org.ops4j.pax.url.mvn.localRepository", System.getProperty(MAVEN_REPO_LOCAL, "")),
            editKarafConfigurationFile(MAVEN_REPO_LOCAL, ETC_ORG_OPS4J_PAX_URL_MVN_CFG,
                    "org.ops4j.pax.url.mvn.disableAether", "true"),

            when(Boolean.getBoolean(KARAF_DEBUG_PROP))
                .useOptions(KarafDistributionOption.debugConfiguration(KARAF_DEBUG_PORT, true)),

            new KarafDistributionKitConfigurationOption(karafURL, NIX)
                .karafVersion(karafVersion)
                .useDeployFolder(false).unpackDirectory(targetPaxExam),
            new KarafDistributionKitConfigurationOption(karafURL, WINDOWS)
                .karafVersion(karafVersion)
                .useDeployFolder(false).unpackDirectory(targetPaxExam),

            // DO *NOT* add new VMOption() for -Xmx, -XX:*, java.security.egd etc. here! All those should
            // just be in our standard launch scripts, which by KarafDistributionKitConfigurationOption we ARE using!
            //
            // Likewise, please DO *NOT* add other magic here, such as mavenBundle("org.apache.aries.quiesce") or
            // systemPackages() - any tweaks like that should either be in the standard ODL empty Karaf distribution
            // in odlparent, or not needed!!

            // keeping it around can be handy to go poke around it manually after test failures;
            // NB that this very quickly fills up your disk with tens of thousands of files,
            // which is why we have it on by default (easier than having to remember to set a property..),
            // but we wipe it out before every test run (above)
            keepRuntimeFolder(),

            // TODO bug: it looks like this option is ignored when using KarafDistributionKitConfigurationOption?
            configureConsole()
                .ignoreLocalConsole()  // no useless console, as under Karaf 4 the ANSI Art colour codes (banner)
                                       // screw up the Console view in Eclipse when running test there... :(
                .ignoreRemoteShell(),  // remoteShell defaults to true (?), so save time, as not required

            // TODO Experiment with not needing this by scanning this call via reflection, find dependencies,
            // and injecting 'em all via a @ProbeBuilder probe.addTest ... that could be cool!
            // or https://ops4j1.jira.com/projects/PAXEXAM/issues/PAXEXAM-543 ?
            // features(maven("org.apache.karaf.features","standard",
            //         karafVersion).classifier("features").type("xml"), "wrap"),
            // wrappedBundle(maven("com.google.truth", "truth").versionAsInProject()),
            // TODO Guava is "just" a dependency of Truth.. ideally it should not be loaded like this here
            // because that could hide problems in feature definitions.. we should probably repackage it inside truth?
            // mavenBundle(maven("com.google.guava", "guava").versionAsInProject()),

            when(featureRepositoryURL() != null)
                .useOptions(features(featureRepositoryURL(), featureNames()))
        };

        return OptionUtils.combine(defaultOptions, getAdditionalPaxExamOptions());
    }

    protected static final String getKarafVersion() {
        // This karafVersion must match the exact minor version of Karaf due to
        // https://bugs.opendaylight.org/show_bug.cgi?id=8578
        // (see also https://ops4j1.jira.com/projects/PAXEXAM/issues/PAXEXAM-598)
        return MavenUtils.getArtifactVersion("org.apache.karaf.features", "standard");
    }

    protected MavenUrlReference getKarafURL() {
        return maven().groupId("org.opendaylight.odlparent")
                   .artifactId("opendaylight-karaf-empty").versionAsInProject().type("tar.gz");
        // NB the tar.gz is almost half the size of the zip, so use that, even for Windows (works fine)
    }

    protected abstract UrlReference featureRepositoryURL();

    protected String[] featureNames() {
        return new String[] { featureName() };
    }

    protected abstract String featureName();

    /**
     * Override this method to provide more options to config.
     *
     * @return An array of additional config options
     */
    protected Option[] getAdditionalPaxExamOptions() {
        return new Option[0];
    }

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.addTest(AbstractIntegrationTest.class);
        // TODO util to enumerate all inner classes; probe.addTest(LogRule.class);
        return probe;
    }

    private static Option editKarafConfigurationFile(String source, String configFilePath, String key, String value) {
        LOG.warn("{}: In {} change {} = {}", source, configFilePath, key, value);
        return KarafDistributionOption.editConfigurationFilePut(configFilePath, key, value);
    }

    // TODO public @Rule LogRule logRule = new LogRule();

    // TODO ClasspathHellDuplicatesCheckRule https://git.opendaylight.org/gerrit/#/c/50851/

}
