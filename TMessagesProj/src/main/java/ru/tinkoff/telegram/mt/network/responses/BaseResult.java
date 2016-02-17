package ru.tinkoff.telegram.mt.network.responses;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONObject;

import ru.tinkoff.telegram.mt.R;

/**
 * @author a.shishkin1
 */


public class BaseResult implements IJsonable {

    private ResultCode resultCode;
    private JSONObject source;
    private int what;
    private Bundle additions;
    private int argument;
    private boolean isSpecificDispatch;

    public boolean isSpecificDispatch() {
        return isSpecificDispatch;
    }

    public void setSpecificDispatch(boolean isSpecificDispatch) {
        this.isSpecificDispatch = isSpecificDispatch;
    }

    //    public void setAssociation(String association) {
//        this.association = association;
//    }
//
//    public String getAssociation() {
//        return association;
//    }


    public Bundle getAdditions() {
        return additions;
    }

    public void setAdditions(Bundle additions) {
        this.additions = additions;
    }

    public void setArgument(int argument) {
        this.argument = argument;
    }

    public int getArgument() {
        return argument;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public int getWhat() {
        return what;
    }

    public JSONObject getSource() {
        return source;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public void fillByJson(JSONObject jobj) {
        source = jobj;
        String codeName = jobj.optString("resultCode");
        resultCode = ResultCode.CODES_BY_NAME.get(codeName);
        if(resultCode == null) {
            throw new IllegalArgumentException("unknown ResultCode for name " + codeName);
        }

    }

    public Error asError() {
        if(resultCode == ResultCode.OK) {
            throw new IllegalStateException("ResultCode.OK is not error");
        }
        Error result = new Error(resultCode.name());
        result.fillByJson(source);
        return result;
    }

    public Error asError(Context context) {
        if(resultCode == ResultCode.OK) {
            throw new IllegalStateException("ResultCode.OK is not error");
        }
        Error result = new Error(context);
        result.fillByJson(source);
        return result;
    }

    @Override
    public JSONObject createJson() {
        throw new UnsupportedOperationException();
    }

    public static class Error implements IJsonable {
        public String trackingId;
        public String title;
        public String detail;

        public Error(String title) {
            this.title = title;
        }

        public Error(Context context) {
            this.title = context.getString(R.string.mt_error);
        }

        @Override
        public void fillByJson(JSONObject jobj) {
            detail = jobj.optString("plainMessage");
            trackingId = jobj.optString("trackingId");
        }

        @Override
        public JSONObject createJson() {
            throw new UnsupportedOperationException();
        }
    }



}
