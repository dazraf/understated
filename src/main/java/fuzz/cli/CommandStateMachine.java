package fuzz.cli;

public interface CommandStateMachine<TState> {

  public static <TState> CommandStateMachineBuilder<TState> builder(
      TState initialState) {
    return new CommandStateMachineBuilder<TState>(initialState);
  }

  public void fire(String command, String[] parameters);

  public void fire(String string);
}
