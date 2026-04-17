package com.example.jobportal.utils;

import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;

/**
 * Centralizes sort field resolution for all paginated endpoints.
 *
 * - Maps frontend-friendly names (snake_case, spaces) to entity field names
 * - Whitelists allowed fields to prevent JPA crashes and SQL injection
 * - Throws IllegalArgumentException (→ 400) on invalid input
 */
public final class SortUtils {

    private SortUtils() {}

    // Whitelist: frontend name → entity field name
    private static final Map<String, String> JOB_SORT_FIELDS = Map.of(
            "created_at",   "createdAt",
            "createdat",    "createdAt",
            "published_at", "publishedAt",
            "publishedat",  "publishedAt",
            "published at", "publishedAt",
            "title",        "title",
            "salary_min",   "salaryMin",
            "salarymin",    "salaryMin",
            "views_count",  "viewsCount",
            "viewscount",   "viewsCount"
    );

    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");

    /**
     * Resolves a raw sort string to a valid entity field name.
     * Throws IllegalArgumentException if the field is not whitelisted.
     */
    public static String resolveJobSortField(String raw) {
        if (raw == null || raw.isBlank()) {
            return "createdAt"; // safe default
        }
        String key = raw.trim().toLowerCase();
        String resolved = JOB_SORT_FIELDS.get(key);
        if (resolved == null) {
            throw new IllegalArgumentException(
                "Invalid sort field: '" + raw + "'. Allowed values: " + JOB_SORT_FIELDS.keySet()
            );
        }
        return resolved;
    }

    /**
     * Parses direction string to Sort.Direction, defaults to DESC on invalid input.
     */
    public static Sort.Direction resolveDirection(String raw) {
        if (raw == null || raw.isBlank()) {
            return Sort.Direction.DESC;
        }
        String upper = raw.trim().toUpperCase();
        if (!ALLOWED_DIRECTIONS.contains(upper)) {
            throw new IllegalArgumentException(
                "Invalid sort direction: '" + raw + "'. Allowed values: ASC, DESC"
            );
        }
        return Sort.Direction.valueOf(upper);
    }

    /**
     * Convenience method: returns a Sort object for job queries.
     */
    public static Sort jobSort(String sortField, String direction) {
        return Sort.by(resolveDirection(direction), resolveJobSortField(sortField));
    }
}
