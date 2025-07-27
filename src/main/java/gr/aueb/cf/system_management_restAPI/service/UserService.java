package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.UserReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;

    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUsername(String username) throws AppObjectNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User with username: " + username + " not found"));

        return mapper.mapToUserReadOnlyDTO(user);
    }

    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserById(Long id) throws AppObjectNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User with id: " + id + " not found"));

        return mapper.mapToUserReadOnlyDTO(user);
    }
}