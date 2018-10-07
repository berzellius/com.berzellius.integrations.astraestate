package com.berzellius.integrations.astraestate.service;

import com.berzellius.integrations.astraestate.dmodel.Site;
import com.berzellius.integrations.astraestate.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by berz on 07.08.2017.
 */
@Service
@Transactional
public class SiteServiceImpl implements SiteService {

    @Autowired
    SiteRepository siteRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<Site> getSitesByUrl(String url){
        try {
            url = "http://".concat(getDomainName(url));
            System.out.println("will check " + url);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery();
            Root<Site> siteRoot = criteriaQuery.from(Site.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(siteRoot.get("url"), this.cutSlashInTail(url)));
            //System.out.println("check url:" + this.cutSlashInTail(url));
            predicates.add(criteriaBuilder.equal(siteRoot.get("url"), this.cutSlashInTail(url.replace("/www.", "/"))));
            //System.out.println("check url:" + this.cutSlashInTail(url.replace("/www.", "/")));
            predicates.add(criteriaBuilder.equal(siteRoot.get("url"), this.cutSlashInTail(url.replace("://", "://www."))));
            //System.out.println("check url:" + this.cutSlashInTail(url.replace("://", "://www.")));

            criteriaQuery.select(siteRoot)
                    .where(
                            criteriaBuilder.or(predicates.toArray(new Predicate[]{}))
                    );

            List<Site> sites = entityManager.createQuery(criteriaQuery).getResultList();
            return  sites;
        } catch (URISyntaxException e) {
            System.out.println("URI syntax error!");
            return null;
        }
    }

    @Override
    public Site getSiteByUrl(String url){
        List<Site> sites = this.getSitesByUrl(url);
        return (sites != null && sites.size() > 0)? sites.get(0) : null;
    }

    @Override
    public List<Site> getSitesByUrlAndPassword(String url, String password){
        try {
            url = "http://".concat(getDomainName(url));
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery();
            Root<Site> siteRoot = criteriaQuery.from(Site.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(siteRoot.get("url"), this.cutSlashInTail(url)));
            System.out.println("check url:" + this.cutSlashInTail(url));
            predicates.add(criteriaBuilder.equal(siteRoot.get("url"), this.cutSlashInTail(url.replace("/www.", "/"))));
            System.out.println("check url:" + this.cutSlashInTail(url.replace("/www.", "/")));
            predicates.add(criteriaBuilder.equal(siteRoot.get("url"), this.cutSlashInTail(url.replace("://", "://www."))));
            System.out.println("check url:" + this.cutSlashInTail(url.replace("://", "://www.")));

            criteriaQuery.select(siteRoot)
                    .where(
                            criteriaBuilder.and(
                                criteriaBuilder.or(predicates.toArray(new Predicate[]{})),
                                   criteriaBuilder.equal(siteRoot.get("password"), password)
                            )
                    );

            List<Site> sites = entityManager.createQuery(criteriaQuery).getResultList();
            return  sites;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public Site findByUrlAndPassword(String origin, String password) {
        List<Site> sites = this.getSitesByUrlAndPassword(origin, password);
        if(sites.size() == 0){
            return null;
        }

        Site site = sites.get(0);
        return site;
    }

    @Override
    public String cutSlashInTail(String url){
        if(url.endsWith("/")){
            return url.substring(0, url.length() - 1);
        }

        return url;
    }

    public static String getDomainName(String url) throws URISyntaxException {
        url = url.substring(0, url.indexOf("?") > 0 ?  url.indexOf("?") : url.length());
        System.out.println(url);
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
