/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public class PropertyHelper {
    private final Properties properties = new Properties();
    private static Logger logger = LoggerFactory.getLogger(PropertyHelper.class.getName());
    private URL resourceURL;

    public PropertyHelper(String fileName) throws IOException {
        File propertiesFile = new File(fileName);
        if (propertiesFile.exists()) {
            InputStream is = new FileInputStream(fileName);
            this.properties.load(is);
            logger.info("* loaded properties from file: " + fileName);
            is.close();
        } else {
            this.asResource(fileName, Thread.currentThread().getContextClassLoader());
        }

    }

    public PropertyHelper(String fileName, ClassLoader resourceLoader) throws IOException {
        this.asResource(fileName, resourceLoader);
    }

    public PropertyHelper(Properties properties) {
        if (properties != null) {
            this.properties.putAll(properties);
        }

    }

    private void asResource(String fileName, ClassLoader resourceLoader) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Using " + resourceLoader.getClass().getName() + ": " + resourceLoader);
            Enumeration<URL> resources = resourceLoader.getResources(fileName);

            StringBuilder sb;
            for(sb = new StringBuilder(); resources.hasMoreElements();
                sb.append("\t").append(((URL)resources.nextElement()).toExternalForm())) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
            }

            logger.debug("Found Resources\n" + (sb.length() == 0 ? "<No resources found>" : sb.toString()));
        }

        this.resourceURL = resourceLoader.getResource(fileName);
        if (this.resourceURL != null) {
            logger.info("Loaded from " + this.resourceURL.toExternalForm());
            InputStream rs = this.resourceURL.openStream();
            logger.info("* Loaded properties from resource as stream: " + fileName);
            this.properties.load(rs);
            rs.close();
            logger.info("* Loading properties using: " + rs);
        }

    }

    public String getValue(String key) {
        return key != null && this.properties.getProperty(key) != null ?
            org.rioproject.util.PropertyHelper.expandProperties(this.properties.getProperty(key)) : null;
    }

    public String getValue(String key, String defaultValue) {
        return key == null ? defaultValue :
            this.properties.getProperty(org.rioproject.util.PropertyHelper.expandProperties(key),
                defaultValue);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public URL getResourceURL() {
        return this.resourceURL;
    }
}


