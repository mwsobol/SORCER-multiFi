package sorcer.util;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Builder {

    public static Class parent;

    public static Context initData;

    public static Properties properties;

    public static Object initData(String path) {
        if (initData != null) {
            return (( ServiceContext )initData).get(path);
        } else {
            return null;
        }
    }

    public static Object initData(String path, Object defaulValue) {
        Object initValue = defaulValue;
        if (initData != null) {
            initValue = (( ServiceContext )initData).get(path);
        }
        if (initValue == null && defaulValue != null) {
            return defaulValue;
        } else {
            return initValue;
        }
    }

    public static void setParent(Class clazz) {
        parent = clazz;
    }

    public static String property(String property) {
        return System.getProperty(property);
    }

    public static String property(String property, Properties properties) {
        return properties.getProperty(property);
    }

    public static String setProperty(String property, String value) {
        return System.setProperty(property, value);
    }

    public static Object setProperty(String property,  String value, Properties properties) {
        return properties.setProperty(property, value);
    }

    public static Properties resourceProperties(String resourceName, Object dependent) throws IOException {
        ClassLoader classLoader = dependent.getClass().getClassLoader();
        URL url = classLoader.getResource(resourceName);
        Properties properties = new Properties();
        try (InputStream rs = url.openStream()) {
            properties.load(rs);
        }
        return properties;
    }

    public static Properties providerProperties(String propertiesFile, Object provider) throws IOException {
        Class<?> providerClass;
        if (provider instanceof Class) {
            providerClass = (Class) provider;
        } else {
            providerClass = provider.getClass();
        }
        return new PropertyHelper(propertiesFile, providerClass.getClassLoader()).getProperties();
    }

    public static Properties modelProperties(Class<?> providerClass) throws IOException {
        String propertiesFile = System.getProperty("model.properties");
        return new PropertyHelper(propertiesFile, providerClass.getClassLoader()).getProperties();
    }

    public static Properties providerProperties(Class<?> providerClass) throws IOException {
        String propertiesFile = System.getProperty("provider.properties");
        return new PropertyHelper(propertiesFile, providerClass.getClassLoader()).getProperties();
    }

}
