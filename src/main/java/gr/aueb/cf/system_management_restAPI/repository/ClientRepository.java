package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByUserUsername(String username);
    Optional<Client> findByUuid(String uuid);
    Optional<Client> findByVat(String vat);

    boolean existsByUserUsername(String username);
    boolean existsByVat(String vat);
}
