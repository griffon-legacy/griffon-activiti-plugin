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
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractActivitiProvider implements ActivitiProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractActivitiProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withActiviti(Closure<R> closure) {
        return withActiviti(DEFAULT, closure);
    }

    public <R> R withActiviti(String processEngineName, Closure<R> closure) {
        if (isBlank(processEngineName)) processEngineName = DEFAULT;
        if (closure != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on processEngine '" + processEngineName + "'");
            }
            return closure.call(processEngineName, getProcessEngine(processEngineName));
        }
        return null;
    }

    public <R> R withActiviti(CallableWithArgs<R> callable) {
        return withActiviti(DEFAULT, callable);
    }

    public <R> R withActiviti(String processEngineName, CallableWithArgs<R> callable) {
        if (isBlank(processEngineName)) processEngineName = DEFAULT;
        if (callable != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on processEngine '" + processEngineName + "'");
            }
            callable.setArgs(new Object[]{processEngineName, getProcessEngine(processEngineName)});
            return callable.call();
        }
        return null;
    }

    protected abstract ProcessEngine getProcessEngine(String processEngineName);
}