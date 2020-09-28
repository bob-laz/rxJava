package com.laz.rx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.laz.rx.controller.ProductController;
import com.laz.rx.model.Product;
import com.laz.rx.model.ProductEvent;
import com.laz.rx.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class Junit5ControllerTest {

  private WebTestClient client;
  private List<Product> expectedList;
  @Autowired private ProductRepository repository;
  @Autowired private ProductController controller;

  @BeforeEach
  void beforeEach() {
    this.client =
        WebTestClient.bindToController(controller).configureClient().baseUrl("/products").build();
    expectedList = repository.findAll().collectList().block();
  }

  @Test
  void testGetAllProducts() {
    client
        .get()
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(Product.class)
        .isEqualTo(expectedList);
  }

  @Test
  void testProductInvalidIdNotFound() {
    client.get().uri("/{id}", "aaa").exchange().expectStatus().isNotFound().expectBody().isEmpty();
  }

  @Test
  void testProductIdFound() {
    Product expectedProduct = expectedList.get(0);
    client
        .get()
        .uri("/{id}", expectedProduct.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Product.class)
        .isEqualTo(expectedProduct);
  }

  @Test
  void testProductEvents() {
    ProductEvent expectedEvent = new ProductEvent(0L, "Product Event");

    FluxExchangeResult<ProductEvent> result =
        client
            .get()
            .uri("/events")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(ProductEvent.class);

    StepVerifier.create(result.getResponseBody())
        .expectNext(expectedEvent)
        .expectNextCount(2)
        .consumeNextWith(event -> assertEquals(Long.valueOf(3), event.getEventId()))
        .thenCancel()
        .verify();
  }
}