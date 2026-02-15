package com.smartstay.console.dao;

import com.smartstay.console.handlers.ChangesMapConverter;
import com.smartstay.console.handlers.GenericMapConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AgentActivities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    private String agentId;
    private String activityType;
    private String description;
    private String source;
    private String sourceId;

    @Convert(converter = GenericMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> oldObject;

    @Convert(converter = GenericMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> newObject;

    @Convert(converter = ChangesMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Map<String, Object>> changesJson;

    private Date createdAt;
}
