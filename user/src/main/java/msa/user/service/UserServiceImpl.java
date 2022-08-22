package msa.user.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.user.client.OrderServiceClient;
import msa.user.dto.UserDto;
import msa.user.entity.UserEntity;
import msa.user.error.FeignErrorDecoder;
import msa.user.repository.UserRepository;
import msa.user.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Environment env;
    private final RestTemplate restTemplate;
    private final OrderServiceClient orderServiceClient;

    @Override
    public UserDto createUser(UserDto userDto) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);   // 매칭 전략 지정

        userDto.setUserId(UUID.randomUUID().toString());
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }
        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        // Using a RestTemplate
        /*
        String orderUrl = String.format(env.getProperty("order_service.url"), userId); // http://127.0.0.1:8000/order-service/%s/orders";
        ResponseEntity<List<ResponseOrder>> resultResponse =
                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<ResponseOrder>>() {
                });
        List<ResponseOrder> orderList = resultResponse.getBody();
        */

        // Using a feign client, and exception handling
        /*
        List<ResponseOrder> orderList = null;
        try {
            orderList = orderServiceClient.getOrders(userDto.getUserId());
        } catch (FeignException.FeignClientException ex) {
            log.error(ex.getMessage());
        }
        */

        // Using ErrorDecoder
        List<ResponseOrder> orderList = orderServiceClient.getOrders(userDto.getUserId());
        userDto.setOrders(orderList);
        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new UsernameNotFoundException(email + " not found email");
        }
        return new ModelMapper().map(userEntity, UserDto.class);
    }

    @Override   // spring security method 구현
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException(username + " not found username");
        }
        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true,
                 new ArrayList<>());
    }

}
