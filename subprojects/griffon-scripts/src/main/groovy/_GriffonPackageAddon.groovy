/*
 * Copyright 2009-2012 the original author or authors.
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

/**
 * Gant script that packages a Griffon addon inside of a plugin
 *
 * @author Danno Ferrin
 */

// No point doing this stuff more than once.
if (getBinding().variables.containsKey('_griffon_package_addon_called')) return
_griffon_package_addon_called = true

includeTargets << griffonScript('_GriffonPackage')

target(name: 'packageAddon', description: 'Packages a Griffon addon',
        prehook: null, posthook: null) {
    projectType = 'plugin'
    createStructure()

    try {
        profile("compile") {
            compile()
        }
    } catch (Exception e) {
        logError("Compilation error", e)
        exit(1)
    }

    addonJarDir = "${artifactPackageDirPath}/dist"
    ant.mkdir(dir: addonJarDir)

    cliJarName = "griffon-${pluginName}-compile-${pluginVersion}.jar"
    if (cliSourceDir.exists()) {
        ant.jar(destfile: "$addonJarDir/$cliJarName") {
            fileset(dir: projectCliClassesDir)
        }
    }

    if (!isAddonPlugin) return

    profile("creating config") {
        createConfig()
    }

    packageResources()

    File metainfDirPath = new File("${basedir}/griffon-app/conf/metainf")
    addonJarName = "griffon-${pluginName}-runtime-${pluginVersion}.jar"
    ant.jar(destfile: "$addonJarDir/$addonJarName") {
        fileset(dir: projectMainClassesDir) {
            exclude(name: 'Config*.class')
            exclude(name: 'BuildConfig*.class')
            exclude(name: '*GriffonPlugin*.class')
            exclude(name: 'application.properties')
        }
        fileset(dir: i18nDir)
        fileset(dir: resourcesDir)

        if (metainfDirPath.list()) {
            metainf(dir: metainfDirPath)
        }
    }
}
