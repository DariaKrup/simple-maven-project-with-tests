import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.remoteParameters.hashiCorpVaultParameter

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
}

object Build : BuildType({
    name = "Build"

    params {
        param("web_agentName", "", "webPopulatedSelect", "headers" to """
            Authorization:Bearer eyJ0eXAiOiAiVENWMiJ9.bzBibk4taDhicjdKS1daWENnUkQ1eXU0Z1o4.MjdhZDY2OTAtYzM0Ny00NTVkLTk2OWItZmRhZjI5NmI3NTFl
            Accept:application/json
        """.trimIndent(), "method" to "GET", "display" to "normal", "format" to "json", "multiple" to "true", "label" to "Web parameter for agent name", "tagSupport" to "true", "url" to "http://10.128.93.57:8134/app/rest/agents/id:%text_agent_number%", "transform" to """
            [
              {
                "operation": "shift",
                "spec": {
                  "name": ["options[#].key", "options[#].value"]
                }
              }
            ]
        """.trimIndent())
        password("password", "credentialsJSON:27a44fd6-e392-410a-b15d-f0488c67be9a", readOnly = true)
        select("select", "", label = "Parameter with multiple selection",
                allowMultiple = true, valueSeparator = ";",
                options = listOf("a1" to "1", "a2" to "2", "a6" to "9"))
        hashiCorpVaultParameter {
            name = "env.AWS_SECRET_ACCESS_KEY"
            query = "aws/data/access!/AWS_SECRET_ACCESS_KEY"
        }
        text("text_parameter_any", "not_empty_text", display = ParameterDisplay.HIDDEN, allowEmpty = true)
        text("text_agent_number", "31", display = ParameterDisplay.HIDDEN, readOnly = true, allowEmpty = true)
        hashiCorpVaultParameter {
            name = "env.AWS_ACCESS_KEY_ID"
            readOnly = true
            query = "aws/data/access!/AWS_ACCESS_KEY_ID"
        }
        checkbox("checkbox", "", description = """Tick checkbox for "true"""", display = ParameterDisplay.PROMPT,
                  checked = "true", unchecked = "false")
        text("text_parameter_regex", "a1",
              regex = "a1*", validationMessage = "REGEX check failed!")
        text("text_parameter_not_empty", "", label = "Not empty parameter", description = "Fill this field", display = ParameterDisplay.PROMPT, allowEmpty = false)
        text("system.JVM_PROPERTY", "%env.JDK_1_8%", allowEmpty = true)
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            id = "Maven2"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }
})
