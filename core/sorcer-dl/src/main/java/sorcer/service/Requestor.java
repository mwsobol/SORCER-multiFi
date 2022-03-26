/*
 * Copyright 2016 the original author or authors.
 * Copyright 2016 SorcerSoft.org.
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

package sorcer.service;

/**
 * An top-level common interface for all request service (requestors) in SORCER.
 * Request services are frontend services created by users. Services
 * associated directly with executable codes are called operation services that
 * used by elementary request services that in turn are aggregated into various
 * types of combined services like domains and disciplines.
 *
 * @author Mike Sobolewski
 */
public interface Requestor extends Service, Identifiable {

    public void setName(String name);

    /**
     * Returns service multi-fidelities of this request.
     */
    public Fi getMultiFi();

    /**
     * Returns a morpher updating at runtime multi-fidelities of this request.
     */
    public Morpher getMorpher();

}
