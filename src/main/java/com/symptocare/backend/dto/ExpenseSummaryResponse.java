package com.symptocare.backend.dto;

import com.symptocare.backend.model.Expense.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseSummaryResponse {

    private BigDecimal totalAmount;
    private String totalFormatted;
    private long totalTransactions;

    // Breakdown by category → total amount per category
    private Map<ExpenseCategory, BigDecimal> categoryBreakdown;

    // Breakdown by category → formatted strings
    private Map<ExpenseCategory, String> categoryBreakdownFormatted;

    // Highest expense this period
    private ExpenseResponse highestExpense;

    // Month and year this summary is for
    private Integer month;
    private Integer year;
}