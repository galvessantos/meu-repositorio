package com.montreal.oauth.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.oauth.domain.entity.UserInfo;
import com.montreal.oauth.domain.repository.infrastructure.CustomJpaRepository;


@Repository
public interface IUserRepository extends CustomJpaRepository<UserInfo, Long>, JpaSpecificationExecutor<UserInfo>  {
   
   UserInfo findByUsername(String username);

   @Query(value = "SELECT * FROM users u WHERE LOWER(u.username) = LOWER(CONCAT(:username, ''))", nativeQuery = true)
   Optional<UserInfo> obterByUsername(@Param("username") String username);

   UserInfo findFirstById(Long id);

   @Query("SELECT u FROM UserInfo u WHERE u.email = :email")
   UserInfo findByEmail(@Param("email") String email);

   @Query(value = "SELECT * FROM users u WHERE descriptografar(u.email) = :email", nativeQuery = true)
   Optional<UserInfo> findByEmailDecrypted(@Param("email") String email);
   
   @Query("SELECT u FROM UserInfo u WHERE u.id = :id")
   Optional<UserInfo> findFirstByIdUpdatePassword(@Param("id") Long id);

   @Query("SELECT u FROM UserInfo u WHERE u.link = :link")
   UserInfo findLink(@Param("link") String link);
   
   boolean existsByUsername(String username);

   boolean existsByCpf(String cpf);
   
   Page<UserInfo> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
   
   @Query(
	    value = "SELECT * FROM users u " +
	            "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
	            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
	            "OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
	            "OR LOWER(u.cpf) LIKE LOWER(CONCAT('%', :searchTerm, '%'))",
	    countQuery = "SELECT COUNT(*) FROM users u " +
	                 "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
	                 "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
	                 "OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
	                 "OR LOWER(u.cpf) LIKE LOWER(CONCAT('%', :searchTerm, '%'))",
	    nativeQuery = true
	)
	Page<UserInfo> searchByMultipleFields(@Param("searchTerm") String searchTerm, Pageable pageable);


   @Query("SELECT u FROM UserInfo u WHERE u.companyId = :companyId")
   List<UserInfo> findAllByCompanyId(@Param("companyId") Long companyId);
   
   @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM UserInfo u WHERE UPPER(u.username) = UPPER(:username)")
   boolean existsByUsernameIgnoreCase(@Param("username") String username);

   boolean existsByEmail(String email);
   
   @Query(value = """
		    SELECT * FROM users u 
		    WHERE 
		        (:fieldValue IS NULL OR 
		        CASE 
		            WHEN :fieldName = 'fullName' THEN descriptografar(u.fullname)
		            WHEN :fieldName = 'cpf' THEN descriptografar(u.cpf)
		            WHEN :fieldName = 'phone' THEN descriptografar(u.phone)
		            ELSE NULL 
		        END ILIKE CONCAT('%', :fieldValue, '%'))
		    """,
		    nativeQuery = true)
		Page<UserInfo> searchByDynamicField(
		        @Param("fieldName") String fieldName,
		        @Param("fieldValue") String fieldValue,
		        Pageable pageable);


	@Query("SELECT DISTINCT u FROM UserInfo u " +
			"LEFT JOIN FETCH u.roles r " +
			"LEFT JOIN FETCH r.roleFunctionalities rf " +
			"LEFT JOIN FETCH rf.functionality f " +
			"WHERE u.id = :userId")
	Optional<UserInfo> findByIdWithRolesAndFunctionalities(@Param("userId") Long userId);

	@org.springframework.data.jpa.repository.Modifying
	@Query("UPDATE UserInfo u SET u.isFirstLoginCompleted = :firstLoginCompleted WHERE u.id = :userId")
	int updateFirstLoginCompleted(@Param("userId") Long userId, @Param("firstLoginCompleted") boolean firstLoginCompleted);

}
