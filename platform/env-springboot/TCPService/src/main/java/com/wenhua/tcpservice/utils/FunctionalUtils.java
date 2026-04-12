package com.wenhua.tcpservice.utils;

import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class FunctionalUtils
{
    private FunctionalUtils() { throw new IllegalAccessError(); }
    
    public static <K, V> @NonNull Map<K, V> buildImmutableMap(@NonNull Consumer<Map<K, V>> consumer)
    {
        final HashMap<K, V> map = new HashMap<>();
        consumer.accept(map);
        return Collections.unmodifiableMap(map);
    }
}
