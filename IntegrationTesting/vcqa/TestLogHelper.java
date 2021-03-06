/*
 * ************************************************************************
 *
 * Copyright 2010 VMware, Inc. All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package com.vmware.vcqa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Class to handle the MDC setting and removing on per test case basis. This helps
 * us log each test case into it's own log file. Please see
 * {@link http://logback.qos.ch/manual/appenders.html#SiftingAppender}
 * and {@link http://logback.qos.ch/manual/mdc.html}
 * @author nalinm
 *
 */
public class TestLogHelper
{
   private static final Logger log = LoggerFactory.getLogger(TestLogHelper.class);
   public static final String TEST_NAME = "testname";

   /**
    * Adds the test name to MDC so that sift appender can use it and log the new
    * log events to a different file
    * @param name name of the new log file
    * @throws Exception
    */
   public static void
   startTestLogging(String name) throws Exception
   {
      MDC.put(TEST_NAME, name);
   }

   /**
    * Removes the key (log file name) from MDC
    * @return name of the log file, if one existed in MDC
    */
   public static String
   stopTestLogging()
   {
      String name = MDC.get(TEST_NAME);
      MDC.remove(TEST_NAME);
      return name;
   }
}
