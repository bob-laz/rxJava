package com.laz.rx;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class MonoTest {

  @Test
  void firstMono() {
    Mono.just("A").log().subscribe();
  }

  @Test
  void monoWithConsumer() {
    Mono.just("A").log().subscribe(System.out::println);
  }

  @Test
  void monoWithDoOn() {
    Mono.just("A")
        .log()
        .doOnSubscribe(subs -> System.out.println("Subscribed: " + subs))
        .doOnRequest(req -> System.out.println("Request: " + req))
        .doOnSuccess(complete -> System.out.println("Complete: " + complete))
        .subscribe(System.out::println);
  }

  @Test
  void emptyMono() {
    Mono.empty().log().subscribe(System.out::println);
  }

  @Test
  void emptyMonoOnComplete() {
    Mono.empty().log().subscribe(System.out::println, null, () -> System.out.println("Done"));
  }

  @Test
  void monoWithUncheckedError() {
    Mono.error(new RuntimeException()).log().subscribe();
  }

  /** Unchecked exceptions do not need to be declared with reactive */
  @Test
  void monoWithCheckedError() {
    Mono.error(new Exception())
        .log()
        .subscribe(System.out::println, e -> System.out.println("Error: " + e));
    // equivalent to catching the exception, doing something & rethrowing it
  }

  @Test
  void monoWithDoOnError() {
    // essentially equivalent to previous test case
    Mono.error(new Exception()).doOnError(e -> System.out.println("Error: " + e)).log().subscribe();
  }

  @Test
  void monoSwallowException() {
    Mono.error(new Exception())
        .onErrorResume(
            e -> {
              System.out.println("Caught: " + e);
              return Mono.just("B");
            })
        .log()
        .subscribe();
  }

  @Test
  void monoOnErrorReturn() {
    // just return a value on error instead of calling a function on error
    Mono.error(new Exception()).onErrorReturn("B").log().subscribe();
  }
}
