package ru.tinkoff.telegram.mt.network;

/**
 * @author a.shishkin1
 */


public interface INetworkCallback extends IApiCallback {


    void onException(Exception exception);

    void onNetworkDialogRequested();
    void onNetworkDialogDismissRequested();

}
