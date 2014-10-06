package fuzz.understated;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import fuzz.understated.CommandInvocation;
import fuzz.understated.CommandStateMachine;
import fuzz.understated.CommandStateMachineBuilder;

public class CLITests {

  // declare the application states
  private enum States {
    App, AppNotLoggedIn, AppLoggedIn, GameStarted, GameFinished, AppFinished
  };

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testSimpleGameApp() {
    CommandStateMachineBuilder<States> smb = getBuilder();

    final AtomicInteger loggedRefs = new AtomicInteger();
    final AtomicInteger gameRefs = new AtomicInteger();

    smb.configure(States.AppNotLoggedIn)
        .addCommand("login", States.AppLoggedIn, ci -> {
          printCommandInvocation(ci);
          println("ok");
          return true;
        })

        .addOnEntry(
            ci -> {
              println("entering ", ci.getState().name(), " ",
                  loggedRefs.decrementAndGet());
            });

    smb.configure(States.AppLoggedIn)
        .addCommand("logout", States.AppNotLoggedIn, ci -> {
          printCommandInvocation(ci);
          return true;
        })
        .addCommand("start", States.GameStarted, ci -> {
          printCommandInvocation(ci);
          return true;
        })
        .addOnEntry(
            ci -> {
              println("entering ", ci.getState().name(), " ",
                  loggedRefs.incrementAndGet());
            });

    smb.configure(States.GameStarted)
        .subStateOf(States.AppLoggedIn)
        .addCommand("stop", States.GameFinished, ci -> {
          printCommandInvocation(ci);
          return true;
        })
        .addOnEntry(
            ci -> {
              println("entering ", ci.getState().name(), " ",
                  gameRefs.incrementAndGet());
            });

    smb.configure(States.GameFinished)
        .subStateOf(States.AppLoggedIn)
        .addOnEntry(
            ci -> {
              println("entering ", ci.getState().name(), " ",
                  gameRefs.decrementAndGet());
            });

    CommandStateMachine<States> sm = smb.build();
    assertEquals(loggedRefs.get(), 0);
    sm.fire("login", new String[] { "fuzz", "1232" });
    assertEquals(loggedRefs.get(), 1);
    sm.fire("start", new String[] { "@fuzz" });
    assertEquals(gameRefs.get(), 1);
    sm.fire("stop");
    assertEquals(gameRefs.get(), 0);
    sm.fire("logout");
    assertEquals(loggedRefs.get(), 0);
  }

  private CommandStateMachineBuilder<States> getBuilder() {
    CommandStateMachineBuilder<States> smb = CommandStateMachine
        .builder(States.AppNotLoggedIn);
    return smb;
  }

  @Test
  public void testHelpSystem() {
    CommandStateMachineBuilder<States> smb = getBuilder();
    smb.configure(States.AppNotLoggedIn)
        .addCommand("login", States.AppLoggedIn, ci -> {
          printCommandInvocation(ci);
          println("ok");
          return true;
        }).addOnEntry(ci -> {
          println("entering ", ci.getState().name());
        });

    smb.configure(States.AppLoggedIn)
        .addCommand("logout", States.AppNotLoggedIn, ci -> {
          printCommandInvocation(ci);
          return true;
        }).addCommand("start", States.GameStarted, ci -> {
          printCommandInvocation(ci);
          return true;
        }).addOnEntry(ci -> {
          println("entering ", ci.getState().name());
        });

    smb.configure(States.GameStarted).subStateOf(States.AppLoggedIn)
        .addCommand("stop", States.GameFinished, ci -> {
          printCommandInvocation(ci);
          return true;
        }).addOnEntry(ci -> {
          println("entering ", ci.getState().name(), " ");
        });

    smb.configure(States.GameFinished).subStateOf(States.AppLoggedIn)
        .addOnEntry(ci -> {
          println("entering ", ci.getState().name());
        });

    CommandStateMachine<States> sm = smb.build();
    sm.fire("login", new String[] { "fuzz", "1232" });
    sm.fire("help"); // we expect to see a
    sm.fire("start", new String[] { "@fuzz" });
    sm.fire("stop");
    sm.fire("logout");

  }

  private void printCommandInvocation(CommandInvocation<States> ci) {
    println("command ", ci.getCommand(), "called with", ci.getParameters());
  }

  private static void println(Object... args) {
    for (Object p : args) {
      if (p == null) {
        System.out.print("<>");
      } else if (p.getClass().isArray()) {
        printArray((Object[]) p);
      } else {
        System.out.print(p);
      }
    }
    System.out.println();
  }

  private static void printArray(Object[] a) {
    println();
    for (Object p : a) {
      println("  " + p.toString());
    }
  }
}
