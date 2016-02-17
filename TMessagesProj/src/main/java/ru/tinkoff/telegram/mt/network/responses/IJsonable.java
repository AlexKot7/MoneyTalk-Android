package ru.tinkoff.telegram.mt.network.responses;

import org.json.JSONObject;

/**
 * @author a.shishkin1
 */


public interface IJsonable {

    void fillByJson(JSONObject jobj);

    JSONObject createJson();

}
