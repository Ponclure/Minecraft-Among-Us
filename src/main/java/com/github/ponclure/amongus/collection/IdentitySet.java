package com.github.ponclure.amongus.collection;

import java.util.Set;

public interface IdentitySet<T> extends Set<T> {

    T get(T o);

}