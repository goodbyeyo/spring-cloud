package msa.catalog.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.catalog.entity.CatalogEntity;
import msa.catalog.repository.CatalogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final CatalogRepository repository;

    @KafkaListener(topics = "catalog-topic")
    public void updateQty(String kafkaMessage) {
        log.info("Kafka message: -> {}", kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try{
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CatalogEntity entity = repository.findByProductId(String.valueOf(map.get("productId")));
        if (entity != null) {
            entity.setStock(entity.getStock() - Integer.parseInt(String.valueOf(map.get("qty"))));
            repository.save(entity);
        }

    }

}
