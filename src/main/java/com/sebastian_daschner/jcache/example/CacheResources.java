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

import javax.cache.Cache;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("test/{key}")
@Produces(MediaType.TEXT_PLAIN)
@Stateless
public class CacheResources {

    @Inject
    Cache<String, String> cache;

    @PathParam("key")
    private String key;

    @GET
    public String get() {
        return cache.get(key);
    }

    @PUT
    public void store(final String value) {
        cache.put(key, value);
    }

}
