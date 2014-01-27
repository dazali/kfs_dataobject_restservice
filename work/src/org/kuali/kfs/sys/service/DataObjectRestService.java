package org.kuali.kfs.sys.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public interface DataObjectRestService {

    @GET
    @Path("/{namespace}/{dataobject}.{type}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML})
	public Response getDataObjects(@PathParam("namespace") String namespace, @PathParam("dataobject") String dataobject, @PathParam("type") String type, @Context UriInfo info, @Context HttpHeaders headers, @Context HttpServletRequest request) throws Exception;

    @GET
    @Path("/{namespace}")
    @Produces({MediaType.TEXT_HTML})
    public Response getDataObjectsByModule(@PathParam("namespace") String namespace, @Context UriInfo info) throws Exception;
}
