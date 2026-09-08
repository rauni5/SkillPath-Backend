package com.skillpath.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillpath.model.User.User;
import com.skillpath.model.enums.Proficiency;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
 Optional<User> findByFirebaseUid(String firebaseUid);
 boolean existsByFirebaseUid(String firebaseUid);

 /** Single query does both the lookup and the enabled check, so a
  *  disabled profile's token 404s exactly the same as an unknown one —
  *  doesn't leak whether the token used to be valid. */
 Optional<User> findByPublicProfileTokenAndPublicProfileEnabledTrue(String token);

 /** Case-insensitive substring match on name or email; used by the admin
  *  user list search box. Pass null/blank q to just page through everyone.
  *  adminFilter/activeFilter are optional (null = don't filter on that). */
 @Query("""
     SELECT u FROM User u
     WHERE (:q IS NULL
        OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
       AND (:adminFilter IS NULL OR u.admin = :adminFilter)
       AND (:activeFilter IS NULL OR u.active = :activeFilter)
     """)
 Page<User> search(
         @Param("q") String q,
         @Param("adminFilter") Boolean adminFilter,
         @Param("activeFilter") Boolean activeFilter,
         Pageable pageable);

 long countByAdminTrue();
 long countByAvailabilityTrue();
 long countByCreatedAtAfter(Instant since);
 long countByExperienceLevel(Proficiency level);

 /** Daily signup counts since [since], one row per calendar day in the
  *  range (zero-filled via generate_series) so charts render a full
  *  30-day axis instead of just the days that happened to have a signup.
  *  gs.day comes back from generate_series() as a timestamp (Postgres
  *  quirk of the interval-step overload), so it's cast back to date
  *  explicitly rather than left for the JDBC driver to guess. */
 @Query(value = """
     SELECT CAST(gs.day AS date) AS day, COALESCE(COUNT(u.id), 0) AS cnt
     FROM generate_series(CAST(:since AS date), CURRENT_DATE, interval '1 day') AS gs(day)
     LEFT JOIN users u ON CAST(u.created_at AS date) = CAST(gs.day AS date)
     GROUP BY gs.day
     ORDER BY gs.day
     """, nativeQuery = true)
 List<Object[]> signupCountsSince(@Param("since") Instant since);

 default List<DailySignup> signupTrendSince(Instant since) {
     return signupCountsSince(since).stream()
             .map(row -> new DailySignup(toLocalDate(row[0]), ((Number) row[1]).longValue()))
             .toList();
 }

 /** The JDBC driver's Java type for a `date` column varies by version/config
  *  (java.sql.Date, java.time.LocalDate, java.sql.Timestamp, or even
  *  java.time.Instant) — normalize whatever comes back instead of assuming one.
  *  Written with plain instanceof checks (not switch pattern matching) since
  *  this repo's Java version wasn't confirmed to be 21+. */
 private static LocalDate toLocalDate(Object raw) {
     if (raw instanceof LocalDate d) return d;
     if (raw instanceof java.sql.Date d) return d.toLocalDate();
     if (raw instanceof java.sql.Timestamp t) return t.toLocalDateTime().toLocalDate();
     if (raw instanceof Instant i) return i.atZone(java.time.ZoneOffset.UTC).toLocalDate();
     if (raw instanceof java.util.Date d) return d.toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDate();
     throw new IllegalStateException(
             "Unexpected date type from driver: " + (raw == null ? "null" : raw.getClass()));
 }

 record DailySignup(LocalDate date, long count) {}
 @Query("""
     SELECT u FROM User u
     WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
     ORDER BY u.name ASC
     """)
 Page<User> searchByNameOrEmail(@Param("q") String q, Pageable pageable);
}
