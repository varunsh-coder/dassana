package app.dassana.core.runmanager.launch.api;


import app.dassana.core.runmanager.client.infra.S3Store;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.HttpResponse;

import javax.inject.Inject;

// The @Controller annotation defines the class as a controller mapped to the path /alert
@Controller("/alert")
public class Alert {

    @Inject
    private S3Store s3Store;

    @Value("${env.dassanaBucket}")
    String s3Bucket;

    // The @Get annotation maps the index method to all requests that use an HTTP GET
    @Get
    public HttpResponse<String> getAlert(@QueryValue("vendorId") String vendorId, @QueryValue("alertId") String alertId) {
        String uploadedPath = getUploadedPath(vendorId, alertId);
        // fetches the alert using uploaded path as key by calling getObject method in s3Store class
        HttpResponse<String> response = HttpResponse.ok().body(s3Store.getContent(uploadedPath));
        return response;
    }

    // gets the objects uploaded path (Key) in S3
    public String getUploadedPath(String vendorId, String alertId) {
        return "alerts".concat("/").concat(vendorId).concat("/").concat(alertId);
    }
}
