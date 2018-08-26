package com.stackifier.interfaces;

/**
 * Created by Talal Ahmed on 25/08/2018
 */
public interface Deserializer {
    Throwable deserialize(final String stacktrace);
}
