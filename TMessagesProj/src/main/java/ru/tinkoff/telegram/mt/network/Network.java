package ru.tinkoff.telegram.mt.network;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import ru.tinkoff.mt.processor.InjectFromConfig;
import ru.tinkoff.telegram.mt.BuildConfig;
import ru.tinkoff.telegram.mt.Glue;
import ru.tinkoff.telegram.mt.network.params.BaseParams;
import ru.tinkoff.telegram.mt.network.parse.DefaultParsers;

/**
 * @author a.shishkin1
 */


@InjectFromConfig
public class Network {

    public static final String X_USER = "";
    private static final String URL_QA = "";
    private static final String URL_UAT = "";
    private static final String URL_X_REPLACE_BEFORE = "";
    private static final String URL_X_REPLACE_AFTER = "";

    private static final String URL_PROD = "https://api.tcsbank.ru/v1/";
    public static final String BASE_URL = BuildConfig.DEBUG ? URL_QA : URL_PROD;
    public static final String BASE_URL_X = BASE_URL.replace(URL_X_REPLACE_BEFORE, URL_X_REPLACE_AFTER);

    public static boolean isProdApi() { return BASE_URL.equals(URL_PROD); }

    private static InputStream openConnection(IRequest request) throws NetworkException {
        HttpsURLConnection connection = null;
        Exception ex = null;
        try {

            URL url = new URL(request.getUrl() + "?" + urlEncodeUTF8(request.getParams()));
            if(Glue.DEBUG)
                Log.i("Network.request", url.toString());
            connection = (HttpsURLConnection)url.openConnection();
            connection.setConnectTimeout(37 * 1000);
            int code = connection.getResponseCode();
            if(code >= 200 && code < 299) {
                return connection.getInputStream();
            } else {
                throw new NetworkException(code, DefaultParsers.TO_STRING_PARSER.parse(connection.getErrorStream()));
            }
        } catch (MalformedURLException e) {
            ex = e;
        } catch (IOException e) {
            ex = e;
        }
        if(Glue.DEBUG)
            Log.e("Network","connection failed");
        NetworkException forThrow = new NetworkException(0, null);
        forThrow.initCause(ex);
        throw forThrow;
    }

    public static  <T> T execute(IRequest req, IParser<T> parser) throws NetworkException {
        InputStream is = openConnection(req);
        T res = parser.parse(is);
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public interface IParser<T> {
        T parse(InputStream stream);
    }

    public interface IRequest extends Parcelable {
        String getUrl();
        HashMap<String, String> getParams();
    }

    public static class RequestParams extends HashMap<String, String> implements Parcelable{

        public RequestParams() {
        }

        protected RequestParams(Parcel in) {
            int size = in.readInt();
            for(int i = 0; i < size; i++) {
                add(in.readString(), in.readString());
            }
        }

        public static final Creator<RequestParams> CREATOR = new Creator<RequestParams>() {
            @Override
            public RequestParams createFromParcel(Parcel in) {
                return new RequestParams(in);
            }

            @Override
            public RequestParams[] newArray(int size) {
                return new RequestParams[size];
            }
        };

        public RequestParams add(String name, String value) {
            super.put(name, value);
            return this;
        }

        public RequestParams addAll(Map<String, String> params) {
            super.putAll(params);
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(size());
            for(Entry<String, String> e : entrySet()) {
                dest.writeString(e.getKey());
                dest.writeString(e.getValue());
            }
        }
    }

    public static class RequestImpl implements IRequest, Parcelable {

        private final RequestParams params;
        private final String endPoint;

        public final int what;
        private boolean isNeedSpecificDispatch;
        private boolean xApi;
        private boolean showDialog;
        private int argument;
//        private String association;
        private Bundle additions;
        private long delay;

        protected RequestImpl(Parcel in) {
            params = in.readParcelable(RequestParams.class.getClassLoader());
            endPoint = in.readString();
            what = in.readInt();
//            association = in.readString();
            argument = in.readInt();
            xApi = in.readInt() != 0;
            delay = in.readLong();
            additions = in.readBundle();
            isNeedSpecificDispatch = in.readInt() != 0;
        }

        public static final Creator<RequestImpl> CREATOR = new Creator<RequestImpl>() {
            @Override
            public RequestImpl createFromParcel(Parcel in) {
                return new RequestImpl(in);
            }

            @Override
            public RequestImpl[] newArray(int size) {
                return new RequestImpl[size];
            }
        };

        public String getEndPoint() {
            return endPoint;
        }

        @Override
        public String getUrl() {
            return (xApi ? BASE_URL_X : BASE_URL) + endPoint;
        }

        @Override
        public RequestParams getParams() {
            return params;
        }

        private RequestImpl(int what, String endPoint) {
            this.params = new RequestParams();
            this.what = what;
            this.endPoint = endPoint;
            this.xApi = false;
            this.delay = 0L;
            this.isNeedSpecificDispatch = false;
        }

//        public String getAssociation() {
//            return association;
//        }
//
//        public void setAssociation(String association) {
//            this.association = association;
//        }

        public void setArgument(int argument) {
            this.argument = argument;
        }

        public int getArgument() {
            return argument;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public long getDelay() {
            return delay;
        }

        public boolean isShowDialog() {
            return showDialog;
        }

        public void setShowDialog(boolean showDialog) {
            this.showDialog = showDialog;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(params, flags);
            dest.writeString(endPoint);
            dest.writeInt(what);
//            dest.writeString(association);
            dest.writeInt(argument);
            dest.writeInt(xApi ? 1 : 0);
            dest.writeLong(delay);
            dest.writeBundle(additions);
            dest.writeInt(isNeedSpecificDispatch ? 1 : 0);
        }

        public boolean isNeedSpecificDispatch() {
            return isNeedSpecificDispatch;
        }

        public void setIsNeedSpecificDispatch(boolean isNeedSpecificDispatch) {
            this.isNeedSpecificDispatch = isNeedSpecificDispatch;
        }

        public Bundle getAdditions() {
            return additions;
        }

        public void setAdditions(Bundle additions) {
            this.additions = additions;
        }

        public void setXApi(boolean xApi) {
            this.xApi = xApi;
        }
    }

    public static class RequestBuilder {

        private RequestImpl result;

        RequestBuilder(int what, String endPoint) {
            this.result = new RequestImpl(what, endPoint);

        }

        public RequestBuilder addParameter(String name, String value) {
            result.params.add(name, value);
            return this;
        }

        public RequestImpl buildOn(Context context) {
            result.params.addAll(new BaseParams(context, false));
            return result;
        }

        public RequestImpl buildAnonymousOn(Context context) {
            result.params.addAll(new BaseParams(context, true));
            return result;
        }

        public RequestImpl build() {
            return result;
        }
    }

    public static class NetworkException extends Exception {

        private int code;
        private String message;


        public NetworkException(int code, String message) {
            super(message);
            this.code = code;
            this.message = message;
        }


        @Override
        public String getMessage() {
            return message;
        }

        public int getCode() {
            return code;
        }
    }


    private static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static String urlEncodeUTF8(Map<?,?> map) {
        if(map == null || map.size() == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?,?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if(key == null || value == null)
                continue;
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(key.toString()),
                    urlEncodeUTF8(value.toString())
            ));
        }
        return sb.toString();
    }



}
