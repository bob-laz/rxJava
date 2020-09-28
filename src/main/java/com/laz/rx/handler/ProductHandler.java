package com.laz.rx.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.laz.rx.model.Product;
import com.laz.rx.model.ProductEvent;
import com.laz.rx.repository.ProductRepository;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class ProductHandler {

  private final ProductRepository repository;

  public Mono<ServerResponse> getAllProducts(ServerRequest req) {
    Flux<Product> products = repository.findAll();

    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(products, Product.class);
  }

  public Mono<ServerResponse> getProduct(ServerRequest req) {
    System.out.println("no i got called");
    String id = req.pathVariable("id");

    Mono<Product> product = repository.findById(id);

    return product
        .flatMap(
            prod ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromValue(product)))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> saveProduct(ServerRequest req) {
    Mono<Product> productMono = req.bodyToMono(Product.class);

    return productMono.flatMap(
        product ->
            ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(repository.save(product), Product.class));
  }

  public Mono<ServerResponse> updateProduct(ServerRequest req) {
    String id = req.pathVariable("id");
    Mono<Product> existingProductMono = repository.findById(id);
    Mono<Product> productMono = req.bodyToMono(Product.class);

    return productMono
        .zipWith(
            existingProductMono,
            (product, existingProduct) ->
                new Product(existingProduct.getId(), product.getName(), product.getPrice()))
        .flatMap(
            product ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(repository.save(product), Product.class))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> deleteProduct(ServerRequest req) {
    String id = req.pathVariable("id");

    Mono<Product> productMono = repository.findById(id);

    return productMono.flatMap(
        existingProduct -> ServerResponse.noContent().build(repository.delete(existingProduct)));
  }

  public Mono<ServerResponse> deleteAllProducts(ServerRequest req) {
    return ServerResponse.ok().build(repository.deleteAll());
  }

  public Mono<ServerResponse> getProductEvents(ServerRequest req) {
    Flux<ProductEvent> eventsFlux =
        Flux.interval(Duration.ofSeconds(1)).map(val -> new ProductEvent(val, "Product Event"));

    return ServerResponse.ok()
        .contentType(MediaType.TEXT_EVENT_STREAM)
        .body(eventsFlux, ProductEvent.class);
  }
}
