package fuzz.cli;

public class CommandInvocation<TState> {
  private TState state;
  private String command;
  private String[] parameters;

  public CommandInvocation(TState state, String command, String[] parameters) {
    this.state = state;
    this.command = command;
    this.parameters = parameters;
  }

  public String getCommand() {
    return command;
  }

  public String[] getParameters() {
    return parameters;
  }

  public TState getState() {
    return state;
  }
}