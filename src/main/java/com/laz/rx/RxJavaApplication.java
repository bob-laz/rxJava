package com.laz.rx;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.laz.rx.handler.ProductHandler;
import com.laz.rx.model.Product;
import com.laz.rx.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class RxJavaApplication {

  public static void main(String... args) {
    SpringApplication.run(RxJavaApplication.class);
  }

  @Bean
  RouterFunction<ServerResponse> routes(ProductHandler handler) {

    // order matters, /products/events needs to come before /products/{id}
    //    return route(GET("/functional/products").and(accept(APPLICATION_JSON)),
    // handler::getAllProducts)
    //        .andRoute(POST("/products").and(contentType(APPLICATION_JSON)), handler::saveProduct)
    //        .andRoute(
    //            DELETE("/functional/products").and(accept(APPLICATION_JSON)),
    //            handler::deleteAllProducts)
    //        .andRoute(
    //            GET("/functional/products/events").and(accept(TEXT_EVENT_STREAM)),
    //            handler::getProductEvents)
    //        .andRoute(
    //            GET("/functional/products/{id}").and(accept(APPLICATION_JSON)),
    // handler::getProduct)
    //        .andRoute(
    //            PUT("/functional/products/{id}").and(contentType(APPLICATION_JSON)),
    //            handler::updateProduct)
    //        .andRoute(
    //            DELETE("/functional/products/{id}").and(accept(APPLICATION_JSON)),
    //            handler::deleteProduct);

    // similar to above but allows us to nest common parameters
    return nest(
        path("/functional/products"),
        nest(
            accept(APPLICATION_JSON)
                .or(contentType(APPLICATION_JSON))
                .or(accept(TEXT_EVENT_STREAM)),
            route(GET("/"), handler::getAllProducts)
                .andRoute(method(HttpMethod.POST), handler::saveProduct)
                .andRoute(DELETE("/"), handler::deleteAllProducts)
                .andRoute(GET("/events"), handler::getProductEvents)
                .andNest(
                    path("/{id}"),
                    route(method(HttpMethod.GET), handler::getProduct)
                        .andRoute(method(HttpMethod.PUT), handler::updateProduct)
                        .andRoute(method(HttpMethod.DELETE), handler::deleteProduct))));
  }

  @Bean
  CommandLineRunner init(ProductRepository repo) {
    return args -> {
      // clean up any products from previous run
      Mono<Void> deleteMono = repo.deleteAll();

      Flux<Product> productFlux =
          Flux.just(
                  new Product(null, "Big Latte", 2.99),
                  new Product(null, "Big Decaf", 2.49),
                  new Product(null, "Green Tea", 1.99))
              .flatMap(repo::save);

      Flux<Product> findAllFlux = repo.findAll();

      findAllFlux
          .thenMany(deleteMono)
          .thenMany(productFlux)
          .thenMany(findAllFlux)
          .subscribe(System.out::println);
    };
  }
}
