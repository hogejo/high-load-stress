package hu.laba;

import okhttp3.Response;

import java.util.function.Function;

@FunctionalInterface
public interface ResponseValidatorFunction extends Function<Response, Boolean> {
}
