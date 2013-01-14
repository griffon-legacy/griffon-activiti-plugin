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

package griffon.plugins.activiti

import org.activiti.engine.*
import org.activiti.engine.repository.*

import griffon.core.GriffonApplication
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.ConfigUtils

import griffon.plugins.datasource.DataSourceHolder
import griffon.plugins.datasource.DataSourceConnector

import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class ActivitiConnector {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(ActivitiConnector)

    ConfigObject createConfig(GriffonApplication app) {
        if (!app.config.pluginConfig.activiti) {
            app.config.pluginConfig.activiti = ConfigUtils.loadConfigWithI18n('ActivitiConfig')
        }
        app.config.pluginConfig.activiti
    }

    private ConfigObject narrowConfig(ConfigObject config, String engineName) {
        return engineName == DEFAULT ? config.processEngine : config.processEngines[engineName]
    }

    ProcessEngine connect(GriffonApplication app, ConfigObject config, String engineName = DEFAULT) {
        if (ProcessEngineHolder.instance.isProcessEngineConnected(engineName)) {
            return ProcessEngineHolder.instance.getProcessEngine(engineName)
        }

        config = narrowConfig(config, engineName)
        app.event('ActivitiConnectStart', [config, engineName])
        ProcessEngine engine = startActiviti(app, config, engineName)
        ProcessEngineHolder.instance.setProcessEngine(engineName, engine)
        app.event('ActivitiConnectEnd', [engineName, engine])
        engine
    }

    void disconnect(GriffonApplication app, ConfigObject config, String engineName = DEFAULT) {
        if (ProcessEngineHolder.instance.isProcessEngineConnected(engineName)) {
            config = narrowConfig(config, engineName)
            ProcessEngine engine = ProcessEngineHolder.instance.getProcessEngine(engineName)
            app.event('ActivitiDisconnectStart', [config, engineName, engine])
            stopActiviti(app, config, engine, engineName)
            app.event('ActivitiDisconnectEnd', [config, engineName])
            ProcessEngineHolder.instance.disconnectProcessEngine(engineName)
        }
    }

    private ProcessEngine startActiviti(GriffonApplication app, ConfigObject config, String engineName) {
        ProcessEngineConfiguration engineConfiguration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault()
        config.each { key, value ->
            if (key == 'dataSource' && value instanceof CharSequence) {
                String dataSourceName = value.toString()
                value = DataSourceHolder.instance.fetchDataSource(dataSourceName)
            }
            engineConfiguration[key] = value
        }
        engineConfiguration.processEngineName = engineName
        ProcessEngine engine = engineConfiguration.buildProcessEngine()
        ProcessEngines.registerProcessEngine(engine)

        DeploymentBuilder deploymentBuilder = engine.repositoryService
            .createDeployment()
            .name(engineName)

        def bpmlResolver = new PathMatchingResourcePatternResolver(app.class.classLoader)
        ['*.bpmn*.xml', '*.form', '*.png'].each { suffix ->
            for (resource in bpmlResolver.getResources('classpath*:/activiti/'+ engineName +'/**/'+ suffix)) {
                if (LOG.traceEnabled) LOG.trace("Deploying $resource")
                deploymentBuilder.addInputStream(resource.getURL().toString(), resource.inputStream)
            }
        }
        deploymentBuilder.deploy()

        engine
    }

    private void stopActiviti(GriffonApplication app, ConfigObject config, ProcessEngine engine, String engineName) {
        engine.close()
        config.each { key, value ->
            if (key == 'dataSource' && value instanceof CharSequence) {
                String dataSourceName = value.toString()
                if (DataSourceHolder.instance.isDataSourceConnected(dataSourceName)) {
                    ConfigObject dsconfig = DataSourceConnector.instance.createConfig(app)
                    DataSourceConnector.instance.disconnect(app, dsconfig, dataSourceName)
                }
            }
        }
        ProcessEngines.unregister(engine)
        engine.repositoryService.deleteDeployment(engineName)
    }
}