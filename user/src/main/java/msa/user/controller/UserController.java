package msa.user.controller;

import lombok.RequiredArgsConstructor;
import msa.user.dto.UserDto;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {

    private final Environment env;
    private final UserService userService;
    private final Greeting greeting;

    @GetMapping("/health_check")
    public String status() {
        return "It's Working in User Service";
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
}
