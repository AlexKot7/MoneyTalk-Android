package ru.tinkoff.telegram.mt.network.parse;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ru.tinkoff.telegram.mt.network.responses.IJsonable;

/**
 * @author a.shishkin1
 */


public class JsonableParser<T extends IJsonable> extends BaseParser<T> {

    private Constructor c;

    public JsonableParser(Class cl) {
        if(!IJsonable.class.isAssignableFrom(cl)) {
            throw new IllegalArgumentException("only IJsonable can parse by JsonableParser");
        }
        try {
            this.c = cl.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class must have default constructor");
        }
    }

    @Override
    public T parse(JSONObject jobj) {
        try {
            if(jobj == null) {
                return null;
            }
            Object res = c.newInstance();
            ((IJsonable)res).fillByJson(jobj);
            return (T)res;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
