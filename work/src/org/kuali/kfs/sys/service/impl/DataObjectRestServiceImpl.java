package org.kuali.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.ojb.broker.core.proxy.ListProxyDefaultImpl;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.kuali.kfs.sys.businessobject.datadictionary.FinancialSystemBusinessObjectEntry;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.DataObjectRestService;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.type.TypeUtils;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.datadictionary.InquirySectionDefinition;
import org.kuali.rice.kns.lookup.LookupableHelperService;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.service.DataDictionaryService;
import org.kuali.rice.krad.service.PersistenceStructureService;
import org.kuali.rice.krad.service.XmlObjectSerializerService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.ksb.security.service.DigitalSignatureService;
import org.kuali.rice.ksb.util.KSBConstants;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class DataObjectRestServiceImpl implements DataObjectRestService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DataObjectRestServiceImpl.class);

	private DataDictionaryService dataDictionaryService;
	private PersistenceStructureService persistenceStructureService;
    private ParameterService parameterService;
    private PermissionService permissionService;
	private static final String ERROR_PAGE = "<html> <head> <title>Error report</title> <style> <!-- H1 { font-family: Tahoma, Arial, sans-serif; color: white; background-color: #525D76; font-size: 22px; }  H2 { font-family: Tahoma, Arial, sans-serif; color: white; background-color: #525D76; font-size: 16px; }  H3 { font-family: Tahoma, Arial, sans-serif; color: white; background-color: #525D76; font-size: 14px; }  BODY { font-family: Tahoma, Arial, sans-serif; color: black; background-color: white; }  B { font-family: Tahoma, Arial, sans-serif; color: white; background-color: #525D76; }  P,DIV { font-family: Tahoma, Arial, sans-serif; background: white; color: black; font-size: 12px; }  A { color: black; }  A.name { color: black; }  HR { color: #525D76; } --> </style> </head> <body> <h1>HTTP Status [STATUS] - [URI]</h1> <HR size='1' noshade='noshade'> <p> <b>type</b> Status report </p> <p> <b>message</b> <u>[URI]</u> </p> <p> <b>description</b> <u>[DESC]</u> </p> <HR size='1' noshade='noshade'> <div>[EXCEPTION]</div></body> </html>";

	private static final String STATUS = "[STATUS]";
	private static final String URI = "[URI]";
	private static final String DESC = "[DESC]";
	private static final String EXCEPTION = "[EXCEPTION]";

	protected enum DataType {
	    JSON, XML
	}

	@SuppressWarnings("deprecation")
    @Override
    public Response getDataObjects(String namespace, String dataobject, String type, UriInfo info, HttpHeaders headers, HttpServletRequest request) throws Exception {
        try {
            FinancialSystemBusinessObjectEntry boe = null;
            try {
                boe = (FinancialSystemBusinessObjectEntry) getDataDictionaryService().getDictionaryObject(dataobject);
            } catch (NoSuchBeanDefinitionException e) {
                LOG.debug("Failed to retrieve data dictionary object.", e);
            }

            // check for https (will be ignored in dev mode), authorization
            if ((!ConfigContext.getCurrentContextConfig().getDevMode() && !request.isSecure()) ||
                    !isAuthorized(request, boe)) {
                return Response.status(Response.Status.FORBIDDEN).entity(generateErrorResponse(Response.Status.FORBIDDEN.getStatusCode() + "", info.getRequestUri().getPath(), Response.Status.FORBIDDEN.getReasonPhrase())).build();
            }

            if (boe == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(generateErrorResponse(Response.Status.BAD_REQUEST.getStatusCode() + "", info.getRequestUri().getPath(), Response.Status.BAD_REQUEST.getReasonPhrase())).build();
            }

            DataType dataType = null;
            try {
                dataType = DataType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                LOG.debug("Unknown data type.", e);
            }

            Boolean isModuleLocked = getParameterService().getParameterValueAsBoolean(namespace, KfsParameterConstants.PARAMETER_ALL_DETAIL_TYPE, KRADConstants.SystemGroupParameterNames.OLTP_LOCKOUT_ACTIVE_IND);

            // check for locked module, json/xml, uriInfo, inquiry definition
            if (isModuleLocked ||
                    dataType == null ||
                    info == null ||
                    !boe.hasInquiryDefinition()) {
                return Response.status(Response.Status.FORBIDDEN).entity(generateErrorResponse(Response.Status.FORBIDDEN.getStatusCode() + "", info.getRequestUri().getPath(), Response.Status.FORBIDDEN.getReasonPhrase())).build();
            }

            Map<String, String> fieldValues = new HashMap<String, String>();
            for (String key : info.getQueryParameters().keySet()) {
                fieldValues.put(key, info.getQueryParameters().getFirst(key));
            }

            LookupableHelperService lookupableHelperService = getLookupableHelperService(boe.getLookupDefinition().getLookupableID());
            lookupableHelperService.setBusinessObjectClass(boe.getBusinessObjectClass());

            List<? extends BusinessObject> results = lookupableHelperService.getSearchResults(fieldValues);

            List<String> inquiryFields = new ArrayList<String>();
            for (InquirySectionDefinition section : boe.getInquiryDefinition().getInquirySections()) {
                inquiryFields.addAll(section.getInquiryFieldNames());
            }

            List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();
            List<Object> objects = new ArrayList<Object>();

            for (BusinessObject bo : results) {
                Map<String, Object> objectMap = new HashMap<String, Object>();
                Object object = ObjectUtils.createNewObjectFromClass(boe.getBusinessObjectClass());
                for (String propertyName : inquiryFields) {
                    Object propertyValue = ObjectUtils.getPropertyValue(bo, propertyName);
                    Class<?> propertyType = ObjectUtils.getPropertyType(bo, propertyName, getPersistenceStructureService());
                    if (propertyType != null &&
                            (TypeUtils.isStringClass(propertyType) ||
                            TypeUtils.isIntegralClass(propertyType) ||
                            TypeUtils.isDecimalClass(propertyType) ||
                            TypeUtils.isTemporalClass(propertyType) ||
                            TypeUtils.isBooleanClass(propertyType))) {

                        switch (dataType) {
                            case JSON:
                                objectMap.put(propertyName, propertyValue);
                                break;
                            case XML:
                                if (!propertyName.contains(".")) {
                                    ObjectUtils.setObjectProperty(object, propertyName, propertyValue);
                                }

                                break;
                        }
                    }
                }

                resultMap.add(objectMap);
                objects.add(object);
            }

            switch (dataType) {
                case JSON:
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);

                    String jsonData = null;
                    if (resultMap.size() == 1) {
                        jsonData = mapper.defaultPrettyPrintingWriter().writeValueAsString(resultMap.get(0))
                                .replaceFirst(HashMap.class.getSimpleName(), boe.getBusinessObjectClass().getName());
                    } else {
                        jsonData = mapper.defaultPrettyPrintingWriter().writeValueAsString(resultMap)
                                .replaceFirst(ArrayList.class.getSimpleName(), ArrayList.class.getSimpleName()+"<"+boe.getBusinessObjectClass().getName()+">");
                    }

                    return Response.ok(jsonData, MediaType.APPLICATION_JSON).build();
                case XML:
                    String xmlData = null;
                    if (objects.size() == 1) {
                        xmlData = getXmlObjectSerializerService().toXml(objects.get(0));
                    } else {
                        xmlData = getXmlObjectSerializerService().toXml(objects).replaceAll(ListProxyDefaultImpl.class.getName(), List.class.getName());
                    }

                    return Response.ok(xmlData, MediaType.APPLICATION_XML).build();
            }
        }catch (Exception e) {
            LOG.debug("Unexpected exception has occured.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(generateErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() + "", info.getRequestUri().getPath(), Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), e)).build();
        }

        return Response.status(Response.Status.FORBIDDEN).entity(generateErrorResponse(Response.Status.FORBIDDEN.getStatusCode() + "", info.getRequestUri().getPath(), Response.Status.FORBIDDEN.getReasonPhrase())).build();
    }

	protected boolean isAuthorized(HttpServletRequest request, FinancialSystemBusinessObjectEntry boe) throws Exception {
	    if (request != null && boe != null) {
    	    UserSession userSession = KRADUtils.getUserSessionFromRequest(request);
            if (userSession != null && GlobalVariables.getUserSession().getKualiSessionId().equals(userSession.getKualiSessionId())) {
                Class businessObjectClass = boe.getBusinessObjectClass();
                return getPermissionService().isAuthorizedByTemplate(
                        GlobalVariables.getUserSession().getPrincipalId(), KRADConstants.KNS_NAMESPACE,
                        KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                        KRADUtils.getNamespaceAndComponentSimpleName(businessObjectClass),
                        Collections.<String, String>emptyMap());
    	    }
	    }

        return false;
	}

	/*
	protected boolean verifySignature(HttpHeaders headers) {
	    try {
    	    String encodedSignature = headers.getRequestHeader(KSBConstants.DIGITAL_SIGNATURE_HEADER).get(0);
    	    String encodedCertificate = headers.getRequestHeader(KSBConstants.KEYSTORE_CERTIFICATE_HEADER).get(0);
    	    //String verificationAlias = headers.getRequestHeader(KSBConstants.KEYSTORE_ALIAS_HEADER).get(0);

    	    Signature verifySig = null;
            byte[] digitalSignature = null;

            digitalSignature = Base64.decodeBase64(encodedSignature.getBytes("UTF-8"));
            if (StringUtils.isNotBlank(encodedCertificate)) {
                byte[] certificate = Base64.decodeBase64(encodedCertificate.getBytes("UTF-8"));
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                verifySig = getDigitalSignatureService().getSignatureForVerification(cf.generateCertificate(new ByteArrayInputStream(certificate)));
            }// else if (StringUtils.isNotBlank(verificationAlias)) {
                //verifySig = getDigitalSignatureService().getSignatureForVerification(verificationAlias);
            //}

            return verifySig.verify(digitalSignature);
        } catch (Exception e) {
            LOG.error("Failed to initialize digital signature verification.", e);
        }

        return false;
    }*/

    @Override
    public Response getDataObjectsByModule(String namespace, UriInfo info) throws Exception {
        return Response.status(Response.Status.FORBIDDEN).entity(generateErrorResponse(Response.Status.FORBIDDEN.getStatusCode() + "", info.getRequestUri().getPath(), Response.Status.FORBIDDEN.getReasonPhrase())).build();
    }

    protected String generateErrorResponse(String statusCode, String uri, String desc) {
        return generateErrorResponse(statusCode, uri, desc, null);
    }

    protected String generateErrorResponse(String statusCode, String uri, String desc, Exception e) {
        return ERROR_PAGE.replaceAll(Pattern.quote(STATUS), statusCode)
                .replaceAll(Pattern.quote(URI), uri)
                .replaceAll(Pattern.quote(DESC), desc)
                .replaceAll(Pattern.quote(EXCEPTION), e != null ? Matcher.quoteReplacement(ExceptionUtils.getStackTrace(e)) : "");
    }

    protected LookupableHelperService getLookupableHelperService(String lookupableID) {
        LookupableHelperService lookupableHelperService = LookupableSpringContext.getLookupable(lookupableID).getLookupableHelperService();

        return lookupableHelperService;
    }

    public DataDictionaryService getDataDictionaryService() {
        if(dataDictionaryService == null) {
            dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        }
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public PersistenceStructureService getPersistenceStructureService() {
        if(persistenceStructureService == null) {
            persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);
        }
        return persistenceStructureService;
    }

    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    public ParameterService getParameterService() {
        if(parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public XmlObjectSerializerService getXmlObjectSerializerService() {
        return SpringContext.getBean(XmlObjectSerializerService.class);
    }

    protected DigitalSignatureService getDigitalSignatureService() {
        return (DigitalSignatureService) GlobalResourceLoader.getService(KSBConstants.ServiceNames.DIGITAL_SIGNATURE_SERVICE);
    }

    public PermissionService getPermissionService() {
        if(permissionService == null) {
            permissionService = KimApiServiceLocator.getPermissionService();
        }
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

}
