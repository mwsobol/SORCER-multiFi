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

import sorcer.core.provider.exerter.ProviderCache;
import sorcer.service.Accessor;
import sorcer.service.Exerter;
import sorcer.service.Signature;
import sorcer.service.SignatureException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a ProviderCache.
 */
public class ProviderProxyCache implements ProviderCache  {
    private static final ConcurrentHashMap<Signature, Object> proxyCache = new ConcurrentHashMap<>();

    @Override
    public Object getProvider(Signature signature) {
        Object provider = doProviderGet(signature);
        if (provider != null) {
            if (!Accessor.isAlive(provider)) {
                /* If we had a provider and now it's no longer there, retry */
                proxyCache.remove(signature);
                provider = doProviderGet(signature);
            }
        }
        return provider;
    }

    private Object doProviderGet(Signature signature) {
        try {
            return proxyCache.getOrDefault(signature, Accessor.get().getService(signature));
        } catch (SignatureException e) {
            return null;
        }
    }
}
