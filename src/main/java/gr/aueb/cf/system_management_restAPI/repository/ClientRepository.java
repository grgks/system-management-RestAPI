package gr.aueb.cf.system_management_restAPI.repository;


import gr.aueb.cf.system_management_restAPI.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByUserUsername(String username);

    Optional<Client> findByUuid(String uuid);

    Optional<Client> findByVat(String vat);

    List<Client> findByPersonalInfoLastNameContainingIgnoreCase(String lastName);

    @Query("SELECT c FROM Client c WHERE c.personalInfo.firstName LIKE %:name% OR c.personalInfo.lastName LIKE %:name%")
    List<Client> findByFullNameContaining(@Param("name") String name);

    boolean existsByUserUsername(String username);

    boolean existsByVat(String vat);
}
