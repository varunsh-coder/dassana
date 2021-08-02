package app.dassana.core.launch;

import app.dassana.core.contentmanager.ContentManagerApi;
import app.dassana.core.launch.model.RequestConfig;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import java.util.List;
import javax.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Introspected
public class Handler extends MicronautRequestHandler<SQSEvent, Void> {

  private static final Logger logger = LoggerFactory.getLogger(Handler.class);

  private final String dassanaDeadLetterQueue = System.getenv("dassanaDeadLetterQueue");

  @Inject private ContentManagerApi contentManager;
  @Inject private RequestProcessor requestProcessor;
  @Inject private SqsClient sqsClient;


  @Override
  public Void execute(SQSEvent input) {

    List<SQSMessage> sqsMessages = input.getRecords();

    for (SQSMessage message : sqsMessages) {
      try {
        checkValidJson(message);
        RequestConfig requestConfig = new RequestConfig();
        requestConfig.setInputJson(message.getBody());

        requestConfig.setQueueProcessing(true);

        requestConfig.setSkipResourcePrioritization(false);
        requestConfig.setSkipResourceContextualization(false);
        requestConfig.setSkipPostProcessor(false);

        requestProcessor.processRequest(requestConfig);

      } catch (Exception e) {
        handleException(e, message.getBody());

      }


    }

    return null;

  }

  private void handleException(Exception exception, String originalInput) {

    logger.error("Error in processing message: ", exception);
    JSONObject jsonObject = new JSONObject(originalInput);
    jsonObject.put("error", exception);

    sqsClient.sendMessage(
        SendMessageRequest.builder().queueUrl(dassanaDeadLetterQueue)
            .messageBody(jsonObject.toString())
            .build());


  }

  private void checkValidJson(SQSMessage message) {

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.readValue(message.getBody(), JsonNode.class);
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Dassana Engine can only process JSON input, input sent was %s",
              message.getBody()));

    }

  }


}
