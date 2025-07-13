package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, Long>, JpaSpecificationExecutor<PersonalInfo> {

    List<PersonalInfo> findByLastNameContainingIgnoreCase(String lastName);

    List<PersonalInfo> findByFirstNameContainingIgnoreCase(String firstName);

    Optional<PersonalInfo> findByPhone(String phone);

    Optional<PersonalInfo> findByEmail(String email);

    List<PersonalInfo> findByGender(GenderType gender);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
