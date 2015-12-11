package com.xebia.services;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class TimeClient {
    private final static Logger LOG = LoggerFactory.getLogger(TimeClient.class);
    private final Client client;
    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE, d MMM YYYY HH:mm:ss Z");

    public TimeClient(Client client) {
        this.client = client;
    }

    public DateTime getTime() {
        LOG.debug("Retrieving time");
        WebTarget target = client.target("http://json-time.appspot.com/time.json");
        Response response = target.request().buildGet().invoke();
        String s = response.readEntity(String.class);
        DocumentContext parse = JsonPath.parse(s);
        String datetime = parse.read("datetime", String.class);
        return fmt.parseDateTime(datetime);
    }
}
