package com.symptocare.backend.controller;

import com.symptocare.backend.dto.ExpenseFilterRequest;
import com.symptocare.backend.dto.ExpenseRequest;
import com.symptocare.backend.dto.ExpenseResponse;
import com.symptocare.backend.dto.ExpenseSummaryResponse;
import com.symptocare.backend.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // POST /api/expenses
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            Authentication auth,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.create(auth.getName(), request));
    }

    // GET /api/expenses
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAll(Authentication auth) {
        return ResponseEntity.ok(expenseService.getAll(auth.getName()));
    }

    // POST /api/expenses/filter
    @PostMapping("/filter")
    public ResponseEntity<List<ExpenseResponse>> getFiltered(
            Authentication auth,
            @RequestBody ExpenseFilterRequest filter) {
        return ResponseEntity.ok(expenseService.getFiltered(auth.getName(), filter));
    }

    // GET /api/expenses/month?month=6&year=2025
    @GetMapping("/month")
    public ResponseEntity<List<ExpenseResponse>> getByMonthYear(
            Authentication auth,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(expenseService.getByMonthYear(auth.getName(), month, year));
    }

    // GET /api/expenses/summary/month?month=6&year=2025
    @GetMapping("/summary/month")
    public ResponseEntity<ExpenseSummaryResponse> getMonthlySummary(
            Authentication auth,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(expenseService.getMonthlySummary(auth.getName(), month, year));
    }

    // GET /api/expenses/summary/year?year=2025
    @GetMapping("/summary/year")
    public ResponseEntity<ExpenseSummaryResponse> getYearlySummary(
            Authentication auth,
            @RequestParam Integer year) {
        return ResponseEntity.ok(expenseService.getYearlySummary(auth.getName(), year));
    }

    // GET /api/expenses/years
    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAvailableYears(Authentication auth) {
        return ResponseEntity.ok(expenseService.getAvailableYears(auth.getName()));
    }

    // GET /api/expenses/stats/current
    @GetMapping("/stats/current")
    public ResponseEntity<Map<String, Object>> getCurrentMonthStats(Authentication auth) {
        return ResponseEntity.ok(expenseService.getCurrentMonthStats(auth.getName()));
    }

    // PUT /api/expenses/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.update(auth.getName(), id, request));
    }

    // DELETE /api/expenses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            Authentication auth,
            @PathVariable Long id) {
        expenseService.delete(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Expense deleted successfully"));
    }
}