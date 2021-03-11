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

import sorcer.core.SorcerConstants;
import sorcer.core.provider.exerter.cache.ProviderProxyCache;
import sorcer.service.Signature;
import sorcer.util.Sorcer;

/**
 * Defines getting a provider proxy.
 */
public interface ProviderCache {
    /**
     * Get a provider defined by it's signature. If the provider does not exist in this cache, first discover it.
     * If it has been discovered cache it, and return the cached proxy testing for live-ness.
     *
     * @param signature The signature.
     *
     * @return The provider's proxy, or null if not found.
     */
    Object getProvider(Signature signature);

    /**
     * Get an instance of a ProviderCache using the "provider.cache" property, optionally set in the "sorcer.env" file.
     * If the property is not set, the ProviderCache defaults to the
     * sorcer.core.provider.exerter.cache.ProviderProxyCache class.
     *
     * @return An instance of ProviderCache.
     *
     * @throws RuntimeException if the ProviderCache cannot be created.
     */
    static ProviderCache get() {
        String providerCacheClassName = Sorcer.getProperties().getProperty(SorcerConstants.S_PROVIDER_CACHE_NAME,
                                                                           ProviderProxyCache.class.getName());
        try {
            Class<?> providerCacheClass = Class.forName(providerCacheClassName);
            return (ProviderCache)providerCacheClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + providerCacheClassName, e);
        }
    }
}
