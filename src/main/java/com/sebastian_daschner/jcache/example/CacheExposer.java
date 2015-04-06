/*
 * Copyright (C) 2015 Sebastian Daschner, sebastian-daschner.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastian_daschner.jcache.example;

import org.mapdb.BTreeMap;
import org.mapdb.DB;

import javax.annotation.Resource;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CacheExposer {

    private static final String CACHE_NAME = "default";

    @Resource
    ManagedExecutorService mes;

    @Inject
    DB mapDB;

    @Produces
    @ApplicationScoped
    public Cache<String, String> getCache() {
        final CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        final Configuration<String, String> config = new MutableConfiguration<String, String>().setTypes(String.class, String.class)
                .setWriteThrough(true)
                .setReadThrough(true)
                .setCacheWriterFactory(() -> new CacheWriter<String, String>() {

                    private BTreeMap<Object, Object> map = mapDB.getTreeMap(StorageExposer.MAP_DB_NAME);

                    @Override
                    public void write(final Cache.Entry<? extends String, ? extends String> entry) throws CacheWriterException {
                        map.put(entry.getKey(), entry.getValue());
                        mapDB.commit();
                    }

                    @Override
                    public void writeAll(final Collection<Cache.Entry<? extends String, ? extends String>> entries) throws CacheWriterException {
                        entries.forEach(e -> map.put(e.getKey(), e.getValue()));
                        mapDB.commit();
                    }

                    @Override
                    public void delete(final Object key) throws CacheWriterException {
                        map.remove(key);
                        mapDB.commit();
                    }

                    @Override
                    public void deleteAll(final Collection<?> keys) throws CacheWriterException {
                        keys.forEach(map::remove);
                        mapDB.commit();
                    }
                })
                .setCacheLoaderFactory(() -> new CacheLoader<String, String>() {

                    private BTreeMap<Object, Object> map = mapDB.getTreeMap(StorageExposer.MAP_DB_NAME);

                    @Override
                    public String load(final String key) throws CacheLoaderException {
                        return (String) map.get(key);
                    }

                    @Override
                    public Map<String, String> loadAll(final Iterable<? extends String> keys) throws CacheLoaderException {
                        final HashMap<String, String> newMap = new HashMap<>();
                        map.entrySet().forEach(e -> newMap.put((String) e.getKey(), (String) e.getValue()));
                        return newMap;
                    }
                });

        return cacheManager.createCache(CACHE_NAME, config);
    }

}
