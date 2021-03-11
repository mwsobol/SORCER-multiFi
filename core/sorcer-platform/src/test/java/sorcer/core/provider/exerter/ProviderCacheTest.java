/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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
package sorcer.core.provider.exerter;

import net.jini.core.entry.Entry;
import net.jini.lookup.entry.Name;
import org.junit.Test;
import sorcer.core.SorcerConstants;
import sorcer.core.provider.exerter.cache.ProviderProxyCache;
import sorcer.core.provider.exerter.cache.ProviderProxyCacheGuava;
import sorcer.util.Sorcer;

import static org.junit.Assert.*;

public class ProviderCacheTest {

    @Test
    public void testGetJavaCacheUsingProperty() {
        Sorcer.getProperties().setProperty(SorcerConstants.S_PROVIDER_CACHE_NAME,
                                           ProviderProxyCache.class.getName());
        ProviderCache providerCache = ProviderCache.get();
        assertTrue(providerCache instanceof ProviderProxyCache);
    }

    @Test
    public void testGetDefaultCache() {
        Sorcer.getProperties().remove(SorcerConstants.S_PROVIDER_CACHE_NAME);
        assertNull(Sorcer.getProperties().getProperty(SorcerConstants.S_PROVIDER_CACHE_NAME));
        ProviderCache providerCache = ProviderCache.get();
        assertTrue(providerCache instanceof ProviderProxyCache);
    }

    @Test
    public void testGetGuavaCache() {
        Sorcer.getProperties().setProperty(SorcerConstants.S_PROVIDER_CACHE_NAME,
                                           ProviderProxyCacheGuava.class.getName());
        ProviderCache providerCache = ProviderCache.get();
        assertTrue(providerCache instanceof ProviderProxyCacheGuava);
    }

    @Test(expected = RuntimeException.class)
    public void testGetBogusCache() {
        Sorcer.getProperties().setProperty(SorcerConstants.S_PROVIDER_CACHE_NAME, "org.phony.Cache");
        ProviderCache.get();
    }

    @Test(expected = RuntimeException.class)
    public void testGetBogusCacheNotInstance() {
        Sorcer.getProperties().setProperty(SorcerConstants.S_PROVIDER_CACHE_NAME, Object.class.getName());
        ProviderCache.get();
    }

}