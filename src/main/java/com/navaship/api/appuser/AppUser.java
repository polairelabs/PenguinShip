package com.navaship.api.appuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.activity.ActivityLog;
import com.navaship.api.address.Address;
import com.navaship.api.auth.AuthViews;
import com.navaship.api.packages.Package;
import com.navaship.api.shipment.Shipment;
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@Entity
public class AppUser implements UserDetails {
    @Id
    @JsonView(AuthViews.Default.class)
    @GeneratedValue(generator = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;
    @JsonView(AuthViews.Default.class)
    private String firstName;
    @JsonView(AuthViews.Default.class)
    private String lastName;
    @JsonView(AuthViews.Default.class)
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String phoneNumber;
    private String city;
    private String state;
    private String address;
    @Enumerated(EnumType.STRING)
    @JsonView(AuthViews.Default.class)
    @Column(nullable = false)
    private AppUserRoleEnum role;
    private Boolean locked;
    private Boolean enabled;
    @OneToOne
    private Address defaultSourceAddress;

    /*
        Bidirectional mapping:
        - AppUser will have access to his list of Shipments
        - From Shipment, we will have access to user who "owns" that Shipment

        CascadeType.ALL means that any change which happens on AppUser must cascade to Shipment as well.
        - If you save an AppUser, then all associated Shipment will also be saved into database
        - If you delete an AppUser then all Shipments associated with that AppUser will also be deleted
    */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Shipment> shipments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Package> packages = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ActivityLog> activities = new ArrayList<>();

    @JsonView(AuthViews.Default.class)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private SubscriptionDetail subscriptionDetail;

    public AppUser(String email, String password, String firstName, String lastName, String phoneNumber, String city, String state, String address, AppUserRoleEnum role) {
        // Used to create complete (unverified) user with profile information + email and password
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.state = state;
        this.address = address;
        this.role = role;
        this.locked = true;
        this.enabled = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
