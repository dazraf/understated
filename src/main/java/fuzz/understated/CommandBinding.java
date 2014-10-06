package fuzz.understated;

import java.util.function.Predicate;

class CommandBinding<TState> {
  private String command;
  private TState destState;
  private Predicate<CommandInvocation<TState>> check;

  public CommandBinding(String command, TState destState,
      Predicate<CommandInvocation<TState>> check) {
    this.command = command;
    this.destState = destState;
    this.check = check;
  }

  public Predicate<CommandInvocation<TState>> getCheck() {
    return check;
  }

  public String getCommand() {
    return command;
  }

  public TState getDestState() {
    return destState;
  }

  public boolean check(TState srcState, String command, String[] parameters) {
    return check == null
        || check.test(new CommandInvocation<TState>(srcState, command,
            parameters));
  }
}
