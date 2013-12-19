/*
 * Copyright 2013 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.sys.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.khuntley;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

@ConfigureContext(session = khuntley)
public class DataObjectRestServiceTest extends KualiTestBase {

    public class MockUriInfo implements UriInfo {
        MultivaluedMap<String, String> queryParameters;

        public MockUriInfo() {

        }

        public MockUriInfo(MultivaluedMap<String, String> queryParameters) {
            this.queryParameters = queryParameters;
        }

        @Override
        public URI getAbsolutePath() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public UriBuilder getAbsolutePathBuilder() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public URI getBaseUri() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public UriBuilder getBaseUriBuilder() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public List<Object> getMatchedResources() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public List<String> getMatchedURIs() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public List<String> getMatchedURIs(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public String getPath() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public String getPath(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public MultivaluedMap<String, String> getPathParameters() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public MultivaluedMap<String, String> getPathParameters(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public List<PathSegment> getPathSegments() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public List<PathSegment> getPathSegments(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public MultivaluedMap<String, String> getQueryParameters() {
            return queryParameters;
        }
        @Override
        public MultivaluedMap<String, String> getQueryParameters(boolean arg0) {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public URI getRequestUri() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public UriBuilder getRequestUriBuilder() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public void testGetDataObjects() throws Exception {
        assertNotNull(getDataObjectRestService());

        String namespace = "KFS-COA";
        String xmlDataObject = "Account.xml";
        String jsonDataObject = "Account.json";

        MultivaluedMap<String, String> queryParameters = new MetadataMap<String, String>();
        queryParameters.add("chartOfAccountsCode", "BL");
        queryParameters.add("accountNumber", "1031420");

        MockUriInfo mockUriInfo = new MockUriInfo(queryParameters);

        String jsonDataObjectsOutput = getDataObjectRestService().getDataObjects(namespace, jsonDataObject, mockUriInfo);
        System.out.println("JSON output:");
        System.out.println(jsonDataObjectsOutput);

        String xmlDataObjectsOutput = getDataObjectRestService().getDataObjects(namespace, xmlDataObject, mockUriInfo);
        System.out.println("XML output:");
        System.out.println(xmlDataObjectsOutput);
    }

    public DataObjectRestService getDataObjectRestService() {
        return SpringContext.getBean(DataObjectRestService.class);
    }


}
