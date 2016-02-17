package ru.tinkoff.telegram.mt.base;

/**
 * @author a.shishkin1
 */


public interface IConverter<S, D> {

    D convert(S source);

}
