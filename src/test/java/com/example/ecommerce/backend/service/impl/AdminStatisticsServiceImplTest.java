package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.response.AdminDashboardResponse;
import com.example.ecommerce.backend.dto.response.RevenueByMonthResponse;
import com.example.ecommerce.backend.dto.response.TopSellingProductResponse;
import com.example.ecommerce.backend.repository.OrderItemRepository;
import com.example.ecommerce.backend.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminStatisticsServiceImpl")
class AdminStatisticsServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private AdminStatisticsServiceImpl service;

    // ── shared fixture builders ──────────────────────────────────────────────

    /** Builds a raw Object[] row as the repository would return it. */
    private static Object[] revenueRow(int year, int month, BigDecimal revenue) {
        return new Object[] { year, month, revenue };
    }

    private static Object[] productRow(long id, String name, long qty) {
        return new Object[] { id, name, qty };
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getRevenueByMonth()")
    class GetRevenueByMonth {

        @Test
        @DisplayName("maps each repository row to a RevenueByMonthResponse")
        void mapsRowsCorrectly() {
            when(orderRepository.findRevenueByMonth()).thenReturn(List.of(
                    revenueRow(2024, 1, new BigDecimal("1500.00")),
                    revenueRow(2024, 2, new BigDecimal("2300.50"))));

            List<RevenueByMonthResponse> result = service.getRevenueByMonth();

            assertThat(result).hasSize(2);

            RevenueByMonthResponse jan = result.get(0);
            assertThat(jan.year()).isEqualTo(2024);
            assertThat(jan.month()).isEqualTo(1);
            assertThat(jan.yearMonth()).isEqualTo("2024-01");
            assertThat(jan.revenue()).isEqualByComparingTo("1500.00");

            RevenueByMonthResponse feb = result.get(1);
            assertThat(feb.yearMonth()).isEqualTo("2024-02");
            assertThat(feb.revenue()).isEqualByComparingTo("2300.50");
        }

        @Test
        @DisplayName("substitutes BigDecimal.ZERO when revenue column is null")
        void nullRevenueFallsBackToZero() {
            when(orderRepository.findRevenueByMonth())
                    .thenReturn(List.<Object[]>of(revenueRow(2024, 3, null)));

            RevenueByMonthResponse response = service.getRevenueByMonth().get(0);

            assertThat(response.revenue()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("formats single-digit months with leading zero")
        void formatsMonthWithLeadingZero() {
            when(orderRepository.findRevenueByMonth())
                    .thenReturn(List.<Object[]>of(revenueRow(2024, 9, BigDecimal.TEN)));

            assertThat(service.getRevenueByMonth().get(0).yearMonth()).isEqualTo("2024-09");
        }

        @Test
        @DisplayName("returns an empty list when there is no revenue data")
        void returnsEmptyListWhenNoData() {
            when(orderRepository.findRevenueByMonth()).thenReturn(List.of());

            assertThat(service.getRevenueByMonth()).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getTopSellingProducts(int limit)")
    class GetTopSellingProducts {

        @Test
        @DisplayName("maps each repository row to a TopSellingProductResponse")
        void mapsRowsCorrectly() {
            when(orderItemRepository.findTopSellingProducts(2)).thenReturn(List.<Object[]>of(
                    productRow(1L, "Widget A", 500L),
                    productRow(2L, "Widget B", 300L)));

            List<TopSellingProductResponse> result = service.getTopSellingProducts(2);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(1L);
            assertThat(result.get(0).productName()).isEqualTo("Widget A");
            assertThat(result.get(0).totalQuantitySold()).isEqualTo(500L);
        }

        @Test
        @DisplayName("passes limit directly to the repository (no in-memory truncation)")
        void delegatesLimitToRepository() {
            when(orderItemRepository.findTopSellingProducts(5)).thenReturn(List.of());

            service.getTopSellingProducts(5);

            // Verify the repository received the limit — not findTopSellingProducts()
            verify(orderItemRepository).findTopSellingProducts(5);
            verifyNoMoreInteractions(orderItemRepository);
        }

        @Test
        @DisplayName("substitutes 'Unknown' when product name column is null")
        void nullProductNameFallsBackToUnknown() {
            when(orderItemRepository.findTopSellingProducts(1))
                    .thenReturn(List.<Object[]>of(productRow(99L, null, 10L)));

            assertThat(service.getTopSellingProducts(1).get(0).productName())
                    .isEqualTo("Unknown");
        }

        @ParameterizedTest(name = "limit = {0}")
        @ValueSource(ints = { 0, -1, -100 })
        @DisplayName("throws IllegalArgumentException for non-positive limit values")
        void rejectsNonPositiveLimit(int invalidLimit) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.getTopSellingProducts(invalidLimit))
                    .withMessageContaining("limit must be greater than 0");

            verifyNoInteractions(orderItemRepository);
        }

        @Test
        @DisplayName("returns an empty list when the repository returns no rows")
        void returnsEmptyListWhenNoData() {
            when(orderItemRepository.findTopSellingProducts(10)).thenReturn(List.of());

            assertThat(service.getTopSellingProducts(10)).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getDashboardStatistics(int topProductsLimit)")
    class GetDashboardStatistics {

        @Test
        @DisplayName("aggregates revenue and top products into a single response")
        void aggregatesBothDataSources() {
            when(orderRepository.findRevenueByMonth()).thenReturn(List.<Object[]>of(
                    revenueRow(2024, 1, new BigDecimal("999.99"))));
            when(orderItemRepository.findTopSellingProducts(3)).thenReturn(List.<Object[]>of(
                    productRow(7L, "Gadget X", 120L)));

            AdminDashboardResponse dashboard = service.getDashboardStatistics(3);

            assertThat(dashboard.revenueByMonth()).hasSize(1);
            assertThat(dashboard.revenueByMonth().get(0).yearMonth()).isEqualTo("2024-01");

            assertThat(dashboard.topSellingProducts()).hasSize(1);
            assertThat(dashboard.topSellingProducts().get(0).productName()).isEqualTo("Gadget X");
        }

        @Test
        @DisplayName("queries each repository exactly once")
        void queriesEachRepositoryOnce() {
            when(orderRepository.findRevenueByMonth()).thenReturn(List.of());
            when(orderItemRepository.findTopSellingProducts(5)).thenReturn(List.of());

            service.getDashboardStatistics(5);

            verify(orderRepository, times(1)).findRevenueByMonth();
            verify(orderItemRepository, times(1)).findTopSellingProducts(5);
        }

        @ParameterizedTest(name = "topProductsLimit = {0}")
        @ValueSource(ints = { 0, -1, Integer.MIN_VALUE })
        @DisplayName("throws IllegalArgumentException for non-positive topProductsLimit")
        void rejectsNonPositiveTopProductsLimit(int invalidLimit) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> service.getDashboardStatistics(invalidLimit))
                    .withMessageContaining("topProductsLimit must be greater than 0");

            verifyNoInteractions(orderRepository, orderItemRepository);
        }

        @Test
        @DisplayName("returns empty lists when no data exists")
        void returnsEmptyDashboardWhenNoData() {
            when(orderRepository.findRevenueByMonth()).thenReturn(List.of());
            when(orderItemRepository.findTopSellingProducts(10)).thenReturn(List.of());

            AdminDashboardResponse dashboard = service.getDashboardStatistics(10);

            assertThat(dashboard.revenueByMonth()).isEmpty();
            assertThat(dashboard.topSellingProducts()).isEmpty();
        }
    }
}