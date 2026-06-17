package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Rooms.Room;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "hotel")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;


    @Column(columnDefinition = "TEXT[]")
    private String[]photos;

    @Column(columnDefinition = "TEXT[]")
    private String[] amenities;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private  LocalDateTime updatedAt;

    @Embedded
    private HotelContactInfo hotelContactInfo;

    @Column(nullable = false)
    private boolean isActive;

    @OneToMany(
            mappedBy = "hotel",
            cascade = { CascadeType.PERSIST, CascadeType.MERGE },
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Room> rooms = new ArrayList<>();

    @ManyToOne(optional = false)
    private User owner;

}
