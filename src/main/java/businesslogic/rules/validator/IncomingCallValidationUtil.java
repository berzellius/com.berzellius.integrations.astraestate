package businesslogic.rules.validator;


import businesslogic.rules.exceptions.ValidationException;
import dmodel.TrackedCall;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 11.01.2017.
 */
@Service
public interface IncomingCallValidationUtil extends ValidationUtil {
    public boolean validate(TrackedCall call) throws ValidationException;

    void setSimpleFieldsValidationUtil(SimpleFieldsValidationUtil simpleFieldsValidationUtil);
}
