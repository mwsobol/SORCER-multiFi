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
package sorcer.core.provider.exerter.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.provider.ServiceName;
import sorcer.core.provider.exerter.ProviderCache;
import sorcer.core.provider.exerter.ServiceShell;
import sorcer.service.*;
import sorcer.util.ProviderLocator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a ProviderCache using Guava's LocalCache.
 */
public class ProviderProxyCacheGuava implements ProviderCache {
    private static final LoadingCache<Signature, Object> proxyCache = setupProxyCache();
    protected final static Logger logger = LoggerFactory.getLogger(ProviderProxyCacheGuava.class);

    @Override
    public Object getProvider(Signature signature) {
        Object provider = null;
        try {
            provider = proxyCache.get(signature);
            // check if cached proxy is still alive
            ((Exerter)provider).getProviderName();
        } catch (Exception e) {
            proxyCache.refresh(signature);
            try {
                provider = proxyCache.get(signature);
            } catch (ExecutionException executionException) {
                logger.warn("Problem with proxyCache.get", executionException);
            }
        }
        return provider;
    }

    private static LoadingCache<Signature, Object> setupProxyCache() {
        return CacheBuilder.newBuilder()
                           .maximumSize(20)
                           .expireAfterWrite(30, TimeUnit.MINUTES)
                           .build(new CacheLoader<Signature, Object>() {
                               public Object load(Signature signature) throws SignatureException {
                                   if (signature.getProviderName() instanceof ServiceName) {
                                       try {
                                           return ProviderLocator.getProvider(signature);
                                       } catch (SignatureException e) {
                                           e.printStackTrace();
                                       }
                                       logger.warn("No available proxy for {}", signature);
                                       return Context.none;
                                   } else
                                       return Accessor.get().getService(signature);
                               }
                           });
    }

}
