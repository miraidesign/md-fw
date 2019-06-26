//------------------------------------------------------------------------
//    JsonUtil.java
//
//------------------------------------------------------------------------
//
//------------------------------------------------------------------------

package com.miraidesign.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JsonUtil {

    public static Object[] getArray(CharSequence json, String code) {
        Object obj = get(json, code);
        if (obj instanceof Object[]) {
            return (Object[]) obj;
        } else {
            return null;
        }
    }
    public static String getString(CharSequence json, String code) {
        Object obj = get(json,code);
        if (obj == null) return null;
        return obj.toString();
    }
    public static CharArray getCharArray(CharSequence json, String code) {
        Object obj = get(json,code);
        if (obj == null) return null;
        return new CharArray(obj.toString());
    }
    public static int getInt(CharSequence json, String code) {
        Object obj = get(json,code);
        if (obj == null) return 0;
        return CharArray.getInt(obj.toString());
    }
    public static long getLong(CharSequence json, String code) {
        Object obj = get(json,code);
        if (obj == null) return 0;
        return CharArray.getLong(obj.toString());
    }
    public static double getDouble(CharSequence json, String code) {
        Object obj = get(json,code);
        if (obj == null) return 0;
        return CharArray.getDouble(obj.toString());
    }
    public static boolean getBoolean(CharSequence json, String code) {
        Object obj = get(json,code);
        if (obj == null) return false;
        return CharArray.getBoolean(obj.toString());
    }
    public static Object get(CharSequence json, String code) {
        // Get the JavaScript engine
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        String script = "var obj = " + json + ";";
        try {
            engine.eval(script);
            {
                Object obj = engine.eval("obj." + code);
                if (obj instanceof Map) {
                    java.util.Map map = (java.util.Map) obj;
                    Set entrySet = map.entrySet();
                    Object[] arr = new Object[entrySet.size()];
                    int n = 0;
                    for (Object objValue : map.values()) {
                        if (objValue instanceof String) {
                            String sValue = (String) objValue;
                            arr[n] = sValue;
                        } else {
                            arr[n] = map.get(obj);
                        }
                        n++;
                    }
                    return arr;
                }
                return obj;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}

