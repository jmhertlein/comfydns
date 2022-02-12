package com.comfydns.resolver.task;

import com.comfydns.util.UserAgent;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class UsageReportTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(UsageReportTask.class);
    private final String proto, domain;
    private final UUID installId;

    public UsageReportTask(String proto, String domain, UUID installId) {
        this.proto = proto;
        this.domain = domain;
        this.installId = installId;
    }

    @Override
    public void run() {
        try {
            String urlString = String.format("%s://%s/api/usage_report/%s", proto, domain, installId);
            log.info("Reporting usage to {}", urlString);

            Response resp = Request.Post(urlString)
                    .userAgent(UserAgent.USER_AGENT)
                    .execute();
            HttpResponse httpResponse = resp.returnResponse();
            if(httpResponse.getStatusLine().getStatusCode() != 200) {
                log.error("HTTP error reporting usage: {}: {}", httpResponse.getStatusLine().getStatusCode(),
                        httpResponse.getStatusLine().getReasonPhrase());
            }

        } catch (IOException e) {
            log.error("Error reporting usage.", e);
        }
    }
}
