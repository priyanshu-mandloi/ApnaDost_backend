package com.symptocare.backend.service;

import com.symptocare.backend.dto.ExpenseFilterRequest;
import com.symptocare.backend.dto.ExpenseRequest;
import com.symptocare.backend.dto.ExpenseResponse;
import com.symptocare.backend.dto.ExpenseSummaryResponse;
import com.symptocare.backend.model.Expense;
import com.symptocare.backend.model.Expense.ExpenseCategory;
import com.symptocare.backend.model.User;
import com.symptocare.backend.repository.ExpenseRepository;
import com.symptocare.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Create expense
    public ExpenseResponse create(String email, ExpenseRequest request) {
        User user = getUser(email);

        Expense expense = Expense.builder()
                .user(user)
                .amount(request.getAmount())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .paymentMethod(request.getPaymentMethod())
                .expenseDate(request.getExpenseDate())
                .isRecurring(request.isRecurring())
                .build();

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    // Get all expenses
    public List<ExpenseResponse> getAll(String email) {
        User user = getUser(email);
        return expenseRepository.findByUserOrderByExpenseDateDesc(user)
                .stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }

    // Get expenses with filters
    public List<ExpenseResponse> getFiltered(String email, ExpenseFilterRequest filter) {
        User user = getUser(email);
        return expenseRepository.findWithFilters(
                        user,
                        filter.getMonth(),
                        filter.getYear(),
                        filter.getCategory(),
                        filter.getPaymentMethod(),
                        filter.getMinAmount(),
                        filter.getMaxAmount(),
                        filter.getIsRecurring(),
                        filter.getSearch()
                )
                .stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }

    // Get expenses by month and year
    public List<ExpenseResponse> getByMonthYear(String email, Integer month, Integer year) {
        User user = getUser(email);
        return expenseRepository
                .findByUserAndExpenseMonthAndExpenseYearOrderByExpenseDateDesc(user, month, year)
                .stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }

    // Get monthly summary with category breakdown
    public ExpenseSummaryResponse getMonthlySummary(String email, Integer month, Integer year) {
        User user = getUser(email);

        BigDecimal total = expenseRepository.sumByUserAndMonthAndYear(user, month, year);
        long count = expenseRepository.countByUserAndExpenseMonthAndExpenseYear(user, month, year);

        // Build category breakdown
        List<Object[]> rawBreakdown = expenseRepository.categoryBreakdownByMonthYear(user, month, year);
        Map<ExpenseCategory, BigDecimal> breakdown = new EnumMap<>(ExpenseCategory.class);
        Map<ExpenseCategory, String> breakdownFormatted = new EnumMap<>(ExpenseCategory.class);

        for (Object[] row : rawBreakdown) {
            ExpenseCategory cat = (ExpenseCategory) row[0];
            BigDecimal catTotal = (BigDecimal) row[1];
            breakdown.put(cat, catTotal);
            breakdownFormatted.put(cat, "₹" + String.format("%,.2f", catTotal));
        }

        // Get highest expense this month
        List<Expense> monthExpenses = expenseRepository
                .findByUserAndExpenseMonthAndExpenseYearOrderByExpenseDateDesc(user, month, year);

        ExpenseResponse highest = monthExpenses.stream()
                .max((a, b) -> a.getAmount().compareTo(b.getAmount()))
                .map(ExpenseResponse::from)
                .orElse(null);

        ExpenseSummaryResponse summary = new ExpenseSummaryResponse();
        summary.setTotalAmount(total);
        summary.setTotalFormatted("₹" + String.format("%,.2f", total));
        summary.setTotalTransactions(count);
        summary.setCategoryBreakdown(breakdown);
        summary.setCategoryBreakdownFormatted(breakdownFormatted);
        summary.setHighestExpense(highest);
        summary.setMonth(month);
        summary.setYear(year);

        return summary;
    }

    // Get yearly summary
    public ExpenseSummaryResponse getYearlySummary(String email, Integer year) {
        User user = getUser(email);

        BigDecimal total = expenseRepository.sumByUserAndYear(user, year);

        List<Expense> yearExpenses = expenseRepository
                .findByUserAndExpenseYearOrderByExpenseDateDesc(user, year);

        long count = yearExpenses.size();

        ExpenseResponse highest = yearExpenses.stream()
                .max((a, b) -> a.getAmount().compareTo(b.getAmount()))
                .map(ExpenseResponse::from)
                .orElse(null);

        ExpenseSummaryResponse summary = new ExpenseSummaryResponse();
        summary.setTotalAmount(total);
        summary.setTotalFormatted("₹" + String.format("%,.2f", total));
        summary.setTotalTransactions(count);
        summary.setHighestExpense(highest);
        summary.setYear(year);

        return summary;
    }

    // Get distinct years for history dropdown
    public List<Integer> getAvailableYears(String email) {
        User user = getUser(email);
        return expenseRepository.findDistinctYearsByUser(user);
    }

    // Update expense
    public ExpenseResponse update(String email, Long id, ExpenseRequest request) {
        User user = getUser(email);
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setAmount(request.getAmount());
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setRecurring(request.isRecurring());

        // Recalculate month and year from updated date
        expense.setExpenseMonth(request.getExpenseDate().getMonthValue());
        expense.setExpenseYear(request.getExpenseDate().getYear());

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    // Delete expense
    public void delete(String email, Long id) {
        User user = getUser(email);
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expenseRepository.delete(expense);
    }

    // Quick stats for current month
    public Map<String, Object> getCurrentMonthStats(String email) {
        User user = getUser(email);
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        BigDecimal total = expenseRepository.sumByUserAndMonthAndYear(user, month, year);
        long count = expenseRepository.countByUserAndExpenseMonthAndExpenseYear(user, month, year);

        Map<String, Object> stats = new HashMap<>();
        stats.put("month", month);
        stats.put("year", year);
        stats.put("total", total);
        stats.put("totalFormatted", "₹" + String.format("%,.2f", total));
        stats.put("totalTransactions", count);
        return stats;
    }
}