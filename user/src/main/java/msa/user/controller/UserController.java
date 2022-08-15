package msa.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.user.dto.UserDto;
import msa.user.entity.UserEntity;
import msa.user.service.UserService;
import msa.user.vo.Greeting;
import msa.user.vo.RequestUser;
import msa.user.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Slf4j
public class UserController {

    private final Environment env;
    private final UserService userService;
    private final Greeting greeting;

    @GetMapping("/health_check")
    public String check(HttpServletRequest request) {
        log.info("Server port={}", request.getServerPort());
        return String.format("It's Working in User Service" + "\r\n"
                + ", port(local.server.port)=" + env.getProperty("local.server.port") + "\r\n"
                + ", port(server.port)=" + env.getProperty("server.port") + "\r\n"
                + ", gateway ip=" + env.getProperty("gateway.ip") + "\r\n"
                + ", message=" + env.getProperty("greeting.message") + "\r\n"
                + ", token secret=" + env.getProperty("token.secret") + "\r\n"
                + ", token expiration time=" + env.getProperty("token.expiration_time"));
    }

    @GetMapping("/welcome")
    public String welcome() {
//        return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);   // 매칭 전략 지정

        UserDto userdto = mapper.map(user, UserDto.class);
        userService.createUser(userdto);

        ResponseUser responseBody = mapper.map(userdto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUserList() {
        Iterable<UserEntity> userList = userService.getUserByAll();
        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(r-> {
            result.add(new ModelMapper().map(r, ResponseUser.class));
        });
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUserOne(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);
        ResponseUser responseBody = new ModelMapper().map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);

    }
}
