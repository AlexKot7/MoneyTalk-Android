package ru.tinkoff.telegram.mt.network.parse;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.tinkoff.telegram.mt.Glue;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.network.responses.BaseResult;
import ru.tinkoff.telegram.mt.network.responses.IJsonable;

/**
 * @author a.shishkin1
 */


public class DefaultParsers {


    public static Network.IParser<String> TO_STRING_PARSER = new Network.IParser<String>() {
        @Override
        public String parse(InputStream stream) {
            if(stream == null)
                return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = stream.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String res = new String(baos.toByteArray());
            if(Glue.DEBUG)
                Log.i("Network.parse", res);
            return res;
        }
    };


    public static Network.IParser<JSONObject> TO_JSON_OBJECT = new Network.IParser<JSONObject>() {
        @Override
        public JSONObject parse(InputStream stream) {
            try {
                String candidate = TO_STRING_PARSER.parse(stream);
                if(candidate == null)
                    return null;
                return new JSONObject(candidate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    };


    public static Network.IParser<BaseResult> BASE_RESULT_PARSER = new JsonableParser<>(BaseResult.class);




}
