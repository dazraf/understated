package fuzz.understated;

import java.util.function.Predicate;

class Predicates {
  public static <T> Predicate<T> truePredicate() {
    return new Predicate<T>() {
      @Override
      public boolean test(T t) {
        return true;
      }
    };
  }
}
