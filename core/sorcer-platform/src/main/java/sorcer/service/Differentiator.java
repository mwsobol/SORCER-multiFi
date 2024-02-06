/*
 * Copyright 2017 the original author or authors.
 * Copyright 2017 SorcerSoft.org.
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

import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Model;
import sorcer.service.modeling.Wrt;
import sorcer.util.DataTable;

public interface Differentiator extends Controlling {

	public Functionality.Type getType();

	public void setWrt(Wrt wrt);

	public DataTable differentiate(Arg... args) throws EvaluationException;

	public double[] getGradient(String gradientName) throws EvaluationException;

    public DataTable getGradientTable() throws EvaluationException;

}
