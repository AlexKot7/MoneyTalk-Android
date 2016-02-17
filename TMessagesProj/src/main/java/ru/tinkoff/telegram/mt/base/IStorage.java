package ru.tinkoff.telegram.mt.base;

/**
 * @author a.shishkin1
 */


public interface IStorage<T> {

    void store(T t);
    T restore();

}
