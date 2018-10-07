package com.berzellius.integrations.astraestate.service;

import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMAccount;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMAccountUser;
import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.basic.exception.APIAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by berz on 29.03.2018.
 */
@Service
public class AmoCRMAccountInfoServiceImpl implements AmoCRMAccountInfoService {

    protected AmoCRMAccount account = null;

    @Autowired
    protected AmoCRMService amoCRMService;

    public List<AmoCRMAccountUser> getAmoCRMAccountUsers() throws APIAuthException {
        return (this.getCurrentAccount() != null)? this.getCurrentAccount().getUsers() : null;
    }

    public AmoCRMAccount getCurrentAccount() throws APIAuthException {
        if(this.account == null){
            this.account = amoCRMService.getCurrentAccount();
        }

        return account;
    }

}
