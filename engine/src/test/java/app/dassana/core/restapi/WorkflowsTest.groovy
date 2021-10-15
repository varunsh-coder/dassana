package app.dassana.core.restapi

import app.dassana.core.contentmanager.Parser
import app.dassana.core.util.JsonyThings
import app.dassana.core.util.StringyThings
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.json.JSONObject
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@MicronautTest
class WorkflowsTest extends Specification {
    public static final String FOO_NORMALIZER_ID = "foo-cloud-normalize";


    @Inject
    Workflows workflows

    @Inject
    Parser parser;

    @Unroll
    void "workflow-get"() {
        when:
        def response = workflows.getWorkflow("foo", true)

        then:
        response.code() == 404

        when:
        def fooDefault = workflows.getWorkflow("foo-cloud-normalize", true)
        def jsonFromYaml = StringyThings.getJsonFromYaml(fooDefault.body())
        JSONObject jsonObject = new JSONObject(jsonFromYaml)

        then:
        fooDefault.code() == 200
        jsonObject.getString("id") == "foo-cloud-normalize"
        jsonObject.getString("vendor-id") == "foo-cloud-cspm-vendor"
    }

    @Unroll
    void "workflow-update"() {
        when:
        def defaultWorkflowBody = workflows.getWorkflow(FOO_NORMALIZER_ID, true).body()
        JSONObject defaultWorkflow = new JSONObject(StringyThings.getJsonFromYaml(defaultWorkflowBody))
        def outputJsonArray = defaultWorkflow.getJSONArray("output")
        JSONObject newField = new JSONObject();
        newField.put("name", "foo")
        newField.put("value", 42)
        newField.put("value-type", "INT")
        outputJsonArray.put(newField)
        def yamlFromJson = JsonyThings.getYamlFromJson(defaultWorkflow.toString())
        workflows.handleSaveToS3(yamlFromJson)
        //the following two calls are important- in the first call we are getting the default workflow, in the second
        // call we are getting the custom workflow
        def responseDefault = workflows.getWorkflow(FOO_NORMALIZER_ID, true)
        def updatedDefaultWorkflow = workflows.getWorkflow(FOO_NORMALIZER_ID, false)

        then:
        responseDefault.code() == 200
        updatedDefaultWorkflow.code() == 200
        //ensure that we are able to read the updated workflow back
        def updatedWorkflowJson = StringyThings.getJsonFromYaml(updatedDefaultWorkflow.body())
        def outputs = parser.getWorkflow(new JSONObject(updatedWorkflowJson)).output
        boolean desiredOutputFound = false
        outputs.forEach(output -> {
            String name = output.name
            String value = output.value
            String valueType = output.valueType;
            if (name == "foo" && value == "42" && valueType == "INT") {
                desiredOutputFound = true
            }
        })
        desiredOutputFound == true

    }

    @Unroll
    void "workflow-delete"() {
        when:
        workflows.handleDelete(FOO_NORMALIZER_ID)
        def defaultFooWorkflow = workflows.getWorkflow(FOO_NORMALIZER_ID, true)
        def customFooWorkflow = workflows.getWorkflow(FOO_NORMALIZER_ID, false)

        then:
        defaultFooWorkflow.code() == 200
        customFooWorkflow.code() == 200

        //here we are ensuring that the customization we did is no longer available as we have deleted the custom
        // workflow
        def updatedWorkflowJson = StringyThings.getJsonFromYaml(customFooWorkflow.body())
        def outputs = parser.getWorkflow(new JSONObject(updatedWorkflowJson)).output
        boolean desiredOutputFound = true
        outputs.forEach(output -> {
            String name = output.name
            String value = output.value
            String valueType = output.valueType;
            if (name == "foo" && value == "42" && valueType == "INT") {//this shouldn't happen as we have deleted the
                // custom workflow
                desiredOutputFound = false
            }
        })
        desiredOutputFound == true


    }

}
