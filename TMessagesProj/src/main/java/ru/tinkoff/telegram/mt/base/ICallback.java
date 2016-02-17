package ru.tinkoff.telegram.mt.base;

/**
 * @author a.shishkin1
 */


public interface ICallback<Result> {

    void onResult(Result result);
}
