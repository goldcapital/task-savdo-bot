package org.programming.tasksavdobot.domen;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Entity
@Table(name = "product")
@Data
public class ProductEntity {
    @Id
    private UUID id=UUID.randomUUID();
    private String description;
    private String imageUrl;
    private String type;
    private String additionalInfo;
    @Column(name = "name_uz",nullable = false)
    private String nameUz;

    @Column(name = "name_en",nullable = false)
    private String nameEn;

    @Column(name = "name_ru",nullable = false)
    private String nameRu;
    @Column(name = "visible")
    private Boolean visible=true;

    @Column(name = "profile_id")
    private Long profileId;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "profile", updatable = false, insertable = false)
    private ProfileEntity profileEntity;

}