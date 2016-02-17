package ru.tinkoff.telegram.mt.network.parse;

import org.json.JSONObject;

import java.io.InputStream;

import ru.tinkoff.telegram.mt.network.Network;

/**
 * @author a.shishkin1
 */
public abstract class BaseParser<T> implements Network.IParser<T> {

    @Override
    public T parse(InputStream stream) {
        JSONObject jobj = DefaultParsers.TO_JSON_OBJECT.parse(stream);
        return parse(jobj);
    }

    public abstract T parse(JSONObject jobj);
}
