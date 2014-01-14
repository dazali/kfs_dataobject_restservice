package org.kuali.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.kuali.kfs.sys.businessobject.datadictionary.FinancialSystemBusinessObjectEntry;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.DataObjectRestService;
import org.kuali.rice.core.api.util.type.TypeUtils;
import org.kuali.rice.kns.datadictionary.InquirySectionDefinition;
import org.kuali.rice.kns.lookup.LookupableHelperService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.service.DataDictionaryService;
import org.kuali.rice.krad.service.PersistenceStructureService;
import org.kuali.rice.krad.service.XmlObjectSerializerService;
import org.kuali.rice.krad.util.ObjectUtils;

public class DataObjectRestServiceImpl implements DataObjectRestService {

	private DataDictionaryService dataDictionaryService;
	private PersistenceStructureService persistenceStructureService;

	protected enum DataType {
	    JSON, XML
	}

    @Override
    public Response getDataObjects(String namespace, String dataobject, String type, UriInfo info) throws Exception {

        Map<String, String> fieldValues = new HashMap<String, String>();
        if (info != null) {
            for (String key : info.getQueryParameters().keySet()) {
                fieldValues.put(key, info.getQueryParameters().getFirst(key));
            }
        }

        FinancialSystemBusinessObjectEntry boe = (FinancialSystemBusinessObjectEntry) getDataDictionaryService().getDictionaryObject(dataobject);

        LookupableHelperService lookupableHelperService = getLookupableHelperService(boe.getLookupDefinition().getLookupableID());
        lookupableHelperService.setBusinessObjectClass(boe.getBusinessObjectClass());

        List<? extends BusinessObject> results = lookupableHelperService.getSearchResults(fieldValues);

        List<String> inquiryFields = new ArrayList<String>();
        for (InquirySectionDefinition section : boe.getInquiryDefinition().getInquirySections()) {
            inquiryFields.addAll(section.getInquiryFieldNames());
        }

        List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();
        List objects = new ArrayList();

        DataType dataType = DataType.valueOf(type.toUpperCase());

        for (BusinessObject bo : results) {
            Map<String, Object> objectMap = new HashMap<String, Object>();
            Object object = ObjectUtils.createNewObjectFromClass(boe.getBusinessObjectClass());
            for (String propertyName : inquiryFields) {
                Object propertyValue = ObjectUtils.getPropertyValue(bo, propertyName);
                Class propertyType = ObjectUtils.getPropertyType(bo, propertyName, getPersistenceStructureService());
                if (propertyType != null && !propertyName.contains(".") &&
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
                            ObjectUtils.setObjectProperty(object, propertyName, propertyValue);
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
                String jsonData = mapper.defaultPrettyPrintingWriter().writeValueAsString(resultMap);
                jsonData = jsonData.replaceFirst("ArrayList", "ArrayList<"+boe.getBusinessObjectClass().getName()+">");

                return Response.ok(jsonData, MediaType.APPLICATION_JSON).build();
            case XML:
                return Response.ok(getXmlObjectSerializerService().toXml(objects), MediaType.APPLICATION_XML).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Data object not found.").build();
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

    public XmlObjectSerializerService getXmlObjectSerializerService() {
        return SpringContext.getBean(XmlObjectSerializerService.class);
    }
}
