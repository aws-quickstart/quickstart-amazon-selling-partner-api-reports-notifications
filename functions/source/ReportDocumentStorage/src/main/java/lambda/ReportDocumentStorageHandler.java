package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReportDocumentStorageHandler implements RequestHandler<Map<String, String>, String> {

    //Lambda Environment Variables
    private static final String DESTINATION_S3_BUCKET_NAME_ENV_VARIABLE = "DESTINATION_S3_BUCKET_NAME";

    //Lambda Input Parameters
    private static final String OBJECT_PRESIGNED_URL_KEY_NAME = "PresignedUrl";
    private static final String REPORT_TYPE_KEY_NAME = "ReportType";

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("ReportDocumentStorage Lambda handler started");

        validateInput(event);

        String objectPresignedUrl = event.get(OBJECT_PRESIGNED_URL_KEY_NAME);
        String reportType = event.get(REPORT_TYPE_KEY_NAME);
        String destinationS3Bucket = System.getenv(DESTINATION_S3_BUCKET_NAME_ENV_VARIABLE);

        String fileKey = String.format("%s/%s", reportType, UUID.randomUUID());
        logger.log(String.format("File Key: %s", fileKey));

        try {
            InputStream inputStream = URI.create(objectPresignedUrl).toURL().openConnection().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String data = IOUtils.toString(reader);

            AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
            s3.putObject(destinationS3Bucket, fileKey, data);
        } catch (Exception e) {
            throw new InternalError("Report document storage failed", e);
        }

        return fileKey;
    }

    private void validateInput(Map<String, String> event) {
        List<String> requiredParameters = Lists.newArrayList(
                OBJECT_PRESIGNED_URL_KEY_NAME,
                REPORT_TYPE_KEY_NAME);

        if (!event.keySet().containsAll(requiredParameters)) {
            String msg = String.format("The provided input must contain all the following keys: %s",
                    requiredParameters);

            throw new IllegalArgumentException(msg);
        }
    }
}
