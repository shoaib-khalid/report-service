package com.kalsym.report.service.model;

import java.io.Serializable;
import javax.persistence.*;

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
@Table(name = "store")
@ToString
public class Store implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;

    private String address;
    private String city;
    private String postcode;
    private String state;
    private String email;
    private String phone;
    private String verticalCode;

    private String regionCountryId;

    private String clientId;

    public String getNameAbreviation() {
        String abbreviation = "";

        if (name.length() <= 3) {
            abbreviation = name;
        } else {
            String[] myName = name.split(" ");

            for (int i = 0; i < myName.length; i++) {
                String s = myName[i];
                abbreviation = abbreviation + s.charAt(0);

                if (abbreviation.length() == 3) {
                    break;
                }
            }
        }
        return abbreviation;
    }

}
