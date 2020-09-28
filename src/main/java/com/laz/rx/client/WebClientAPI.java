package com.laz.rx.client;

import com.laz.rx.model.Product;
import com.laz.rx.model.ProductEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WebClientAPI {
  private final WebClient webClient;

  public WebClientAPI() {
    webClient = WebClient.builder().baseUrl("http://localhost:8080/products").build();
  }


  public Mono<ResponseEntity<Product>> postNewProduct() {
    return webClient
        .post()
        .body(Mono.just(new Product(null, "Black Tea", 1.99)), Product.class)
        .exchange()
        .flatMap(response -> response.toEntity(Product.class))
        .doOnSuccess(o -> System.out.println("*** POST " + o));
  }

  public Flux<Product> getAllProducts() {
    return webClient
        .get()
        .retrieve()
        .bodyToFlux(Product.class)
        .doOnNext(o -> System.out.println("*** GET ALL: " + o));
  }

  public Mono<Product> updateProduct(String id, String name, double price) {
    return webClient
        .put()
        .uri("/{id}", id)
        .body(Mono.just(new Product(null, name, price)), Product.class)
        .retrieve()
        .bodyToMono(Product.class)
        .doOnSuccess(o -> System.out.println("*** UPDATE: " + o));
  }

  public Mono<Void> deleteProduct(String id) {
    return webClient
        .delete()
        .uri("/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(o -> System.out.println("*** DELETE: " + o));
  }

  public Flux<ProductEvent> getAllEvents() {
    return webClient.get().uri("/events").retrieve().bodyToFlux(ProductEvent.class);
  }
}
