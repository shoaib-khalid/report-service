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
    private String city;
    private String address;
    private String clientId;
    private String postcode;
    private String state;
    private String contactName;
    private String phone;
    private String phoneNumber;
    private String email;
    private String verticalCode;

    private Double serviceChargesPercentage;

    private String paymentType;
    private Integer invoiceSeqNo;
//    private String invoicePrefix;

    @JsonIgnore
    private String regionCountryId;

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

