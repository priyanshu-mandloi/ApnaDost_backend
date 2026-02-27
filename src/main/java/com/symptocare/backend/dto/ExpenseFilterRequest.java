package com.symptocare.backend.dto;

import com.symptocare.backend.model.Expense.ExpenseCategory;
import com.symptocare.backend.model.Expense.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExpenseFilterRequest {

    // Filter by month (1-12) and year
    private Integer month;
    private Integer year;

    // Filter by category
    private ExpenseCategory category;

    // Filter by payment method
    private PaymentMethod paymentMethod;

    // Filter by amount range in rupees
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Filter recurring only
    private Boolean isRecurring;

    // Search by title
    private String search;
}