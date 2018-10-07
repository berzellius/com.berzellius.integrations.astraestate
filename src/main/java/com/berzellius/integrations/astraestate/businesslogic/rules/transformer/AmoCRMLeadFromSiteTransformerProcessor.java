package com.berzellius.integrations.astraestate.businesslogic.rules.transformer;

import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMLead;
import com.berzellius.integrations.astraestate.businesslogic.rules.exceptions.TransformationException;
import org.junit.Assert;

import java.util.HashMap;

/**
 * Created by berz on 07.06.2017.
 */
public class AmoCRMLeadFromSiteTransformerProcessor implements TransformerProcessor {

    protected HashMap<String, Object> params;

    @Override
    public String transform(String input) throws TransformationException {
        throw new TransformationException("string transformation not allowed for LeadFromSiteTransformer");
    }

    @Override
    public <T> T transform(T input) throws TransformationException {
        Assert.assertTrue("input object must be AmoCRMLead!", input instanceof AmoCRMLead);
        AmoCRMLead lead = (AmoCRMLead) input;

        if(!this.getParams().containsKey("site")) {
            return (T) lead;
        }



        return (T) lead;
    }

    @Override
    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }
}
