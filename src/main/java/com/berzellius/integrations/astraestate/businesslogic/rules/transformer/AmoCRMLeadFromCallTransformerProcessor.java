package com.berzellius.integrations.astraestate.businesslogic.rules.transformer;

import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMLead;
import com.berzellius.integrations.astraestate.businesslogic.rules.exceptions.TransformationException;
import org.junit.Assert;

import java.util.HashMap;

/**
 * Created by berz on 07.06.2017.
 */
public class AmoCRMLeadFromCallTransformerProcessor implements TransformerProcessor {

    protected HashMap<String, Object> params;

    @Override
    public String transform(String input) throws TransformationException {
        throw new TransformationException("string transformation not allowed for LeadFromSiteTransformer");
    }

    @Override
    public <T> T transform(T input) throws TransformationException {
        Assert.assertTrue("input object must be AmoCRMLead!", input instanceof AmoCRMLead);
        AmoCRMLead lead = (AmoCRMLead) input;

        // если нет признаков обработки звонка
        if(!this.getParams().containsKey("virtual_number")) {
            return (T) lead;
        }

        String source = this.getStringParam("source");
        Long sourceLeadsCustomField = this.getLongParam("sourceLeadsCustomField");

        System.out.println("Source is <" + source + ">");
        if(source != null && source.contains("CIAN")){
            System.out.println("cian processing..");
            // CIAN - особый случай
            lead.tag(266507l, "cian");

            Assert.assertTrue("SourceLeadsCustomField MUST be set!", sourceLeadsCustomField != null);
            //1228551 - CIAN
            String[] src = {"1228551"};
            lead.setEmptyValueToField(sourceLeadsCustomField);
            lead.addStringValuesToCustomField(sourceLeadsCustomField, src);
        }

        return (T) lead;
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
