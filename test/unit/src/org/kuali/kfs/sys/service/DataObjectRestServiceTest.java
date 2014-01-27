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
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.ksb.security.admin.service.JavaSecurityManagementService;
import org.kuali.rice.ksb.security.service.DigitalSignatureService;
import org.kuali.rice.ksb.util.KSBConstants;

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
            return URI.create("https://localhost:8080/kfs-dev/remoting/dataobjects/KFS-COA/Account.json?chartOfAccountsCode=BL&accountNumber=1031420");
        }
        @Override
        public UriBuilder getRequestUriBuilder() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public class MockHttpHeaders implements HttpHeaders {
        protected Map<String, List<String>> headerFields = new HashMap<String, List<String>>();

        public MockHttpHeaders() {
            try {
                Signature rsa = getDigitalSignatureService().getSignatureForSigning();

                String moduleKeyStoreAlias = getJavaSecurityManagementService().getModuleKeyStoreAlias();
                headerFields.put(KSBConstants.DIGITAL_SIGNATURE_HEADER, Arrays.asList(new String(Base64.encodeBase64(rsa.sign()), "UTF-8")));
                headerFields.put(KSBConstants.KEYSTORE_ALIAS_HEADER, Arrays.asList(moduleKeyStoreAlias));
                Certificate cert = getJavaSecurityManagementService().getCertificate(moduleKeyStoreAlias);
                headerFields.put(KSBConstants.KEYSTORE_CERTIFICATE_HEADER, Arrays.asList(new String(Base64.encodeBase64(cert.getEncoded()), "UTF-8")));
            }
            catch (Exception ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }

        @Override
        public List<Locale> getAcceptableLanguages() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<MediaType> getAcceptableMediaTypes() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, Cookie> getCookies() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Date getDate() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getHeaderString(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Locale getLanguage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getLength() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public MediaType getMediaType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<String> getRequestHeader(String arg0) {
            // TODO Auto-generated method stub
            return headerFields.get(arg0);
        }

        @Override
        public MultivaluedMap<String, String> getRequestHeaders() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public void testGetDataObjects() throws Exception {
        assertNotNull(getDataObjectRestService());

        String namespace = "KFS-COA";
        String dataObject = "Account";

        MultivaluedMap<String, String> queryParameters = new MetadataMap<String, String>();
        queryParameters.add("chartOfAccountsCode", "BL");
        queryParameters.add("accountNumber", "1031420");

        MockUriInfo mockUriInfo = new MockUriInfo(queryParameters);
        HttpHeaders header = new MockHttpHeaders();

        Response jsonDataObjectsOutput = getDataObjectRestService().getDataObjects(namespace, dataObject, "json", mockUriInfo, header, null);
        System.out.println("JSON output:");
        System.out.println(jsonDataObjectsOutput.getEntity());

        Response xmlDataObjectsOutput = getDataObjectRestService().getDataObjects(namespace, dataObject, "xml" , mockUriInfo, header, null);
        System.out.println("XML output:");
        System.out.println(xmlDataObjectsOutput.getEntity());
    }

    protected DataObjectRestService getDataObjectRestService() {
        return SpringContext.getBean(DataObjectRestService.class);
    }

    protected DigitalSignatureService getDigitalSignatureService() {
        return (DigitalSignatureService) GlobalResourceLoader.getService(KSBConstants.ServiceNames.DIGITAL_SIGNATURE_SERVICE);
    }

    protected JavaSecurityManagementService getJavaSecurityManagementService() {
        return (JavaSecurityManagementService)GlobalResourceLoader.getService(KSBConstants.ServiceNames.JAVA_SECURITY_MANAGEMENT_SERVICE);
    }
}
