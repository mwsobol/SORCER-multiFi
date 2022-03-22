package sorcer.util;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;

public class Builder {

    public static Class parent;

    public static Context initData;

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
}
