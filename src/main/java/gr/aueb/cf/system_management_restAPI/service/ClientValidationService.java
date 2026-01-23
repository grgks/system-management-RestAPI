package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.dto.ClientInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.ClientUpdateDTO;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.PersonalInfoRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientValidationService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PersonalInfoRepository personalInfoRepository;

    public void validateNewClient(ClientInsertDTO dto) throws AppObjectAlreadyExists {
        validateUsername(dto.getUser().getUsername());
        validateEmail(dto.getUser().getEmail());
        validateVat(dto.getVat());
        validatePhone(dto.getPersonalInfo().getPhone());
        validatePersonalInfoEmail(dto.getPersonalInfo().getEmail());
    }

    public void validateClientUpdate(Long clientId, ClientUpdateDTO dto) throws AppObjectAlreadyExists {
        if (dto.getVat() != null) {
            validateVatUpdate(clientId, dto.getVat());
        }
    }

    private void validateUsername(String username) throws AppObjectAlreadyExists {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new AppObjectAlreadyExists("User",
                    "User with username: " + username + " already exists");
        }
    }

    private void validateEmail(String email) throws AppObjectAlreadyExists {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AppObjectAlreadyExists("User",
                    "User with email: " + email + " already exists");
        }
    }

    private void validateVat(String vat) throws AppObjectAlreadyExists {
        if (vat != null && clientRepository.findByVat(vat).isPresent()) {
            throw new AppObjectAlreadyExists("Client",
                    "Client with VAT: " + vat + " already exists");
        }
    }

    private void validatePhone(String phone) throws AppObjectAlreadyExists {
        if (phone != null && personalInfoRepository.findByPhone(phone).isPresent()) {
            throw new AppObjectAlreadyExists("PersonalInfo",
                    "Personal info with phone: " + phone + " already exists");
        }
    }

    private void validatePersonalInfoEmail(String email) throws AppObjectAlreadyExists {
        if (email != null && !email.trim().isEmpty() &&
                personalInfoRepository.findByEmail(email).isPresent()) {
            throw new AppObjectAlreadyExists("PersonalInfo",
                    "Personal info with email: " + email + " already exists");
        }
    }

    private void validateVatUpdate(Long clientId, String vat) throws AppObjectAlreadyExists {
        clientRepository.findById(clientId).ifPresent(existingClient -> {
            if (!vat.equals(existingClient.getVat()) &&
                    clientRepository.findByVat(vat).isPresent()) {
                try {
                    throw new AppObjectAlreadyExists("Client",
                            "Client with VAT: " + vat + " already exists");
                } catch (AppObjectAlreadyExists e) {
                    throw new RuntimeException(e);

                    //Code smell = try-catch που wrap-άρει exception μέσα σε lambda
                    //Σπάει το exception contract
                    //RuntimeException δεν catch-άρεται από caller
                    //Περιττή πολυπλοκότητα
                    //na to do me to apo katw

//                    private void validateVatUpdate(Long clientId, String vat) throws AppObjectAlreadyExists {
//                        Optional<Client> existing = clientRepository.findById(clientId);
//
//                        if (existing.isPresent()) {
//                            Client existingClient = existing.get();
//
//                            if (!vat.equals(existingClient.getVat()) &&
//                                    clientRepository.findByVat(vat).isPresent()) {
//                                throw new AppObjectAlreadyExists("Client", "Client with VAT: " + vat + " already exists");
//

                }
            }
        });
    }
}