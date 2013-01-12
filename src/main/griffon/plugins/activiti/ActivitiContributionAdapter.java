/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.activiti;

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;

/**
 * @author Andres Almiray
 */
public class ActivitiContributionAdapter implements ActivitiContributionHandler {
    private static final String DEFAULT = "default";

    private ActivitiProvider provider = DefaultActivitiProvider.getInstance();

    public void setActivitiProvider(ActivitiProvider provider) {
        this.provider = provider != null ? provider : DefaultActivitiProvider.getInstance();
    }

    public ActivitiProvider getActivitiProvider() {
        return provider;
    }

    public <R> R withActiviti(Closure<R> closure) {
        return withActiviti(DEFAULT, closure);
    }

    public <R> R withActiviti(String processEngineName, Closure<R> closure) {
        return provider.withActiviti(processEngineName, closure);
    }

    public <R> R withActiviti(CallableWithArgs<R> callable) {
        return withActiviti(DEFAULT, callable);
    }

    public <R> R withActiviti(String processEngineName, CallableWithArgs<R> callable) {
        return provider.withActiviti(processEngineName, callable);
    }
}