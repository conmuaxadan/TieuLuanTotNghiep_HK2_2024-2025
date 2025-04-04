package com.raindrop.identity_service.service;

import com.raindrop.event.UserProfileEvent;
import com.raindrop.identity_service.dto.request.UserRequest;
import com.raindrop.identity_service.dto.response.UserResponse;
import com.raindrop.identity_service.entity.Role;
import com.raindrop.identity_service.entity.User;
import com.raindrop.identity_service.exception.AppException;
import com.raindrop.identity_service.enums.ErrorCode;
import com.raindrop.identity_service.mapper.ProfileMapper;
import com.raindrop.identity_service.mapper.UserMapper;
import com.raindrop.identity_service.repository.RoleRepository;
import com.raindrop.identity_service.repository.UserRepository;
import com.raindrop.identity_service.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    ProfileMapper profileMapper;
    KafkaTemplate<String, Object> kafkaTemplate;

    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        var roles = new HashSet<Role>();
        roles.add(Role.builder().name("USER").build());
        user.setRoles(roles);
        user = userRepository.save(user);
        var profileRequest = profileMapper.toUserProfileRequest(request);
        profileRequest.setUserId(user.getId());

        UserProfileEvent profileEvent = UserProfileEvent.builder()
                .userId(profileRequest.getUserId())
                .displayName(profileRequest.getDisplayName())
                .avatarUrl(profileRequest.getAvatarUrl())
                .build();

//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        var header = attributes.getRequest().getHeader("Authorization");
//        profileClient.createProfile(header,profileRequest);

        //Publish message to Kafka
        kafkaTemplate.send("onboard-successful",profileEvent);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).collect(Collectors.toList());
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUserByUsername(String username) {
        return userMapper.toUserResponse(userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found")));
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public User updateUser(UserRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getPassword() == null && request.getEmail() == null) {
            throw new RuntimeException("At least one of password or email must be provided");
        }
        userMapper.updateUser(user, request);
        user.setPassword(new BCryptPasswordEncoder(10).encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userRepository.save(user);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteUser(UserRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponse(user);
    }
}
