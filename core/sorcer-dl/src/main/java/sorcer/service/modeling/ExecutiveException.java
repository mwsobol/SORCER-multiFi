/*
 * Copyright 2019 the original author or authors.
 * Copyright 2019 SorcerSoft.org.
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

import sorcer.service.Context;

@SuppressWarnings("rawtypes")
public class ExecutiveException extends Exception {

    static final long serialVersionUID = 1L;

    private Context context;

    public ExecutiveException() {
    }

    public ExecutiveException(String msg) {
        super(msg);
    }

    public ExecutiveException(Context context) {
        this.context = context;
    }

    public ExecutiveException(String msg, Context context) {
        super(msg);
        this.context = context;
    }

    public ExecutiveException(Exception exception) {
        super(exception);
    }

    public ExecutiveException(String msg, Exception e) {
        super(msg, e);

    }

    public ExecutiveException(String msg, Context context, Exception e) {
        super(msg, e);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
