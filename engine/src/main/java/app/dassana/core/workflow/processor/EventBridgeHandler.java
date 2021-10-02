package app.dassana.core.workflow.processor;

import app.dassana.core.launch.model.ProcessingResponse;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

@Singleton
public class EventBridgeHandler {

  private final String dassanaEventBridgeBusName = System.getenv().get("dassanaEventBridgeBusName");

  @Inject private EventBridgeClient eventBridgeClient;

  public static final String NORMALIZER_NOT_FOUND = "normalizer_not_found";

  private static final Logger logger = LoggerFactory.getLogger(EventBridgeHandler.class);


  public void handleEventBridge(ProcessingResponse processingResponse, String normalizerId) {

    if (StringUtils.isEmpty(normalizerId)) {
      normalizerId = NORMALIZER_NOT_FOUND;
    }

    if (StringUtils.isNotEmpty(dassanaEventBridgeBusName)) {
      PutEventsRequestEntry putEventsRequestEntry = PutEventsRequestEntry.builder()
          .eventBusName(dassanaEventBridgeBusName)
          .detail(processingResponse.getDecoratedJson())
          .source(Decorator.DASSANA_KEY)
          .detailType(normalizerId)
          .build();

      PutEventsResponse putEventsResponse = eventBridgeClient.putEvents(
          PutEventsRequest.builder().entries(putEventsRequestEntry).build());

      if (putEventsResponse.failedEntryCount() > 0) {
        throw new RuntimeException(
            "Unable to send events to event-bridge due to error: ".concat(putEventsResponse.toString()));
      }
    } else {
      logger.warn("dassanaEventBridgeBusName not set as environment variable");
    }


  }

}
