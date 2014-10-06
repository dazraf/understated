# understated
* A simple state-machine for building command shells. 
* Supports dynamic state transitions and sub-state command inheritance. 
* Java 8.
* Should work nicely with other libraries e.g. jline, rx-java 
* Thanks to: [stateless4j](https://github.com/oxo42/stateless4j)

## intro
Usage of the library centres around the use of `CommandStateMachineBuilder` and `CommandStateMachine`.

Your application will present a set of states as an enumeration e.g.


```
  private enum States {
    App, AppNotLoggedIn, AppLoggedIn, GameStarted, GameFinished, AppFinished
  };
```


The general pattern of use is then:

1. create a builder
2. sprinkle a little code for state transitions and callbacks
3. create the state machine and feed in commands

## creating the builder

Call the static `builder` method on the `CommandStateMachine` interface, passing the initial state of the machine.


```
  CommandStateMachineBuilder<States> smb 
    = CommandStateMachine.builder(States.AppNotLoggedIn);
```

## a little code for state transitions and callbacks

For each interesting state, prepare its configuration

```
   smb.configure(States.AppLoggedIn)
```

and all the respective transitions and callbacks...

### state transitions

```
  smb.configure(States.AppLoggedIn)
  .addCommand("logout", States.AppNotLoggedIn, ci -> {
    // do something dynamic here ... 
    return true;
  })
  .addCommand("start", States.GameStarted, ci -> {
    // ...
```

Each `addCommand` will define a state transition when the statemachine encounters the given command. The method carries as a last parameter a predicate that returns true if and only if at runtime the transition is valid. If it is not valid, the transition is not carried out.

### callbacks

`understated` supports just two types of callbacks: on state entry and exit. Here's an example of state entry:

```
  smb.configure(States.AppNotLoggedIn)
      .addCommand("login", States.AppLoggedIn, ci -> {
        return true;
      })
      
      .addOnEntry(
          ci -> {
            println("entering " + ci.getState().name());
          });
```

### substates and command inheritance

A state can be made to be within the context of a super state. For example, the following states that GameStarted is part of the AppLoggedIn state. 

```
    smb.configure(States.GameStarted)
        .subStateOf(States.AppLoggedIn)
        .addCommand("stop", States.GameFinished, ci -> {
        ...
```

A substate command will inherit *all* commands from its super state hierarchy. In the above example, whilst we're playing the game, we can decide to execute a command in the AppLoggedIn state e.g. logout.

## creating the state machine and feeding commands
Create the statemachine with a simple call to `build`
```
  CommandStateMachine<States> sm = smb.build();
```

`understated` expects commands as a name and an array of string parameters e.g.

```
    sm.fire("login", new String[] { "fuzz", "1232" });
    sm.fire("start", new String[] { "@fuzz" });
    sm.fire("stop");
    sm.fire("logout");
```
