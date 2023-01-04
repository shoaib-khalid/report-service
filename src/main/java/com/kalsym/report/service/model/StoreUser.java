package com.kalsym.report.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "store_user")
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class StoreUser implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @NotBlank(message = "storeId is required")
    private String storeId;


    @JsonIgnore
    @NotBlank(message = "username is required")
    private String username;

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String name;

    private String phoneNumber;

    private String email;
    @JsonIgnore
    private Boolean locked;
    @JsonIgnore
    private Boolean deactivated;
    Date created;
    Date updated;
    @JsonIgnore
    private String roleId;
    @JsonIgnore
    private String fcmToken;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false, nullable = true)
    private List<StoreShiftSummary> shiftSummaries;
}