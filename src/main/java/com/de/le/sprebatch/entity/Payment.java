package com.de.le.sprebatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Data // Generates getters, setters, toString, equals, and hashCode via Lombok
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, length = 255)
    private String paymentId;

    @Column(name = "payment_type", length = 255)
    private String paymentType;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_status", length = 255)
    private String paymentStatus;

    @Column(name = "amount")
    private Integer amount;

}
