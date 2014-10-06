package fuzz.cli;

/**
 * Callback interface for any invocation of commands
 * 
 * @param <TState>
 */
@FunctionalInterface
public interface CommandCallback<TState> {
  void callback(CommandInvocation<TState> invocation);
}