package com.smartstay.console.dao;

import jakarta.persistence.*;
import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class HostelImages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String imageUrl;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "hostel_id")
    private HostelV1 hostel;
}
