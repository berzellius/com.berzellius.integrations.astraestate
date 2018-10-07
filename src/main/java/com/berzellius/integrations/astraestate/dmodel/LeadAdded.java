package com.berzellius.integrations.astraestate.dmodel;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by berz on 25.06.2017.
 */
@Entity(name = "LeadAdded")
@Table(
        name = "lead_added"
)
@Access(AccessType.FIELD)
public class LeadAdded extends DModelEntity {
    /*
     * Состояние в контексте обработки
     */
    public static enum State {
        NEW,
        DONE
    }

    public LeadAdded() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "call_id_generator")
    @SequenceGenerator(name = "call_id_generator", sequenceName = "call_id_seq")
    @NotNull
    @Column(updatable = false, insertable = false, columnDefinition = "bigint")
    private Long id;

    @Column(name = "lead_id")
    protected Long leadId;

    @Enumerated(EnumType.STRING)
    protected State state;

    @Override
    public Long getId() {
        return id;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LeadAdded) &&
                this.getId().equals(((LeadAdded) obj).getId());
    }

    @Override
    public String toString() {
        return "LeadAdded#".concat(this.getId().toString());
    }
}
