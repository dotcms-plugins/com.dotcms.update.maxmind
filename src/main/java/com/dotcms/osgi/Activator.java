/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dotcms.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.osgi.framework.BundleContext;
import com.dotcms.filters.interceptor.FilterWebInterceptorProvider;
import com.dotcms.filters.interceptor.WebInterceptorDelegate;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.filters.InterceptorFilter;
import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.liferay.util.FileUtil;
import io.vavr.control.Try;

public final class Activator extends GenericBundleActivator {


    private LoggerContext pluginLoggerContext;

    @Override
    public void start(BundleContext context) throws Exception {



        // Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        // Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(), false, dotcmsLoggerContext,
                        dotcmsLoggerContext.getConfigLocation());

        // Initializing services...
        initializeServices(context);

        // copyGeoDbToDotCMS() ;
        copyGeoDbToDotCMS();

    }

    @Override
    public void stop(BundleContext context) throws Exception {


        // Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }



    private void copyGeoDbToDotCMS() throws IOException {

        SimpleDateFormat timestampFormatter = new SimpleDateFormat("yy-MM-dd_HH-mm-ss");
        File dotCMSGeo = new File(Config.CONTEXT.getRealPath("/WEB-INF/geoip2/GeoLite2-City.mmdb"));
        File dotCMSGeoBackup = new File(Config.CONTEXT
                        .getRealPath("/WEB-INF/geoip2/GeoLite2-City-" + timestampFormatter.format(new Date()) + ".mmdb"));
        File dotCMSGeoNew = new File(Config.CONTEXT.getRealPath("/WEB-INF/geoip2/GeoLite2-City-new.mmdb"));



        String currentMD5 = null;
        try (InputStream is = java.nio.file.Files.newInputStream(dotCMSGeo.toPath())) {
            currentMD5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        }

        String incomingMD5 = null;
        String incomingGeoDB = "/GeoLite2-City.mmdb";

        try (InputStream is = this.getClass().getResourceAsStream(incomingGeoDB)) {
            incomingMD5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        }

        Logger.info(this.getClass().getName(), "Current GeoDB MD5 : " + currentMD5);
        Logger.info(this.getClass().getName(), "New GeoDB MD5     : " + incomingMD5);
        // do we need to replace the old file?
        if (incomingMD5 != null && !currentMD5.equals(incomingMD5)) {
            Logger.info(this.getClass().getName(), "Replacing " + incomingGeoDB);
            if (dotCMSGeo.exists()) {
                FileUtil.copyFile(dotCMSGeo, dotCMSGeoBackup);
            }

            try (final InputStream in = this.getClass().getResourceAsStream(incomingGeoDB)) {
                IOUtils.copy(in, new FileOutputStream(dotCMSGeoNew));
            }

            if (dotCMSGeo.exists()) {
                dotCMSGeo.delete();
            }
            FileUtil.copyFile(dotCMSGeoNew, dotCMSGeo);



            currentMD5 = null;
            try (InputStream is = java.nio.file.Files.newInputStream(dotCMSGeo.toPath())) {
                currentMD5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
            }

            Logger.info(this.getClass().getName(), "Updated GeoDB MD5 : " + currentMD5);

        }



    }



}
