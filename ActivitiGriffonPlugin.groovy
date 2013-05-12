/*
 * Copyright 2012-2013 the original author or authors.
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
    String version = '1.2.0'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.3.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [datasource: '1.3.0']
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

[Activiti][1] is a light-weight workflow and Business Process Management (BPM)
Platform targeted at business people, developers and system admins. Its core
is a super-fast and rock-solid BPMN 2 process engine for Java. It's open-source
and distributed under the Apache license.

Usage
-----
Upon installation the plugin will generate the following artifacts in
`$appdir/griffon-app/conf`:

 * ActivitiConfig.groovy - contains ProcessEngine definitions.

A new dynamic method named `withActiviti` will be injected into all controllers,
giving you access to a `org.activiti.engine.ProcessEngine` object, with which
you'll be able to make calls to the repository. Remember to make all repository
calls off the UI thread otherwise your application may appear unresponsive when
doing long computations inside the UI thread.

This method is aware of multiple engines. If no engineName is specified
when calling it then the default dataSource will be selected. Here are two
example usages, the first queries against the default dataSource while the second
queries a dataSource whose name has been configured as 'internal'

    package sample
    class SampleController {
        def queryAllActivitiEngines = {
            withActiviti { engineName, sql -> ... }
            withActiviti('internal') { engineName, sql -> ... }
        }
    }

The following list enumerates all the variants of the injected method

 * `<R> R withActiviti(Closure<R> stmts)`
 * `<R> R withActiviti(CallableWithArgs<R> stmts)`
 * `<R> R withActiviti(String engineName, Closure<R> stmts)`
 * `<R> R withActiviti(String engineName, CallableWithArgs<R> stmts)`

These methods are also accessible to any component through the singleton
`griffon.plugins.activiti.ActivitiEnhancer`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `ActivitiEnhancer.enhance(metaClassInstance)`.

This plugin relies on the facilities exposed by the [datasource][2] plugin.

For each configured processEngine the plugin will deploy all `*.bpmn*.xml`,
`*.form` and `*.png` files that are found in a  conventional location that
matches the engine's name. For example, for the `default` engine this location
is `griffon-app/resources/activiti/default`.

Configuration
-------------
### ActivitiAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.activiti.ActivitiAware`. This transformation injects the
`griffon.plugins.activiti.ActivitiContributionHandler` interface and default
behavior that fulfills the contract.

### Dynamic Method Injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.activiti.injectInto = ['controller', 'service']

Dynamic method injection will be skipped for classes implementing
`griffon.plugins.activiti.ActivitiContributionHandler`.

### Events

The following events will be triggered by this addon

 * ActivitiConnectStart[config, engineName] - triggered before connecting to
   the process engine
 * ActivitiConnectEnd[engineName, engine] - triggered after connecting to the
   process engine
 * ActivitiDisconnectStart[config, engineName, engine] - triggered before
   disconnecting from the process engine
 * ActivitiDisconnectEnd[config, engineName] - triggered after disconnecting
   from the process engine

### Multiple Process Engines

The config file `ActivitiConfig.groovy` defines a default processEngine block.
As the name implies this is the process engine used by default, however you can
configure named process engines by adding a new config block. For example
connecting to a process engine whose name is 'internal' can be done in this way

    processEngines {
        internal {
            mailServerHost = 'server.acme.com'
        }
    }

This block can be used inside the `environments()` block in the same way as the
default processEngine block is used.

### Configuration Storage

The plugin will load and store the contents of `ActivitiConfig.groovy` inside the
application's configuration, under the `pluginConfig` namespace. You may retrieve
and/or update values using

    app.config.pluginConfig.activiti

### Connect at Startup

The plugin will attempt a connection to the default database at startup. If this
behavior is not desired then specify the following configuration flag in
`Config.groovy`

    griffon.activiti.connect.onstartup = false

Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`ActivitiEnhancer.enhance(metaClassInstance, activitiProviderInstance)` where
`activitiProviderInstance` is of type `griffon.plugins.activiti.ActivitiProvider`.
The contract for this interface looks like this

    public interface ActivitiProvider {
        <R> R withActiviti(Closure<R> closure);
        <R> R withActiviti(CallableWithArgs<R> callable);
        <R> R withActiviti(String engineName, Closure<R> closure);
        <R> R withActiviti(String engineName, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyActivitiProvider implements ActivitiProvider {
        public <R> R withActiviti(Closure<R> closure) { null }
        public <R> R withActiviti(CallableWithArgs<R> callable) { null }
        public <R> R withActiviti(String engineName, Closure<R> closure) { null }
        public <R> R withActiviti(String engineName, CallableWithArgs<R> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            ActivitiEnhancer.enhance(service.metaClass, new MyActivitiProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@ActivitiAware` then usage
of `ActivitiEnhancer` should be avoided at all costs. Simply set
`activitiProviderInstance` on the service instance directly, like so, first the
service definition

    @griffon.plugins.activiti.ActivitiAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.activitiProvider = new MyActivitiProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-activiti-compile-x.y.z.jar`, with locations

 * dsdl/activiti.dsld
 * gdsl/activiti.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
possible thanks to the [lombok][3] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][3] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/activiti-<version>/dist/griffon-activiti-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:\
        griffon-lombok-compile-<version>.jar:griffon-activiti-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@ActivitiAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][4]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@ActivitiAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][3] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-activiti-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/activiti-<version>/dist/griffon-activiti-compile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@ActivitiAware`.


[1]: http://activiti.org
[2]: /plugin/datasource 
[3]: /plugin/lombok
[4]: http://netbeans.org/kb/docs/java/annotations-lombok.html
'''
}
