/*
 * Copyright © 2014, 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean.OutputStreamOptions;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

public class ForbiddenMethodCheckTest {

    private Checker checker;
    private ByteArrayOutputStream baos;

    @Before
    public void setup() throws CheckstyleException {
        baos = new ByteArrayOutputStream();

        InputSource inputSource = new InputSource(
                ForbiddenMethodCheckTest.class.getClassLoader().getResourceAsStream("forbidden-method-test.xml"));
        Configuration configuration = ConfigurationLoader.loadConfiguration(inputSource,
                new PropertiesExpander(System.getProperties()), IgnoredModulesOptions.EXECUTE);

        checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());
        checker.configure(configuration);
        checker.addListener(new DefaultLogger(baos, OutputStreamOptions.NONE));
    }

    @After
    public void destroy() {
        checker.destroy();
    }

    @Test
    public void testForbiddenMethod() throws Exception {
        verify(ForbiddenMethodTestClass.class, true,
                "13: method forbiddenMethod must not be called",
                "14: method forbiddenMethod must not be called");
    }

    private void verify(Class<?> testClass, boolean checkCount, String... expectedMessages)
            throws CheckstyleException {
        String filePath =
                System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
                        + "java" + File.separator + testClass.getName().replaceAll("\\.", "/") + ".java";
        File testFile = new File(filePath);
        checker.process(Lists.newArrayList(testFile));
        String output = baos.toString();
        if (checkCount) {
            int count = output.split("\n").length - 2;
            assertEquals(expectedMessages.length, count);
        }
        for (String message : expectedMessages) {
            assertTrue("Expected message not found: " + message + "; output: " + output, output.contains(message));
        }
    }
}
