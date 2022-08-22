package msa.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.order.dto.OrderDto;
import msa.order.entity.OrderEntity;
import msa.order.message.KafkaProducer;
import msa.order.message.OrderProducer;
import msa.order.service.OrderService;
import msa.order.vo.RequestOrder;
import msa.order.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/")
public class OrderController {
    private final Environment env;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in Order Service on PORT %s",
                env.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
                                                     @RequestBody RequestOrder order) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(order, OrderDto.class);
        orderDto.setUserId(userId);

        // JPA
        /*
        OrderDto createOrder = orderService.createOrder(orderDto);
        ResponseOrder responseBody = mapper.map(createOrder, ResponseOrder.class);
        */

        // data setting for Kafka
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(order.getQty() * order.getUnitPrice());

        // send order to kafka
        kafkaProducer.send("catalog-topic", orderDto);
        orderProducer.send("orders", orderDto);

        ResponseOrder responseBody = mapper.map(orderDto, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }


    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) {
        Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> result = new ArrayList<>();
        orderList.forEach(r -> {
            result.add(new ModelMapper().map(r, ResponseOrder.class));
        });
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
