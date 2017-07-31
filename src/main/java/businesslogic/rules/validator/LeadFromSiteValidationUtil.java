package businesslogic.rules.validator;

import businesslogic.rules.exceptions.ValidationException;
import dmodel.LeadFromSite;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 11.01.2017.
 */
@Service
public interface LeadFromSiteValidationUtil extends ValidationUtil {
    public boolean validate(LeadFromSite leadFromSite) throws ValidationException;

    void setSimpleFieldsValidationUtil(SimpleFieldsValidationUtil simpleFieldsValidationUtil);
}
