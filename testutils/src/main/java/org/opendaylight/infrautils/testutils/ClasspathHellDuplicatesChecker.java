/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import com.google.errorprone.annotations.Var;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ModuleRef;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Check classpath for duplicates.
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesChecker {

    public static final ClasspathHellDuplicatesChecker INSTANCE = new ClasspathHellDuplicatesChecker();

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    private Map<String, List<String>> duplicates;

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public Map<String, List<String>> check() {
        if (duplicates == null) {
            duplicates = recheck();
        }
        return duplicates;
    }

    public String toString(Map<String, List<String>> map) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, List<String>> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append('\n');
            for (String location : entry.getValue()) {
                sb.append("    ");
                sb.append(location);
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    protected Map<String, List<String>> recheck() {
        Map<String, List<String>> seen = new HashMap<>();
        // To debug this scanner, use ClassGraph().verbose()
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().ignoreClassVisibility().scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                String path = resource.getPath();
                @Var List<String> classpathElements = seen.get(path);
                if (classpathElements == null) {
                    classpathElements = new ArrayList<>(1);
                    seen.put(path, classpathElements);
                }
                classpathElements.add(getClasspathElementString(resource));
            }
        }
        return seen.entrySet().stream().filter(entry -> entry.getValue().size() > 1)
                .filter(entry -> !isHarmlessDuplicate(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SuppressFBWarnings("DM_CONVERT_CASE")
    protected boolean isHarmlessDuplicate(String resourcePath) {
        // list from org.jhades.reports.DuplicatesReport
        return resourcePath.equals("META-INF/MANIFEST.MF")
            || resourcePath.equals("META-INF/INDEX.LIST")
            || resourcePath.equals("META-INF/ORACLE_J.SF")
            || resourcePath.toUpperCase().startsWith("META-INF/NOTICE")
            || resourcePath.toUpperCase().startsWith("META-INF/LICENSE")
            || resourcePath.toUpperCase().startsWith("LICENSE")
            || resourcePath.toUpperCase().startsWith("LICENSE/NOTICE")
        // list formerly in ClasspathHellDuplicatesCheckRule (moved here in INFRAUTILS-52)
            || resourcePath.endsWith(".txt")
            || resourcePath.endsWith("LICENSE")
            || resourcePath.endsWith("license.html")
            || resourcePath.endsWith("about.html")
            || resourcePath.endsWith("readme.html")
            || resourcePath.equals("META-INF/services")
            || resourcePath.equals("META-INF/DEPENDENCIES")
            || resourcePath.equals("META-INF/git.properties")
            || resourcePath.equals("META-INF/io.netty.versions.properties")
            || resourcePath.equals("META-INF/jersey-module-version")
            || resourcePath.startsWith("OSGI-INF/blueprint/")
            || resourcePath.startsWith("org/opendaylight/blueprint/")
            || resourcePath.equals("WEB-INF/web.xml")
            || resourcePath.endsWith("reference.conf") // in Akka's JARs
            || resourcePath.equals("META-INF/eclipse.inf")
            || resourcePath.equals("META-INF/ECLIPSE_.SF")
            || resourcePath.equals("META-INF/ECLIPSE_.RSA")
            || resourcePath.equals("META-INF/BC2048KE.DSA")
            || resourcePath.equals("META-INF/BC2048KE.SF")
            || resourcePath.equals("META-INF/BC1024KE.SF")
            || resourcePath.equals("OSGI-INF/bundle.info")
            // Something doesn't to be a perfectly clean in Maven Surefire:
            || resourcePath.startsWith("META-INF/maven/")
            || resourcePath.contains("surefire")
            // org.slf4j.impl.StaticLoggerBinder.class in testutils for the LogCaptureRule
            || resourcePath.equals("org/slf4j/impl/StaticLoggerBinder.class")
            // INFRAUTILS-35: JavaLaunchHelper is both in java and libinstrument.dylib (?) on Mac OS X
            || resourcePath.contains("JavaLaunchHelper")
            // javax.annotation is a big mess... :( E.g. javax.annotation.Resource (and some others)
            // are present both in rt.jar AND javax.annotation-api-1.3.2.jar and similar - BUT those
            // JARs cannot just be excluded, because they contain some additional annotations, in the
            // (reserved!) package javax.annotation, such as javax.annotation.Priority et al.  The
            // super proper way to address this cleanly would be to make our own JAR for javax.annotation
            // and have it contain ONLY what is not already in package javax.annotation in rt.jar.. but for now:
            || resourcePath.equals("javax/annotation/Resource$AuthenticationType.class")
            // Java 9 modules
            || resourcePath.equals("module-info.class")
            || resourcePath.contains("findbugs")
        // list newly introduced in INFRAUTILS-52, because classgraph scans more than JHades did
            || resourcePath.equals("plugin.properties")
            || resourcePath.equals(".api_description")
            ;
    }

    protected String getClasspathElementString(Resource resource) {
        File file = resource.getClasspathElementFile();
        if (file != null) {
            return file.toString();
        } else {
            ModuleRef moduleRef = resource.getModuleRef();
            if (moduleRef != null) {
                return moduleRef.toString();
            } else {
                return "???";
            }
        }
    }
}
