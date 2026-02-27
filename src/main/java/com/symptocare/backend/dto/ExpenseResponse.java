package com.symptocare.backend.dto;

import com.symptocare.backend.model.Expense;
import com.symptocare.backend.model.Expense.ExpenseCategory;
import com.symptocare.backend.model.Expense.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseResponse {

    private Long id;
    private BigDecimal amount;
    private String amountFormatted;
    private String title;
    private String description;
    private ExpenseCategory category;
    private PaymentMethod paymentMethod;
    private LocalDate expenseDate;
    private Integer expenseMonth;
    private Integer expenseYear;
    private boolean isRecurring;
    private LocalDateTime createdAt;

    public static ExpenseResponse from(Expense expense) {
        ExpenseResponse res = new ExpenseResponse();
        res.setId(expense.getId());
        res.setAmount(expense.getAmount());
        res.setAmountFormatted("₹" + String.format("%,.2f", expense.getAmount()));
        res.setTitle(expense.getTitle());
        res.setDescription(expense.getDescription());
        res.setCategory(expense.getCategory());
        res.setPaymentMethod(expense.getPaymentMethod());
        res.setExpenseDate(expense.getExpenseDate());
        res.setExpenseMonth(expense.getExpenseMonth());
        res.setExpenseYear(expense.getExpenseYear());
        res.setRecurring(expense.isRecurring());
        res.setCreatedAt(expense.getCreatedAt());
        return res;
    }
}