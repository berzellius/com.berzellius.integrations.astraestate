package com.berzellius.integrations.astraestate.dto.site;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by berz on 15.06.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lead implements Serializable {

    public Lead() {
    }

    public final CleanLead toCleanLead(){
        CleanLead cleanLead = new CleanLead();
        cleanLead.setName(
                this.getName() != null ?
                        this.getName() :
                            this.getName1() != null ?
                                    this.getName1() :
                                        this.getImya() != null ?
                                                this.getImya() : null
        );

        cleanLead.setEmail(
                this.getEmail() != null ?
                        this.getEmail() :
                            this.getEmail1() != null ?
                                    this.getEmail1() :
                                        this.getE_mail() != null ? this.getE_mail() : null
        );

        cleanLead.setPhone(
                this.getPhone() != null ?
                        this.getPhone() :
                            this.getPhone1() != null ?
                                    this.getPhone1() :
                                        this.getTelefon() != null ?
                                                this.getTelefon() : null
        );

        cleanLead.setComment(this.getComment());
        cleanLead.setOrigin(this.getOrigin());
        cleanLead.setParam(this.getParam());
        cleanLead.setPloshad(this.getPloshad());
        cleanLead.setReferer(this.getReferer());
        cleanLead.setSubject(this.getSubject());
        cleanLead.setUtm_content(this.getUtm_content());
        cleanLead.setUtm_campaign(this.getUtm_campaign());
        cleanLead.setUtm_medium(this.getUtm_medium());
        cleanLead.setUtm_source(this.getUtm_source());
        cleanLead.setUtm_term(this.getUtm_term());

        return cleanLead;
    }

    /**
    * Поля заказа
    */
    private String name;
    @JsonProperty("NAME")
    private String name1;
    private String Imya;
    private String email;
    @JsonProperty("EMAIL")
    private String email1;
    private String phone;
    @JsonProperty("PHONE")
    private String phone1;
    private String Telefon;
    private String comment;
    @JsonProperty("e-mail")
    private String e_mail;
    private String ploshad;
    private String param;
    private String subject;
    private String zadanie;
    private String referer;
    private String origin;
    /**
    * Utm
    */
    private String utm_source;
    private String utm_medium;
    private String utm_content;
    private String utm_campaign;
    private String utm_term;

    /**
     * roistat_visit
     */
    private Long roistat_visit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUtm_source() {
        return utm_source;
    }

    public void setUtm_source(String utm_source) {
        this.utm_source = utm_source;
    }

    public String getUtm_medium() {
        return utm_medium;
    }

    public void setUtm_medium(String utm_medium) {
        this.utm_medium = utm_medium;
    }

    public String getUtm_content() {
        return utm_content;
    }

    public void setUtm_content(String utm_content) {
        this.utm_content = utm_content;
    }

    public String getUtm_campaign() {
        return utm_campaign;
    }

    public void setUtm_campaign(String utm_campaign) {
        this.utm_campaign = utm_campaign;
    }

    public String getUtm_term() {
        return utm_term;
    }

    public void setUtm_term(String utm_term) {
        this.utm_term = utm_term;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getE_mail() {
        return e_mail;
    }

    public void setE_mail(String e_mail) {
        this.e_mail = e_mail;
    }

    public String getPloshad() {
        return ploshad;
    }

    public void setPloshad(String ploshad) {
        this.ploshad = ploshad;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getZadanie() {
        return zadanie;
    }

    public void setZadanie(String zadanie) {
        this.zadanie = zadanie;
    }

    public String getImya() {
        return Imya;
    }

    public void setImya(String imya) {
        Imya = imya;
    }

    public String getTelefon() {
        return Telefon;
    }

    public void setTelefon(String telefon) {
        Telefon = telefon;
    }

    public Long getRoistat_visit() {
        return roistat_visit;
    }

    public void setRoistat_visit(Long roistat_visit) {
        this.roistat_visit = roistat_visit;
    }
}
