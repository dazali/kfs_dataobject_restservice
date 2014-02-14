/*
 * Copyright 2014 The Kuali Foundation.
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
package org.kuali.kfs.sys.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.ojb.broker.core.proxy.ListProxyDefaultImpl;
import org.kuali.kfs.sys.businessobject.datadictionary.FinancialSystemBusinessObjectEntry;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigContext;
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
import org.kuali.rice.krad.web.controller.DocumentControllerBase;
import org.kuali.rice.krad.web.form.DocumentFormBase;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DataObjectRestServiceController extends DocumentControllerBase {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DataObjectRestServiceController.class);

    private DataDictionaryService dataDictionaryService;
    private PersistenceStructureService persistenceStructureService;
    private ParameterService parameterService;
    private PermissionService permissionService;

    @Override
    protected DocumentFormBase createInitialForm(HttpServletRequest request) {
        DocumentFormBase form = new DocumentFormBase();
        form.setReturnLocation("NO_RETURN");
        return form;
    }

    @RequestMapping(value = "/{namespace}/{dataobject}.json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getDataObjectsAsJSON(@PathVariable("namespace") String namespace, @PathVariable("dataobject") String dataobject, HttpServletRequest request) throws Exception {
        try {
            FinancialSystemBusinessObjectEntry boe = getBusinessObject(dataobject);

            HttpStatus status = validateRequest(boe, namespace, dataobject, request);
            if (!status.equals(HttpStatus.OK)) {
                return new ResponseEntity<String>(generateErrorResponse(status.toString(), request.getRequestURI(), status.getReasonPhrase()), status);
            }

            List<? extends BusinessObject> results = getSearchResults(request, boe);
            List<String> inquiryFields = getInquiryFields(boe);

            List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();
            for (BusinessObject bo : results) {
                Map<String, Object> objectMap = new HashMap<String, Object>();
                Object object = ObjectUtils.createNewObjectFromClass(boe.getBusinessObjectClass());
                for (String propertyName : inquiryFields) {
                    Object propertyValue = ObjectUtils.getPropertyValue(bo, propertyName);
                    Class<?> propertyType = ObjectUtils.getPropertyType(bo, propertyName, getPersistenceStructureService());
                    if (isPropertyTypeValid(propertyType)) {
                        objectMap.put(propertyName, propertyValue);
                    }
                }

                resultMap.add(objectMap);
            }

            if (resultMap.size() == 1) {
                return new ResponseEntity(resultMap.get(0), HttpStatus.OK);
            } else {
                return new ResponseEntity(resultMap, HttpStatus.OK);
            }
        } catch (Exception e) {
            LOG.debug("Unexpected exception has occured.", e);
            return new ResponseEntity<String>(generateErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(), request.getRequestURI(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{namespace}/{dataobject}.xml", method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<String> getDataObjectsAsXML(@PathVariable("namespace") String namespace, @PathVariable("dataobject") String dataobject, HttpServletRequest request) throws Exception {
        try {
            FinancialSystemBusinessObjectEntry boe = getBusinessObject(dataobject);

            HttpStatus status = validateRequest(boe, namespace, dataobject, request);
            if (!status.equals(HttpStatus.OK)) {
                return new ResponseEntity<String>(generateErrorResponse(status.toString(), request.getRequestURI(), status.getReasonPhrase()), status);
            }

            List<? extends BusinessObject> results = getSearchResults(request, boe);
            List<String> inquiryFields = getInquiryFields(boe);

            List<Object> objects = new ArrayList<Object>();
            for (BusinessObject bo : results) {
                Object object = ObjectUtils.createNewObjectFromClass(boe.getBusinessObjectClass());
                for (String propertyName : inquiryFields) {
                    Object propertyValue = ObjectUtils.getPropertyValue(bo, propertyName);
                    Class<?> propertyType = ObjectUtils.getPropertyType(bo, propertyName, getPersistenceStructureService());
                    if (isPropertyTypeValid(propertyType)) {
                        if (!propertyName.contains(".")) {
                            ObjectUtils.setObjectProperty(object, propertyName, propertyValue);
                        }
                    }
                }

                objects.add(object);
            }

            String xml = null;
            if (objects.size() == 1) {
                xml = getXmlObjectSerializerService().toXml(objects.get(0));
            } else {
                xml = getXmlObjectSerializerService().toXml(objects).replaceAll(ListProxyDefaultImpl.class.getName(), List.class.getName());
            }

            return new ResponseEntity<String>(xml, HttpStatus.OK);
        } catch (Exception e) {
            LOG.debug("Unexpected exception has occured.", e);
            return new ResponseEntity<String>(generateErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(), request.getRequestURI(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected boolean isAuthorized(HttpServletRequest request, FinancialSystemBusinessObjectEntry boe) throws Exception {
        if (request != null && boe != null) {
            UserSession userSession = KRADUtils.getUserSessionFromRequest(request);
            if (userSession != null && GlobalVariables.getUserSession().getKualiSessionId().equals(userSession.getKualiSessionId())) {
                Class businessObjectClass = boe.getBusinessObjectClass();
                return getPermissionService().isAuthorizedByTemplate(GlobalVariables.getUserSession().getPrincipalId(), KRADConstants.KNS_NAMESPACE, KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS, KRADUtils.getNamespaceAndComponentSimpleName(businessObjectClass), Collections.<String, String> emptyMap());
            }
        }

        return false;
    }

    protected String generateErrorResponse(String statusCode, String uri, String desc) {
        return generateErrorResponse(statusCode, uri, desc, null);
    }

    protected String generateErrorResponse(String statusCode, String uri, String desc, Exception e) {
        return statusCode + " - " + desc + ": " + uri + "\n" + (e != null ? ExceptionUtils.getStackTrace(e) : "");
    }

    private boolean isPropertyTypeValid(Class<?> propertyType) {
        if (propertyType != null && (TypeUtils.isStringClass(propertyType)
                || TypeUtils.isIntegralClass(propertyType)
                || TypeUtils.isDecimalClass(propertyType)
                || TypeUtils.isTemporalClass(propertyType)
                || TypeUtils.isBooleanClass(propertyType))) {
            return true;
        }

        return false;
    }

    private List<String> getInquiryFields(FinancialSystemBusinessObjectEntry boe) {
        List<String> inquiryFields = new ArrayList<String>();
        for (InquirySectionDefinition section : boe.getInquiryDefinition().getInquirySections()) {
            inquiryFields.addAll(section.getInquiryFieldNames());
        }
        return inquiryFields;
    }

    private List<? extends BusinessObject> getSearchResults(HttpServletRequest request, FinancialSystemBusinessObjectEntry boe) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        for (Object o : request.getParameterMap().keySet()) {
            String[] value = (String[]) request.getParameterMap().get(o);
            fieldValues.put(o.toString(), value[0]);
        }

        LookupableHelperService lookupableHelperService = getLookupableHelperService(boe.getLookupDefinition().getLookupableID());
        lookupableHelperService.setBusinessObjectClass(boe.getBusinessObjectClass());

        List<? extends BusinessObject> results = lookupableHelperService.getSearchResults(fieldValues);
        return results;
    }

    private HttpStatus validateRequest(FinancialSystemBusinessObjectEntry boe, String namespace, String dataobject, HttpServletRequest request) throws Exception {
        // check for https (will be ignored in dev mode), authorization
        if ((!ConfigContext.getCurrentContextConfig().getDevMode() && !request.isSecure()) || !isAuthorized(request, boe)) {
            return HttpStatus.FORBIDDEN;
        }

        if (boe == null) {
            return HttpStatus.BAD_REQUEST;
        }

        Boolean isModuleLocked = getParameterService().getParameterValueAsBoolean(namespace, KfsParameterConstants.PARAMETER_ALL_DETAIL_TYPE, KRADConstants.SystemGroupParameterNames.OLTP_LOCKOUT_ACTIVE_IND);
        if (isModuleLocked || !boe.hasInquiryDefinition()) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.OK;
    }

    private FinancialSystemBusinessObjectEntry getBusinessObject(String dataobject) {
        FinancialSystemBusinessObjectEntry boe = null;
        try {
            boe = (FinancialSystemBusinessObjectEntry) getDataDictionaryService().getDictionaryObject(dataobject);
        } catch (NoSuchBeanDefinitionException e) {
            LOG.debug("Failed to retrieve data dictionary object.", e);
        }
        return boe;
    }

    protected LookupableHelperService getLookupableHelperService(String lookupableID) {
        LookupableHelperService lookupableHelperService = LookupableSpringContext.getLookupable(lookupableID).getLookupableHelperService();

        return lookupableHelperService;
    }

    @Override
    public DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        }
        return dataDictionaryService;
    }

    @Override
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public PersistenceStructureService getPersistenceStructureService() {
        if (persistenceStructureService == null) {
            persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);
        }
        return persistenceStructureService;
    }

    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    public ParameterService getParameterService() {
        if (parameterService == null) {
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

    public PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = KimApiServiceLocator.getPermissionService();
        }
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

}
