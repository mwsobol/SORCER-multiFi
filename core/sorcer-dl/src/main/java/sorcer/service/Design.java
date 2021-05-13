/*
 * Copyright 2021 the original author or authors.
 * Copyright 2021 SorcerSoft.org.
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
 * An top-level common interface for all design service in SORCER.
 * Design services are frontend services created by multidisciplinary
 * designers. Standalone design services that are aggregated
 * multidisciplinary services are called transdisciplines.
 *
 * @author Mike Sobolewski, 05/12/2021
 */
public interface Design extends Development, Identifiable {
        public Fidelity<Development> getDeveloperFi();
    }
