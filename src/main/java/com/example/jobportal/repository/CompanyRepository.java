package com.example.jobportal.repository;

import com.example.jobportal.dto.response.CompanyBaseResponse;
import com.example.jobportal.model.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query("""
                SELECT new com.example.jobportal.dto.response.CompanyBaseResponse(
                    c.id,
                    c.name,
                    c.email,
                    c.isVerified,
                    c.isActive,
                    c.industry,
                    c.companySize,
                    c.address.city,
                    c.address.country
                )
                FROM Company c
                WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:location IS NULL OR LOWER(c.address.city) LIKE LOWER(CONCAT('%', :location, '%')))
                  AND (:isActive IS NULL OR c.isActive = :isActive)
            """)
    Page<CompanyBaseResponse> getAllCompanies(@Param("keyword") String keyword,
                                              @Param("location") String location,
                                              @Param("isActive") Boolean isActive,
                                              Pageable pageable);


}
