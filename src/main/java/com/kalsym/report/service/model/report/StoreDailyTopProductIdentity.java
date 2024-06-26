package com.kalsym.report.service.model.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.kalsym.report.service.model.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Sarosh
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreDailyTopProductIdentity implements Serializable {

    private Date date;
    private String productId;
    private Store store;

    public StoreDailyTopProductIdentity(Date date, String productId) {
        this.date = date;
        this.productId = productId;
    }


    public StoreDailyTopProductIdentity(Date date, String productId, Store store) {
        this.date = date;
        this.productId = productId;
        this.store = store;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.date);
        hash = 79 * hash + Objects.hashCode(this.productId);
        hash = 79 * hash + Objects.hashCode(this.store);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StoreDailyTopProductIdentity other = (StoreDailyTopProductIdentity) obj;
        if (!Objects.equals(this.productId, other.productId)) {
            return false;
        }
        if (!Objects.equals(this.store, other.store)) {
            return false;
        }
        if (!Objects.equals(this.date, other.date)) {
            return false;
        }
        return true;
    }

}
