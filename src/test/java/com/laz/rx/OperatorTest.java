package com.laz.rx;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OperatorTest {

  @Test
  void map() {
    // the function applied in map is executed synchronously
    Flux.range(1, 5).log().map(i -> i * 10).subscribe(System.out::println);
  }

  @Test
  void flatMap() {
    // flatMap function is asynchronous, each execution returns a new flux, all fluxes are combined
    Flux.range(1, 2)
        .log()
        .flatMap(i -> Flux.range(i * 10, 33).log())
        .subscribe(System.out::println);
  }

  @Test
  void flatMap2() {
    Flux.just(1, 50, 100)
        .log()
        .flatMap(i -> Flux.range(i + 1, 33).log())
        .subscribe(System.out::println);
  }

  @Test
  void flatMapMany() {
    // turn a mono into a flux
    Mono.just(3).flatMapMany(i -> Flux.range(1, i)).subscribe(System.out::println);
  }

  @Test
  void concat() throws InterruptedException {
    Flux<Integer> oneToFive = Flux.range(1, 5).delayElements(Duration.ofMillis(400));
    Flux<Integer> sixToTen = Flux.range(6, 5).delayElements(Duration.ofMillis(800));

    Flux.concat(oneToFive, sixToTen).subscribe(System.out::println);

    // equivalent
    // oneToFive.concatWith(sixToTen).subscribe(System.out::println);

    Thread.sleep(7000);
  }

  @Test
  void concatMono() {
    // concatenating mono's makes a flux
    Flux.concat(Mono.just("B"), Mono.just("C")).subscribe(System.out::println);
  }

  @Test
  void merge() throws InterruptedException {
    Flux<Integer> oneToFive = Flux.range(1, 5).delayElements(Duration.ofMillis(400));
    Flux<Integer> sixToTen = Flux.range(6, 5).delayElements(Duration.ofMillis(800));

    Flux.merge(oneToFive, sixToTen).subscribe(System.out::println);

    // equivalent
    // oneToFive.mergeWith(sixToTen).subscribe(System.out::println);

    Thread.sleep(5000);
  }

  @Test
  void zip() {
    Flux<Integer> oneToFive = Flux.range(1, 5);
    Flux<Integer> sixToTen = Flux.range(6, 5);

    Flux.zip(oneToFive, sixToTen, (item1, item2) -> item1 + ", " + item2)
        .subscribe(System.out::println);

    // equivalent except output is of type Tuple2
    // oneToFive.zipWith(sixToTen).subscribe(System.out::println);
  }
}
