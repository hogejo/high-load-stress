package hu.laba;

import okhttp3.Request;

import java.util.function.Function;

@FunctionalInterface
public interface RequestBuilderFunction extends Function<Integer, Request> {
}
