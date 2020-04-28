/*
 * Copyright (c) 2020 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Context for holding the output returned by the {@link Collector}
 * implementations.
 */
public class CollectorContext {

    // Using a namespace string as key in ThreadLocal so that it is unique in
    // ThreadLocal.
    static final String COLLECTOR_CONTEXT_THREAD_LOCAL_KEY = "com.networknt.schema.CollectorKey";

    // Get an instance from thread info (which uses ThreadLocal).
    public static CollectorContext getInstance() {
        return (CollectorContext) ThreadInfo.get(COLLECTOR_CONTEXT_THREAD_LOCAL_KEY);
    }

    /**
     * Map for holding the name and {@link Collector} or a simple Object.
     */
    private Map<String, Object> collectorMap = new HashMap<String, Object>();

    /**
     * Map for holding the name and {@link Collector} class collect method output.
     */
    private Map<String, Object> collectorLoadMap = new HashMap<String, Object>();

    /**
     * Adds a collector with give name. Preserving this method for backward
     * compatibility.
     *
     * @param <E> element
     * @param name String
     * @param collector Collector
     */
    public <E> void add(String name, Collector<E> collector) {
        collectorMap.put(name, collector);
    }

    /**
     * Adds a collector or a simple object with give name.
     *
     * @param <E> element
     * @param object Object
     * @param name String
     *
     */
    public <E> void add(String name, Object object) {
        collectorMap.put(name, object);
    }

    /**
     * Gets the data associated with a given name. Please note if you are using a
     * Collector you should wait till the validation is complete to gather all data.
     * <p>
     * For a Collector, this method will return the collector as long as load method
     * is not called. Once the load method is called this method will return the
     * actual data collected by collector.
     *
     * @param name String
     * @return Object
     */
    public Object get(String name) {
        Object object = collectorMap.get(name);
        if (object instanceof Collector<?> && (collectorLoadMap.get(name) != null)) {
            return collectorLoadMap.get(name);
        }
        return collectorMap.get(name);
    }

    /**
     * Combines data with Collector identified by the given name.
     *
     * @param name String
     * @param data Object
     */
    public void combineWithCollector(String name, Object data) {
        Object object = collectorMap.get(name);
        if (object instanceof Collector<?>) {
            Collector<?> collector = (Collector<?>) object;
            collector.combine(data);
        }
    }

    /**
     * Reset the context
     */
    void reset() {
        this.collectorMap = new HashMap<String, Object>();
        this.collectorLoadMap = new HashMap<String, Object>();
    }

    /**
     * Loads data from all collectors.
     */
    void loadCollectors() {
        Set<Entry<String, Object>> entrySet = collectorMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            if (entry.getValue() instanceof Collector<?>) {
                Collector<?> collector = (Collector<?>) entry.getValue();
                collectorLoadMap.put(entry.getKey(), collector.collect());
            }
        }

    }

}
