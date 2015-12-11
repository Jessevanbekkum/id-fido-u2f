package com.xebia.services;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

public class TimeClientTest {
    @Test
    public void shouldTellTime() {
        Client client = ClientBuilder.newClient();
        TimeClient timeClient = new TimeClient(client);

        timeClient.getTime();
    }

    @Test
    public void parseTest() {
        String s = "Sat, 18 Apr 2015 19:30:24 +0000";
                 //"Sat, 18 Apr 2015 23:21:56 +0200"
        DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE, d MMM YYYY HH:mm:ss Z");

        System.out.println( fmt.print(new DateTime()));

    }
}