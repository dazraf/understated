package fuzz.understated;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Action1;
import com.github.oxo42.stateless4j.delegates.Action2;
import com.github.oxo42.stateless4j.delegates.Func3;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.triggers.TriggerWithParameters2;

public class CommandStateMachineBuilder<TState> {
    private final Map<TState, StateConfiguration> configurations = new HashMap<>();
    private final TState initialState;

    private enum Triggers {
	FireCommand
    };

    public class StateConfiguration {
	private final List<CommandCallback<TState>> onEntryCallbacks = new LinkedList<>();
	private final List<CommandCallback<TState>> onLeaveCallbacks = new LinkedList<>();
	private final Map<String, CommandBinding<TState>> commands = new HashMap<>();
	private final TState srcState;
	private TState parentState;

	StateConfiguration(TState srcState) {
	    this.srcState = srcState;
	}

	public StateConfiguration addOnEntry(CommandCallback<TState> callback) {
	    onEntryCallbacks.add(callback);
	    return this;
	}

	public StateConfiguration addOnExit(CommandCallback<TState> callback) {
	    onLeaveCallbacks.add(callback);
	    return this;
	}

	public StateConfiguration addCommand(String command, TState state,
		Predicate<CommandInvocation<TState>> check) {
	    commands.put(command, new CommandBinding<>(command, state, check));
	    return this;
	}

	public StateConfiguration subStateOf(TState parentState) {
	    this.parentState = parentState;
	    return this;
	}

	void onEntry(TState state, String command, String[] parameters) {
	    CommandInvocation<TState> ci = new CommandInvocation<>(state,
		    command, parameters);

	    for (CommandCallback<TState> cb : onEntryCallbacks) {
		cb.callback(ci);
	    }
	}

	void onLeave(TState state, String command, String[] parameters) {
	    CommandInvocation<TState> ci = new CommandInvocation<>(state,
		    command, parameters);

	    for (CommandCallback<TState> cb : onLeaveCallbacks) {
		cb.callback(ci);
	    }
	}

	Iterable<CommandBinding<TState>> getCommands() {
	    return commands.values();
	}

	Iterable<CommandCallback<TState>> getOnEntryCallbacks() {
	    return onEntryCallbacks;
	}

	Iterable<CommandCallback<TState>> getOnLeaveCallbacks() {
	    return onLeaveCallbacks;
	}

	TState check(String command, String[] params) {
	    CommandBinding<TState> cb = commands.get(command);
	    assert (cb != null);
	    if (cb != null && cb.check(srcState, command, params))
		return cb.getDestState();
	    else if (parentState != null) {
		return configurations.get(parentState).check(command, params);
	    } else {
		return srcState;
	    }
	}

    }

    public CommandStateMachineBuilder(TState initialState) {
	this.initialState = initialState;
    }

    public StateConfiguration configure(TState state) {
	StateConfiguration c = configurations.get(state);
	if (c == null) {
	    c = new StateConfiguration(state);
	    configurations.put(state, c);
	}
	return c;
    }

    public CommandStateMachine<TState> build() {

	final StateMachineConfig<TState, Triggers> smc = new StateMachineConfig<>();

	final TriggerWithParameters2<String, String[], TState, Triggers> triggerWithParams = smc
		.setTriggerParameters(Triggers.FireCommand, String.class,
			String[].class);

	for (Map.Entry<TState, StateConfiguration> entry : configurations
		.entrySet()) {
	    TState srcState = entry.getKey();
	    StateConfiguration conf = entry.getValue();

	    com.github.oxo42.stateless4j.StateConfiguration<TState, Triggers> sc = smc
		    .configure(srcState);

	    sc.permitDynamic(triggerWithParams,
		    new Func3<String, String[], TState>() {
			@Override
			public TState call(String command, String[] params) {
			    return conf.check(command, params);
			}
		    });

	    sc.onEntryFrom(triggerWithParams, new Action2<String, String[]>() {
		@Override
		public void doIt(String command, String[] params) {
		    conf.onEntry(srcState, command, params);
		}
	    }, String.class, String[].class);

	    sc.onExit(new Action1<Transition<TState, Triggers>>() {

		@Override
		public void doIt(Transition<TState, Triggers> arg1) {
		    conf.onLeave(srcState, null, null);
		}
	    });

	}

	final StateMachine<TState, Triggers> sm = new StateMachine<>(
		initialState, smc);

	return new CommandStateMachine<TState>() {

	    @Override
	    public void fire(String command, String[] parameters) {
		sm.fire(triggerWithParams, command, parameters);
	    }

	    @Override
	    public void fire(String command) {
		sm.fire(triggerWithParams, command, null);
	    }

    };
  }
}