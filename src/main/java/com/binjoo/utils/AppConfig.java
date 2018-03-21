package com.binjoo.utils;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import com.binjoo.core.ParaMap;

public class AppConfig {
    private static ParaMap cfgMap;

    public static synchronized void init() {
        if (cfgMap != null)
            return;
        try {
            cfgMap = new ParaMap();
            ClassLoader classLoader = AppConfig.class.getClassLoader();
            InputStream is = classLoader.getResourceAsStream("/appConfig.properties");
            Properties appConfig = new Properties();
            appConfig.load(is);
            Iterator it = appConfig.keySet().iterator();
            while (it.hasNext()) {
                String key = String.valueOf(it.next()).trim();
                String value = appConfig.getProperty(key).trim();
                cfgMap.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPro(String key) {
        init();
        if (cfgMap.containsKey(key))
            return cfgMap.getString(key);
        else
            return null;
    }

    public static Integer getIntPro(String key) {
        init();
        return cfgMap.getInteger(key);
    }

}
