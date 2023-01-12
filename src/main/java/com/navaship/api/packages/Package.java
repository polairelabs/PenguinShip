package com.navaship.api.packages;

import com.google.gson.JsonObject;
import com.navaship.api.appuser.AppUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    private String name;
    private BigDecimal weight;
    private BigDecimal value;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser user;
    @Column(updatable = false)

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public Map<String, Object> toPackageMap() {
        Map<String, Object> packageMap = new HashMap<>();
        packageMap.put("name", name);
        packageMap.put("weight", weight);
        packageMap.put("height", height);
        packageMap.put("width", width);
        packageMap.put("length", length);
        return packageMap;
    }

//    public JsonObject additionalInfoToJson() {
//        JsonObject item = new JsonObject();
//        item.addProperty("name", name);
//        item.addProperty("company", company);
//        item.addProperty("phone", phone);
//        item.addProperty("email", email);
//        return item;
//    }
}
