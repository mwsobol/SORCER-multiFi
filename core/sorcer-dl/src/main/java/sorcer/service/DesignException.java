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
 *
 * @author Mike Sobolewski
 */

package sorcer.service;

public class DesignException extends Exception {

	private static final long serialVersionUID = 1L;

	private ProviderInfo providerInfo;

	public DesignException() {
		super();
	}

	public DesignException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a new DesignException with an embedded exception.
	 *
	 * @param cause
	 *            embedded exception
	 */
	public DesignException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this exception's detail message.
	 */
	public DesignException(String message, Throwable cause) {
		super(message, cause);
	}

	public DesignException(String message, Throwable cause, ProviderInfo providerInfo) {
		super(message, cause);
		this.providerInfo = providerInfo;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName()).append(": ");
		sb.append(getLocalizedMessage());
		if (providerInfo != null) {
			sb.append("\n").append(providerInfo);
		}
		return sb.toString();
	}
}
