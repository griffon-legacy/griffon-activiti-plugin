/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class ActivitiGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.0.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [datasource: '0.3']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-activiti-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'BPM support via Activiti'
    String description = '''
The Activiti plugin enables BPM workflows on Griffon applications.

[Activiti][1] is a light-weight workflow and Business Process Management (BPM) Platform targeted at business people,
developers and system admins. Its core is a super-fast and rock-solid BPMN 2 process engine for Java. It's
open-source and distributed under the Apache license.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * ActivitiConfig.groovy - contains ProcessEngine definitions.

A new dynamic method named `withActiviti` will be injected into all controllers,
giving you access to a `org.activiti.engine.ProcessEngine` object, with which you'll be able
to make calls configured process engines. Remember to make all these calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.
This method is aware of multiple engines. If no engineName is specified when calling
it then the default process engine will be selected. Here are two example usages, the first
queries against the default engine while the second queries an engine whose name has
been configured as 'internal'

    package sample
    class SampleController {
        def queryAllActivitiEngines = {
            withActiviti { engineName, processEngine -> ... }
            withActiviti('internal') { engineName, processEngine -> ... }
        }
    }
    
This method is also accessible to any component through the singleton `griffon.plugins.activiti.ActivitiConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`ActivitiEnhancer.enhance(metaClassInstance, activitiProviderInstance)`.

This plugin relies on the facilities exposed by the [datasource][2] plugin.

For each configured processEngine the plugin will deploy all `*.bpmn*.xml`, `*.form` and `*.png` files that are found in a 
conventional location that matches the engine's name. For example, for the `default` engine this location is
`griffon-app/resources/activiti/default`.

Configuration
-------------
### Dynamic method injection

The `withActiviti()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.activiti.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * ActivitiConnectStart[config, engineName] - triggered before connecting to the process engine
 * ActivitiConnectEnd[engineName, engine] - triggered after connecting to the process engine
 * ActivitiDisconnectStart[config, engineName, engine] - triggered before disconnecting from the process engine
 * ActivitiDisconnectEnd[config, engineName] - triggered after disconnecting from the process engine

### Multiple Process Engines

The config file `ActivitiConfig.groovy` defines a default processEngine block. As the name
implies this is the process engine used by default, however you can configure named process engines
by adding a new config block. For example connecting to a process engine whose name is 'internal'
can be done in this way

    processEngines {
        internal {
            mailServerHost = 'server.acme.com'
        }
    }

This block can be used inside the `environments()` block in the same way as the
default processEngine block is used.

Testing
-------
The `withActiviti()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `ActivitiEnhancer.enhance(metaClassInstance, activitiProviderInstance)` where 
`activitiProviderInstance` is of type `griffon.plugins.activiti.ActivitiProvider`. The contract for this interface looks like this

    public interface ActivitiProvider {
        Object withActiviti(Closure closure);
        Object withActiviti(String engineName, Closure closure);
        <T> T withActiviti(CallableWithArgs<T> callable);
        <T> T withActiviti(String engineName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyActivitiProvider implements ActivitiProvider {
        Object withActiviti(String engineName = 'default', Closure closure) { null }
        public <T> T withActiviti(String engineName = 'default', CallableWithArgs<T> callable) { null }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            ActivitiEnhancer.enhance(service.metaClass, new MyActivitiProvider())
            // exercise service methods
        }
    }


[1]: http://activiti.org
[2]: /plugin/datasource 
'''
}
