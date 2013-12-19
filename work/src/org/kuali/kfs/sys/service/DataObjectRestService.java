package org.kuali.kfs.sys.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/")
public interface DataObjectRestService {

    @GET
    @Path("/dataobjects/{namespace}/dataobject")
	public String getDataObjects(@PathParam("namespace") String namespace, @PathParam("namespace") String dataobject, @Context UriInfo info);

}
