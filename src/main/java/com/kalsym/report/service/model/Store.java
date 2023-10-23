
package com.kalsym.report.service.model;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "store")
public class Store implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;
    private String clientId;
    @JsonIgnore
    private String regionCountryStateId;
    @JsonIgnore
    private String regionCountryId;


    public String getNameAbreviation() {
        StringBuilder abbreviation = new StringBuilder();

        if (name.length() <= 3) {
            abbreviation = new StringBuilder(name);
        } else {
            String[] myName = name.split(" ");

            for (int i = 0; i < myName.length; i++) {
                String s = myName[i];
                abbreviation.append(s.charAt(0));

                if (abbreviation.length() == 3) {
                    break;
                }
            }
        }
        return abbreviation.toString();
    }
}

