package com.xebia.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xebia.services.TimeClient;

@Path("time")
public class TimeResource {
    private final static Logger LOG = LoggerFactory.getLogger(TimeResource.class);
    private final TimeClient client;

    public TimeResource(final TimeClient client) {
        this.client = client;
    }

    @GET
    public String getTime() {
        DateTime time = client.getTime();

        return time.toString();
    }

}
