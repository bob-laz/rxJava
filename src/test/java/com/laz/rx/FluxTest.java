package com.laz.rx;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

public class FluxTest {

  @Test
  void firstFlux() {
    Flux.just("A", "B", "C").log().subscribe();
  }

  @Test
  void fluxArray() {
    Flux.fromArray(new String[] {"A", "B", "C"}).log().subscribe();
  }

  @Test
  void fluxIterable() {
    Flux.fromIterable(Arrays.asList("A", "B", "C")).log().subscribe();
  }

  @Test
  void fluxRange() {
    Flux.range(5, 10).log().subscribe();
  }

  @Test
  void fluxInterval() throws InterruptedException {
    Flux.interval(Duration.ofSeconds(1)).log().subscribe();
    Thread.sleep(5000);
  }

  @Test
  void fluxIntervalWithTake() throws InterruptedException {
    // not that take is not backpressure, it just takes n elements then cancels the subscription
    Flux.interval(Duration.ofSeconds(1)).log().take(2).subscribe();
    Thread.sleep(5000);
  }

  @Test
  void fluxRequest() {
    // only requesting 3 elements, example of backpressure
    // onComplete not called because stream has 5 elements and we only request 3
    Flux.range(1, 5).log().subscribe(null, null, null, s -> s.request(3));
  }

  @Test
  void fluxCustomSubscriber() {
    // request a random number of elements every time
    Flux.range(1, 10)
        .log()
        .subscribe(
            new BaseSubscriber<>() {
              int elementsToProcess = 3;
              int counter = 0;

              public void hookOnSubscribe(Subscription subscription) {
                System.out.println("Subscribed!");
                request(elementsToProcess);
              }

              public void hookOnNext(Integer value) {
                counter++;
                if (counter == elementsToProcess) {
                  counter = 0;

                  Random r = new Random();
                  elementsToProcess = r.ints(1, 4).findFirst().getAsInt();
                  request(elementsToProcess);
                }
              }
            });
  }

  @Test
  void fluxLimitRate() {
    // request a fixed number of elements every time
    Flux.range(1, 5).log().limitRate(3).subscribe();
  }
}
