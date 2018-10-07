package com.berzellius.integrations.astraestate.businesslogic.rules.transformer;

import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMContact;
import com.berzellius.integrations.astraestate.businesslogic.rules.exceptions.TransformationException;
import org.junit.Assert;

import java.util.HashMap;

/**
 * Created by berz on 07.06.2017.
 */
public class AmoCRMContactFromCallTransformerProcessor implements TransformerProcessor {

    protected HashMap<String, Object> params;

    @Override
    public String transform(String input) throws TransformationException {
        throw new TransformationException("string transformation not allowed for LeadFromSiteTransformer");
    }

    @Override
    public <T> T transform(T input) throws TransformationException {
        Assert.assertTrue("input object must be AmoCRMContact!", input instanceof AmoCRMContact);
        AmoCRMContact contact = (AmoCRMContact) input;

        // если нет признаков обработки звонка
        if(!this.getParams().containsKey("virtual_number")) {
            return (T) contact;
        }

        String source = this.getStringParam("source");
        Long sourceContactsCustomField = this.getLongParam("sourceContactsCustomField");

        System.out.println("Source is <" + source + ">");
        if(source != null && source.contains("CIAN")){
            Assert.assertTrue("SourceContactsCustomField MUST be set!", sourceContactsCustomField != null);
            //1228553 - CIAN
            String[] src = {"1228553"};
            contact.setEmptyValueToField(sourceContactsCustomField);
            contact.addStringValuesToCustomField(sourceContactsCustomField, src);
        }

        return (T) contact;
    }

    protected Long getLongParam(String name){
        Object param = this.getParam(name);
        if(param == null)
            return null;

        return (Long) param;
    }

    protected String getStringParam(String name){
        Object param = this.getParam(name);
        if(param == null)
            return null;

        return (String) param;
    }

    protected Object getParam(String name){
        return (this.getParams().containsKey(name))? this.getParams().get(name) : null;
    }

    @Override
    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }
}
