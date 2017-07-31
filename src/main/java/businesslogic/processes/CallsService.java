package businesslogic.processes;


import dto.site.CallRecordRequest;
import dto.site.CallRequest;
import dto.site.ContactsAddingRequest;
import dto.site.Result;
import org.springframework.stereotype.Service;

/**
 * Created by berz on 14.03.2017.
 */
@Service
public interface CallsService {
    Result newCallFromWebhook(CallRequest callRequest);

    Result newCallRecords(CallRecordRequest callRecordRequest);

    Result newContactsAddedInCRM(ContactsAddingRequest contactsAddingRequest);
}
