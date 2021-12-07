package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public class ReportNotificationProcessorHandler implements RequestHandler<SQSEvent, String> {

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("NotificationProcessor Lambda handler started");

        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            logger.log("Received new notification: " + msg.getBody());
            //TODO: start Step Functions state machine
        }

        return "Finished processing incoming notifications!";
    }
}
