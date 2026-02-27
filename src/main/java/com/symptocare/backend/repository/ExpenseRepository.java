package com.symptocare.backend.repository;

import com.symptocare.backend.model.Expense;
import com.symptocare.backend.model.Expense.ExpenseCategory;
import com.symptocare.backend.model.Expense.PaymentMethod;
import com.symptocare.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // All expenses for user sorted by date desc
    List<Expense> findByUserOrderByExpenseDateDesc(User user);

    // Filter by month and year
    List<Expense> findByUserAndExpenseMonthAndExpenseYearOrderByExpenseDateDesc(
            User user, Integer month, Integer year);

    // Filter by year only
    List<Expense> findByUserAndExpenseYearOrderByExpenseDateDesc(
            User user, Integer year);

    // Filter by category
    List<Expense> findByUserAndCategoryOrderByExpenseDateDesc(
            User user, ExpenseCategory category);

    // Filter by month, year and category
    List<Expense> findByUserAndExpenseMonthAndExpenseYearAndCategoryOrderByExpenseDateDesc(
            User user, Integer month, Integer year, ExpenseCategory category);

    // Filter by payment method
    List<Expense> findByUserAndPaymentMethodOrderByExpenseDateDesc(
            User user, PaymentMethod paymentMethod);

    // Filter recurring
    List<Expense> findByUserAndIsRecurringOrderByExpenseDateDesc(
            User user, boolean isRecurring);

    // Security check
    Optional<Expense> findByIdAndUser(Long id, User user);

    // Total amount for a month/year
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user = :user AND e.expenseMonth = :month AND e.expenseYear = :year")
    BigDecimal sumByUserAndMonthAndYear(
            @Param("user") User user,
            @Param("month") Integer month,
            @Param("year") Integer year);

    // Total amount for a year
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user = :user AND e.expenseYear = :year")
    BigDecimal sumByUserAndYear(@Param("user") User user, @Param("year") Integer year);

    // Category-wise total for a month/year
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user = :user AND e.expenseMonth = :month AND e.expenseYear = :year " +
           "GROUP BY e.category")
    List<Object[]> categoryBreakdownByMonthYear(
            @Param("user") User user,
            @Param("month") Integer month,
            @Param("year") Integer year);

    // Advanced filter with amount range and search
    @Query("SELECT e FROM Expense e WHERE e.user = :user " +
           "AND (:month IS NULL OR e.expenseMonth = :month) " +
           "AND (:year IS NULL OR e.expenseYear = :year) " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:paymentMethod IS NULL OR e.paymentMethod = :paymentMethod) " +
           "AND (:minAmount IS NULL OR e.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR e.amount <= :maxAmount) " +
           "AND (:isRecurring IS NULL OR e.isRecurring = :isRecurring) " +
           "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> findWithFilters(
            @Param("user") User user,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("category") ExpenseCategory category,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("isRecurring") Boolean isRecurring,
            @Param("search") String search);

    // Distinct years for history dropdown
    @Query("SELECT DISTINCT e.expenseYear FROM Expense e WHERE e.user = :user ORDER BY e.expenseYear DESC")
    List<Integer> findDistinctYearsByUser(@Param("user") User user);

    // Count transactions for a month/year
    long countByUserAndExpenseMonthAndExpenseYear(User user, Integer month, Integer year);
}