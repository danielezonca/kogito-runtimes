/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.codegen.api.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class that represents Kogito Symbol Table: each Generator can register its types to be used to perform validation by others
 */
public final class KogitoSymbolTable {

    protected final Map<String, Map<KogitoSymbolId, Object>> content = new HashMap<>();

    public KogitoSymbolTable() {

    }

    public boolean hasResourceType(String resourceType) {
        return content.containsKey(resourceType);
    }

    public Optional<Map<KogitoSymbolId, Object>> getSymbolsFromType(String resourceType) {
        return Optional.ofNullable(content.get(resourceType))
                .map(Collections::unmodifiableMap);
    }

    public Object putSymbol(String resourceType, KogitoSymbolId key, Object value) {
        return content.computeIfAbsent(resourceType, t -> new HashMap<>())
                .put(key, value);
    }

    public <T> Optional<T> getSymbol(String resourceType, KogitoSymbolId symbolName, Class<T> asClass) {
        return getSymbolsFromType(resourceType)
                .flatMap(symbols -> Optional.ofNullable(symbols.get(symbolName)))
                .map(symbol -> castIfPossible(symbolName, symbol, asClass));
    }

    private <T> T castIfPossible(KogitoSymbolId symbolName, Object symbol, Class<T> asClass){
        if (asClass.isAssignableFrom(symbol.getClass())) {
            return asClass.cast(symbol);
        }
        throw new IllegalArgumentException("Impossible to cast '" + symbolName + "' key value as " + asClass.getName() + ", found " + symbol.getClass().getCanonicalName());
    }
}
