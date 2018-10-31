/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import java.util.List;
import java.util.stream.Collectors;
import junit.framework.AssertionFailedError;
import org.jhades.JHades;
import org.jhades.model.ClasspathResource;
import org.jhades.model.ClasspathResourceVersion;
import org.jhades.reports.DuplicatesReport;
import org.jhades.service.ClasspathScanner;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule to run <a href="http://jhades.github.io">JHades</a>. Usage:
 *
 * <pre>public static {@literal @}ClassRule ClasspathHellDuplicatesCheckRule
 *     dupes = new ClasspathHellDuplicatesCheckRule();</pre>
 *
 * <p>NB that the basepom/duplicate-finder-maven-plugin already runs as part of odlparent.
 * (The org.codehaus.mojo:extra-enforcer-rules is a very similar alternative Maven plugin).
 * It has a similar purpose, but covers build time instead of runtime testing.  This JUnit Rule class is
 * thus recommended to be used in particular in tests which previously ran into JAR Hell issues, and for
 * which non-regression with a clear failure message in case of future similar problems is important.
 * (JHades provides more details at runtime than duplicate-finder-maven-plugin does at build time.)
 *
 * <p>Somewhat similar alternative tool to JHades is <a href="http://tattletale.jboss.org">Tattletale</a>
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesCheckRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        checkClasspath();
        return base;
    }

    protected void checkClasspath() {
        boolean excludeSameSizeDups = true;
        ClasspathScanner scanner = new ClasspathScanner();
        List<ClasspathResource> resourcesWithDuplicates = scanner
                .findAllResourcesWithDuplicates(excludeSameSizeDups);
        List<ClasspathResource> filteredResourcesWithDuplicates = filterHarmlessKnownIssues(
                resourcesWithDuplicates);
        if (!filteredResourcesWithDuplicates.isEmpty()) {
            new JHades()
                // .printClassLoaderNames()
                .printClasspath()
                .overlappingJarsReport();
            // Instead of JHades.multipleClassVersionsReport() we call our own, to report only filtered dupes:
            new DuplicatesReport(filteredResourcesWithDuplicates).print();
            throw new AssertionFailedError("Classpath errors detected (see full report printed to STDOUT)");
        }
    }

    protected List<ClasspathResource> filterHarmlessKnownIssues(List<ClasspathResource> resourcesWithDuplicates) {
        List<ClasspathResource> filteredResourcesWithDuplicates = filterFindBugsAnnotation(resourcesWithDuplicates);
        return filterMore(filteredResourcesWithDuplicates);
    }

    private static List<ClasspathResource> filterMore(List<ClasspathResource> resourcesWithDuplicates) {
        return resourcesWithDuplicates.stream()
                .filter(classpathResource -> !classpathResource.getName().endsWith(".txt"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("LICENSE"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/license.html"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/about.html"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/readme.html"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/META-INF/NOTICE"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/META-INF/LICENSE"))
                .filter(classpathResource -> !classpathResource.getName().contains("META-INF/services"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/META-INF/DEPENDENCIES"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/META-INF/git.properties"))
                .filter(classpathResource -> !classpathResource.getName().endsWith(".txt"))
                .filter(r -> !r.getName().endsWith("/META-INF/io.netty.versions.properties"))
                .filter(r -> !r.getName().endsWith("/META-INF/jersey-module-version"))
                .filter(r -> !r.getName().startsWith("/OSGI-INF/blueprint/"))
                .filter(r -> !r.getName().startsWith("/org/opendaylight/blueprint/"))
                .filter(r -> !r.getName().endsWith("/WEB-INF/web.xml"))
                .filter(r -> !r.getName().endsWith("/reference.conf")) // in Akka's JARs
                .filter(r -> !r.getName().endsWith("/META-INF/eclipse.inf"))
                .filter(r -> !r.getName().endsWith("/META-INF/ECLIPSE_.SF"))
                .filter(r -> !r.getName().endsWith("/META-INF/ECLIPSE_.RSA"))
                .filter(r -> !r.getName().endsWith("/META-INF/BC2048KE.DSA"))
                .filter(r -> !r.getName().endsWith("/META-INF/BC2048KE.SF"))
                .filter(r -> !r.getName().endsWith("/META-INF/BC1024KE.SF"))
                .filter(r -> !r.getName().endsWith("/OSGI-INF/bundle.info"))
                // Something doesn't to be a perfectly clean in Maven Surefire:
                .filter(classpathResource -> !classpathResource.getName().contains("/META-INF/maven/"))
                .filter(classpathResource -> !classpathResource.getName().contains("surefire"))
                // org.slf4j.impl.StaticLoggerBinder.class in testutils for the LogCaptureRule
                .filter(r -> !r.getName().contains("/org/slf4j/impl/StaticLoggerBinder.class"))
                // INFRAUTILS-35: JavaLaunchHelper is both in java and libinstrument.dylib (?) on Mac OS X
                .filter(r -> !r.getName().contains("JavaLaunchHelper"))
                // javax.annotation is a big mess... :( E.g. javax.annotation.Resource (and some others)
                // are present both in rt.jar AND javax.annotation-api-1.3.2.jar and similar - BUT those
                // JARs cannot just be excluded, because they contain some additional annotations, in the
                // (reserved!) package javax.annotation, such as javax.annotation.Priority et al.  The
                // super proper way to address this cleanly would be to make our own JAR for javax.annotation
                // and have it contain ONLY what is not already in package javax.annotation in rt.jar.. but for now:
                .filter(r -> !r.getName().equals("/javax/annotation/Resource$AuthenticationType.class"))
                // Java 9 modules
                .filter(r -> !r.getName().equals("/module-info.class"))
                .collect(Collectors.toList());
    }

    private static List<ClasspathResource> filterFindBugsAnnotation(List<ClasspathResource> resourcesWithDuplicates) {
        return resourcesWithDuplicates.stream().filter(classpathResource -> {
            for (ClasspathResourceVersion classpathResourceVersion : classpathResource.getResourceFileVersions()) {
                if (classpathResourceVersion.getClasspathEntry().getUrl().contains("findbugs")) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

}
