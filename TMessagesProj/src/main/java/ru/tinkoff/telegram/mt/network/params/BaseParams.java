package ru.tinkoff.telegram.mt.network.params;

import android.content.Context;

import ru.tinkoff.telegram.mt.BuildConfig;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.network.Network;
import ru.tinkoff.telegram.mt.utils.DeviceUtils;

/**
 * @author a.shishkin1
 */


public class BaseParams extends Network.RequestParams {

    protected BaseParams(MtAppPart appPart, boolean anonymous) {
        super();
        if (! anonymous) {
            add("sessionid", appPart.getSessionId());
            add("sessionId", appPart.getSessionId());
        }
        add("platform", "android");
        add("origin", "mtalk,telegram");
        add("deviceId", DeviceUtils.getDeviceId(appPart));
        add("appVersion", BuildConfig.VERSION_NAME);
    }


    public BaseParams(Context context, boolean anonymous) {
        this(MtAppPart.getMtAppInstance(context), anonymous);
    }
}
