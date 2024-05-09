package hu.laba;

import java.util.function.Consumer;

@FunctionalInterface
public interface ResponseValidatorFunction extends Consumer<RequestResponseContext> {
}
