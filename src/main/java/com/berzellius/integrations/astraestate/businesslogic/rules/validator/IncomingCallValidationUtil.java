package com.berzellius.integrations.astraestate.businesslogic.rules.validator;


import com.berzellius.integrations.astraestate.businesslogic.rules.exceptions.ValidationException;
import com.berzellius.integrations.astraestate.dmodel.TrackedCall;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 11.01.2017.
 */
@Service
public interface IncomingCallValidationUtil extends ValidationUtil {
    public boolean validate(TrackedCall call) throws ValidationException;

    void setSimpleFieldsValidationUtil(SimpleFieldsValidationUtil simpleFieldsValidationUtil);
}
