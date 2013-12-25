package org.kuali.kfs.sys.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public interface DataObjectRestService {

    @GET
    @Path("/{namespace}/{dataobject}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getDataObjects(@PathParam("namespace") String namespace, @PathParam("dataobject") String dataobject, @Context UriInfo info) throws Exception;

}
