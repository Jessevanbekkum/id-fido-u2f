package com.xebia.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.annotation.Timed;
import com.xebia.dao.PersonDAO;
import com.xebia.domain.Person;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;

@Path("person")
public class PersonResource {
    private final static Logger LOG = LoggerFactory.getLogger(PersonResource.class);
    private final PersonDAO dao;

    public PersonResource(final PersonDAO dao) {
        this.dao = dao;
    }

    @GET
    @Timed
    @UnitOfWork
    public Person findPerson(@PathParam("id") LongParam id) {
        return dao.findById(id.get());
    }

    @POST
    @Timed
    @UnitOfWork
    public Person create(Person person) {
        long id = dao.create(person);
        return dao.findById(id);
    }
}
