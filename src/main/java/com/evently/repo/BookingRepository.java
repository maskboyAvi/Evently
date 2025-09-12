package com.evently.repo;

import com.evently.api.dto.BookingDto;
import com.evently.domain.Booking;
import com.evently.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    long countByEventIdAndStatus(Long eventId, BookingStatus status);

    @Query("select new com.evently.api.dto.BookingDto(b.id, b.user.id, b.event.id, (case when b.seat is null then null else b.seat.id end), b.status, b.createdAt, b.updatedAt) from Booking b where b.user.id = :userId")
    List<BookingDto> findDtosByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, BookingStatus status);

    @Query("select cast(b.createdAt as date) as d, count(b) as c from Booking b where b.status = com.evently.domain.BookingStatus.CONFIRMED and b.createdAt >= :since group by cast(b.createdAt as date) order by d asc")
    List<Object[]> countDailyConfirmedSince(@Param("since") Instant since);

    @Query("select count(b) from Booking b where b.status = com.evently.domain.BookingStatus.CANCELED and b.updatedAt >= :since")
    long countCanceledSince(@Param("since") Instant since);

    @Query("select count(b) from Booking b where b.status = com.evently.domain.BookingStatus.CONFIRMED and b.createdAt >= :since")
    long countConfirmedSince(@Param("since") Instant since);
}