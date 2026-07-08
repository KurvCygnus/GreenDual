package com.wenhua.tcpservice.utils;

import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class FunctionalUtils
{
    private FunctionalUtils() { throw new IllegalAccessError("Class \"FunctionalUtils\" is not meant to be instantized!"); }
    
    public static <K, V> @NonNull Map<K, V> buildImmutableMap(@NonNull Consumer<? super Map<K, V>> consumer)
    {
        final var map = new HashMap<K, V>();
        consumer.accept(map);
        return Collections.unmodifiableMap(map);
    }
}
