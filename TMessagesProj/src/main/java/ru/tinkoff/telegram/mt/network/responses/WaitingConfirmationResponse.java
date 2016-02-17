package ru.tinkoff.telegram.mt.network.responses;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author a.shishkin1
 */


public class WaitingConfirmationResponse implements IJsonable, Parcelable {


    public static final String CONFIRMATION_EXTRA = "confirmation_extra";

    private int action; // same as initialOperation
//    private String association;
    private Bundle additions;

    private String operationTicket;
    private ConfirmationType confirmationType;
    private int confirmCodeLength;
    private String initialOperation;

    private String url;
    private String merchantData;
    private String requestSecretCode;

    public WaitingConfirmationResponse(int action, Bundle additions) {
        this.action = action;
        this.additions = additions;
    }

    @Override
    public void fillByJson(JSONObject jobj) {
        operationTicket = jobj.optString("operationTicket");
        JSONObject jconfirmationData = jobj.optJSONObject("confirmationData");
        if(jconfirmationData != null) {
            JSONArray names = jconfirmationData.names();
            if(names != null) {
                String confirmationName = names.optString(0);
                jconfirmationData = jconfirmationData.optJSONObject(confirmationName);
                if(jconfirmationData != null) {
                    confirmCodeLength = jconfirmationData.optInt("codeLength");
                    url = jconfirmationData.optString("url");
                    merchantData = jconfirmationData.optString("merchantData");
                    requestSecretCode = jconfirmationData.optString("requestSecretCode");
                }
                confirmationType = ConfirmationType.TYPE_BY_NAME.get(confirmationName);
            }

        }
        initialOperation = jobj.optString("initialOperation");
        Log.i("WCR", "" + this);
    }

    @Override
    public JSONObject createJson() {
        throw new UnsupportedOperationException();
    }


    public String getOperationTicket() {
        return operationTicket;
    }

    public ConfirmationType getConfirmationType() {
        return confirmationType;
    }

    public int getConfirmCodeLength() {
        return confirmCodeLength;
    }

    public String getInitialOperation() {
        return initialOperation;
    }

    public String getUrl() {
        return url;
    }

    public String getMerchantData() {
        return merchantData;
    }

    public String getRequestSecretCode() {
        return requestSecretCode;
    }

    public Bundle getAdditions() {
        return additions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[operationTicket = ").append(operationTicket)
                .append(", confirmationType = ").append(confirmationType)
                .append(", confirmationCodeLength = ").append(confirmCodeLength)
                .append(", initialOperation = ").append(initialOperation)
                .append(", url = ").append(url)
                .append(", merchantData = ").append(merchantData)
                .append(", requestSecretValue = ").append(requestSecretCode)
                .append("]");
        return sb.toString();
    }

    public int getAction() {
        return action;
    }

    protected void fillFromParcel(Parcel in) {
        action = in.readInt();
        operationTicket = in.readString();
        confirmCodeLength = in.readInt();
        initialOperation = in.readString();
        confirmationType = ConfirmationType.TYPE_BY_NAME.get(in.readString());
        url = in.readString();
        merchantData = in.readString();
        requestSecretCode = in.readString();
        additions = in.readBundle();
    }

    public static final Creator<WaitingConfirmationResponse> CREATOR = new Creator<WaitingConfirmationResponse>() {
        @Override
        public WaitingConfirmationResponse createFromParcel(Parcel in) {
            WaitingConfirmationResponse result = new WaitingConfirmationResponse(0, null);
            result.fillFromParcel(in);
            return result;
        }

        @Override
        public WaitingConfirmationResponse[] newArray(int size) {
            return new WaitingConfirmationResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(action);
        dest.writeString(operationTicket);
        dest.writeInt(confirmCodeLength);
        dest.writeString(initialOperation);
        dest.writeString(confirmationType.getName());
        dest.writeString(url);
        dest.writeString(merchantData);
        dest.writeString(requestSecretCode);
        dest.writeBundle(additions);

    }
}
