package me.hogejo.highload.stress;

import java.util.function.Consumer;

@FunctionalInterface
public interface ResponseValidatorFunction extends Consumer<RequestResponseContext> {
}
