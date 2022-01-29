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

package sorcer.service.modeling;

import sorcer.service.Fidelity;
import sorcer.service.Identifiable;
import sorcer.service.Projection;

/**
 * @author Mike Sobolewski 03/11/2021
 */
public class ProjectionMultiFi extends Fidelity<Identifiable> {

    public ProjectionMultiFi(Projection... projections) {
        setSelects(projections);
        select = projections[0];
    }

    public ProjectionMultiFi(String name, Projection... projections) {
        this.fiName = name;
        setSelects(projections);
        select = projections[0];
    }
}
