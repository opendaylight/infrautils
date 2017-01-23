/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import junit.framework.AssertionFailedError;
import org.jhades.JHades;
import org.jhades.model.ClasspathResource;
import org.jhades.model.ClasspathResourceVersion;
import org.jhades.service.ClasspathScanner;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule to run <a href="http://jhades.github.io">JHades</a>. Usage:
 *
 * <pre>public static {@literal @}ClassRule JHadesRule jHades = new JHadesRule();</pre>
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
        final boolean excludeSameSizeDups = false;
        final ClasspathScanner scanner = new ClasspathScanner();
        final List<ClasspathResource> resourcesWithDuplicates = scanner
                .findAllResourcesWithDuplicates(excludeSameSizeDups);
        final List<ClasspathResource> filteredResourcesWithDuplicates = filterHarmlessKnownIssues(
                resourcesWithDuplicates);
        if (!filteredResourcesWithDuplicates.isEmpty()) {
            new JHades()
                .printClassLoaderNames()
                .printClasspath()
                .overlappingJarsReport()
                .multipleClassVersionsReport(excludeSameSizeDups);
            throw new AssertionFailedError("Classpath errors detected "
                    + "(see full report printed to STDOUT; but note some dupes are filtered): "
                    + filteredResourcesWithDuplicates);
        }
    }

    protected List<ClasspathResource> filterHarmlessKnownIssues(List<ClasspathResource> resourcesWithDuplicates) {
        resourcesWithDuplicates = filterFindBugsAnnotation(resourcesWithDuplicates);
        resourcesWithDuplicates = filterTXT(resourcesWithDuplicates);
        return resourcesWithDuplicates;
    }

    private List<ClasspathResource> filterTXT(List<ClasspathResource> resourcesWithDuplicates) {
        return resourcesWithDuplicates.stream()
                .filter(classpathResource -> !classpathResource.getName().endsWith(".txt"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/META-INF/NOTICE"))
                .filter(classpathResource -> !classpathResource.getName().endsWith("/META-INF/LICENSE"))
                .collect(Collectors.toList());
    }

    // TODO remove when https://git.opendaylight.org/gerrit/#/c/47337/ is sorted out
    private List<ClasspathResource> filterFindBugsAnnotation(List<ClasspathResource> resourcesWithDuplicates) {
        ArrayList<ClasspathResource> filteredList = new ArrayList<>(resourcesWithDuplicates);
        for (ClasspathResource classpathResource : resourcesWithDuplicates) {
            for (ClasspathResourceVersion classpathResourceVersion : classpathResource.getResourceFileVersions()) {
                if (classpathResourceVersion.getClasspathEntry().getUrl().contains("findbugs")) {
                    filteredList.remove(classpathResource);
                }
            }
        }
        return filteredList;
    }

}
