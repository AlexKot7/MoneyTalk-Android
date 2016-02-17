package ru.tinkoff.telegram.mt.entities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.tinkoff.telegram.mt.network.responses.IJsonable;

/**
 * @author a.shishkin1
 */


public class MtConfig implements IJsonable {

    private Map<String, Object> config;

    private transient Map<String, Object> cache;


    public MtConfig(JSONObject jobj) {
        fillByJson(jobj);
    }

    @Override
    public void fillByJson(JSONObject jobj) {
        config = new HashMap<>();
        cache = new HashMap<>();
        fillMap(null, jobj, config);
    }

    private void fillMap(String path, JSONObject jobj, Map<String, Object> map) {
        Iterator<String> keys = jobj.keys();
        String key;
        while (keys.hasNext()) {
            key = keys.next();
            String rekey = ((path == null) ? "" : (path + ".")) + key;
            Object object = jobj.opt(key);
            if(object == null)
                continue;
            if(object instanceof JSONObject) {
                fillMap(rekey, (JSONObject) object, map);
            } else if (object instanceof JSONArray) {
                fillMap(rekey, (JSONArray) object, map);
            } else {
                map.put(rekey, object);
            }
        }
    }

    private void fillMap(String path, JSONArray jarr, Map<String, Object> map) {
        for(int i = 0; i < jarr.length(); i++) {
            String rekey = ((path == null) ? "" : (path + ".")) + "[" + i + "]";
            Object object = jarr.opt(i);
            if(object == null)
                continue;
            if(object instanceof JSONObject) {
                fillMap(rekey, (JSONObject) object, map);
            } else if (object instanceof JSONArray) {
                fillMap(rekey, (JSONArray) object, map);
            } else {
                map.put(rekey, object);
            }
        }
    }

    public Object get(String key) {
        return config.get(key);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    @Override
    public JSONObject createJson() {
        return null;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Object> entry : config.entrySet()) {
            sb.append("[" + entry.getKey() + " : " + entry.getValue() + "]\n");
        }
        return sb.toString();
    }
}
