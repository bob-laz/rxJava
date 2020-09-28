package com.laz.rx.controller;

import com.laz.rx.client.WebClientAPI;
import com.laz.rx.model.Product;
import com.laz.rx.model.ProductEvent;
import com.laz.rx.repository.ProductRepository;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
public class ProductController {

  private final ProductRepository repository;
  private final WebClientAPI api;

  @GetMapping
  public Flux<Product> getAllProducts() {
    return repository.findAll();
  }

  @GetMapping("{id}")
  public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id) {
    return repository
        .findById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Product> save(@RequestBody Product product) {
    return repository.save(product);
  }

  @PutMapping("{id}")
  public Mono<ResponseEntity<Product>> update(
      @PathVariable String id, @RequestBody Product product) {
    return repository
        .findById(id)
        .flatMap(
            existingProduct -> {
              existingProduct.setName(product.getName());
              existingProduct.setPrice(product.getPrice());
              return repository.save(existingProduct);
            })
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> delete(@PathVariable String id) {
    return repository.findById(id).flatMap(repository::delete);
  }

  @DeleteMapping
  public Mono<Void> deleteAll() {
    return repository.deleteAll();
  }

  @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ProductEvent> getProductEvents() {
    return Flux.interval(Duration.ofSeconds(1)).map(val -> new ProductEvent(val, "Product Event"));
  }

  @GetMapping("/api")
  public Flux<Product> triggerApiCalls() {
    return api.postNewProduct()
        .thenMany(api.getAllProducts())
        .take(1)
        .flatMap(p -> api.updateProduct(p.getId(), "White Tea", 0.99))
        .flatMap(p -> api.deleteProduct(p.getId()))
        .thenMany(api.getAllProducts());
  }
}
