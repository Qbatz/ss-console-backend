package com.smartstay.console.dao;

import com.smartstay.console.converters.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CustomerActivities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    private String customerId;
    private String hostelId;
    // from customer activity type enum
    private String activityType;
    private String description;
    // from customer activity source enum
    private String source;
    private String sourceId;
    // from user type enum
    private String userType;
    private Date createdAt;
    private String platform;
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> sourceIds;
}
