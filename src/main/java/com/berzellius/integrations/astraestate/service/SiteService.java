package com.berzellius.integrations.astraestate.service;

import com.berzellius.integrations.astraestate.dmodel.Site;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by berz on 07.08.2017.
 */
@Service
public interface SiteService {
    List<Site> getSitesByUrl(String url);

    Site getSiteByUrl(String url);

    List<Site> getSitesByUrlAndPassword(String url, String password);

    Site findByUrlAndPassword(String origin, String password);

    String cutSlashInTail(String url);
}
