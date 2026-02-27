package com.symptocare.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Amount in Indian Rupees (₹) — BigDecimal for precision
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Title e.g. "Swiggy dinner", "Electricity bill"
    @Column(nullable = false)
    private String title;

    // Optional longer note
    @Column(columnDefinition = "TEXT")
    private String description;

    // Category for filtering/grouping
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    // Payment method
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // The date the expense happened (user sets this)
    @Column(nullable = false)
    private LocalDate expenseDate;

    // Derived fields for fast filtering — set automatically
    @Column(nullable = false)
    private Integer expenseMonth;   // 1–12

    @Column(nullable = false)
    private Integer expenseYear;    // e.g. 2025

    // Is this a recurring expense? e.g. Netflix subscription
    @Column(nullable = false)
    private boolean isRecurring;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // Auto-extract month and year from expenseDate for easy filtering
        if (this.expenseDate != null) {
            this.expenseMonth = this.expenseDate.getMonthValue();
            this.expenseYear = this.expenseDate.getYear();
        }
    }

    public enum ExpenseCategory {
        FOOD,           // Groceries, restaurants, Swiggy/Zomato
        TRANSPORT,      // Fuel, Ola, Uber, auto
        ENTERTAINMENT,  // Movies, OTT, games
        SHOPPING,       // Clothes, Amazon, Flipkart
        HEALTH,         // Medicine, doctor, gym
        UTILITIES,      // Electricity, internet, phone recharge
        EDUCATION,      // Courses, books, fees
        RENT,           // House rent, PG
        SAVINGS,        // SIP, FD, investment
        OTHER
    }

    public enum PaymentMethod {
        CASH,
        UPI,            // GPay, PhonePe, Paytm
        CREDIT_CARD,
        DEBIT_CARD,
        NET_BANKING,
        OTHER
    }
}
