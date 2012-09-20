griffon.project.dependency.resolution = {
    // implicit variables
    // pluginName:     plugin's name
    // pluginVersion:  plugin's version
    // pluginDirPath:  plugin's install path
    // griffonVersion: current Griffon version
    // groovyVersion:  bundled groovy
    // springVersion:  bundled Spring
    // antVertsion:    bundled Ant
    // slf4jVersion:   bundled Slf4j

    // inherit Griffon' default dependencies
    inherits "global"
    log "warn"
    repositories {
        griffonHome()
        mavenCentral()
        mavenRepo 'https://maven.alfresco.com/nexus/content/repositories/activiti/'
        mavenRepo 'http://repository.springsource.com/maven/bundles/release'

        // pluginDirPath is only available when installed
        // String basePath = pluginDirPath? "${pluginDirPath}/" : ''
        // flatDir name: "${pluginName}LibDir", dirs: ["${basePath}lib"]
    }
    dependencies {
        String activitiVersion = '5.10'
        compile("org.activiti:activiti-engine:$activitiVersion") {
            excludes 'groovy', 'spring-beans'
        }
        compile "org.springframework:org.springframework.beans:$springVersion",
                "org.springframework:org.springframework.asm:$springVersion",
                "org.springframework:org.springframework.core:$springVersion"
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon-framework.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (@griffon.version@)"
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}