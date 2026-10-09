package game.gal;

import java.util.List;
import java.util.Random;

import gal.ast.Actions;
import gal.ast.Automaton;
import gal.ast.BinaryOp;
import gal.ast.Condition;
import gal.ast.Expression;
import gal.ast.FunCall;
import gal.ast.Mode;
import gal.ast.Parameter;
import gal.ast.Transition;
import gal.ast.UnaryOp;

public final class GalRuntime {

	// Fields
	private final Automaton automate;// construit par le parseur
	private final GalGameContext context;// contexte du jeu
	private final Random random; // pour laction aleatoire

	private String current;

	// Constructeur
	public GalRuntime(Automaton automate, GalGameContext context) {
		this.automate = automate;
		this.context = context;
		this.random = new Random();

		this.current = automate.initial_state.name;// defini dans .gal
	}

	// Getter
	public String currentState() {
		return this.current;
	}

	// exexute la prochaine step de lautomate
	public void step() {
		Mode mode = currentMode();
		if (mode == null || mode.behaviour == null) {
			this.context.waitAction();
			return;
		}
		for (Transition transition : mode.behaviour.transitions) {
			if (evaluateCondition(transition.condition)) {
				executeActions(transition.action);// first transition that follows condion
				this.current = transition.target.name;
				return;
			}
		}
		this.context.waitAction();// si aucune transition wait
	}

	// retourne le mode correspondant
	private Mode currentMode() {
		for (Mode mode : automate.modes) {
			if (mode.state.name.equals(current)) {
				return mode;
			}
		}
		return null;
	}

	// to evaluate a GAL condition we evaluate the expression
	private boolean evaluateCondition(Condition condition) {
		if (condition == null) {
			return false;
		}
		return evaluateExpression(condition.expression());
	}

	private boolean evaluateExpression(Expression expression) {
		if (expression == null) {
			return false;
		}
		if (expression instanceof BinaryOp) {
			BinaryOp binaryOp = (BinaryOp) expression;

			if ("&".equals(binaryOp.operator)) {
				return evaluateExpression(binaryOp.left_operand) && evaluateExpression(binaryOp.right_operand);
			}
			if ("/".equals(binaryOp.operator)) {
				return evaluateExpression(binaryOp.left_operand) || evaluateExpression(binaryOp.right_operand);
			}

			return false;
		}

		if (expression instanceof UnaryOp) {
			UnaryOp unaryOp = (UnaryOp) expression;

			if ("!".equals(unaryOp.operator)) {
				return !evaluateExpression(unaryOp.operand);
			}

			return false;
		}

		if (expression instanceof FunCall call) {
			return evaluateCall(call);
		}

		return false;
	}

	private boolean evaluateCall(FunCall call) {

		if (call == null) {
			return false;
		}

		if ("True".equals(call.name)) {
			return true;
		}

		if ("Cell".equals(call.name) || "Step".equals(call.name)) {
			return this.context.cellCondition(parameter(call, 0), parameter(call, 1));
		}

		if ("Closest".equals(call.name)) {
			return this.context.closestCondition(parameter(call, 0), parameter(call, 1));
		}

		if ("Face".equals(call.name)) {
			return this.context.faces(parameter(call, 0));
		}

		if ("Got".equals(call.name)) {
			return this.context.gotCondition(parameter(call, 0));
		}

		return false;
	}

	// executes the actions of the transition
	private void executeActions(Actions actions) {
		if (actions == null || actions.actions.isEmpty()) {
			this.context.waitAction();
			return;
		}

		// plusieurs actions en sequences
		if (";".equals(actions.operator)) {
			for (FunCall action : actions.actions) {
				executeAction(action);// une action
			}

			return;
		}

		// random
		if ("/".equals(actions.operator)) {
			FunCall action = chooseRandomAction(actions.actions);
			executeAction(action);
			return;
		}

		throw new UnsupportedOperationException("Operateur GAL non supporte : " + actions.operator);
	}

	// chooses randomly an action based on the %
	private FunCall chooseRandomAction(List<FunCall> actions) {
		int total = 0;
		for (FunCall action : actions) {
			total += pourcentage(action);
		}

		if (total <= 0) {
			int randomIndex = random.nextInt(actions.size());
			return actions.get(randomIndex);
		}

		int random = this.random.nextInt(total);
		int sum = 0;

		for (FunCall action : actions) {
			sum += pourcentage(action);
			if (random < sum) {
				return action;
			}
		}

		return actions.get(actions.size() - 1);
	}

	private int pourcentage(FunCall action) {
		if (action.percent == FunCall.NO_PERCENT) {
			return 1;
		}
		return Math.max(0, action.percent);
	}

	// executes a gal action
	private void executeAction(FunCall action) {
		if (action == null) {
			this.context.waitAction();
			return;
		}

		if ("Wait".equals(action.name) || "Rest".equals(action.name)) {
			this.context.waitAction();
			return;
		}

		if ("Move".equals(action.name)) {
			this.context.moveAction(firstParameterOrDefault(action, "F"));
			return;
		}

		if ("Turn".equals(action.name)) {
			this.context.turnAction(firstParameterOrDefault(action, "R"));
			return;
		}
		
		if ("Egg".equals(action.name) || "EGG".equals(action.name)) {
		    this.context.eggAction();
		    return;
		}

		if ("Pop".equals(action.name) || "Wizz".equals(action.name)) {
			this.context.waitAction();
			return;
		}
		this.context.waitAction();// if unknown wait
	}

	// retuens the first param of an action
	private String firstParameterOrDefault(FunCall action, String defaultValue) {
		if (action.parameters.isEmpty()) {
			return defaultValue;
		}

		return parameter(action, 0);
	}

	// returns the param of gal call
	private String parameter(FunCall call, int index) {
		if (index < 0 || index >= call.parameters.size()) {
			return "";
		}

		Parameter parameter = call.parameters.get(index);
		return parameter.toString();
	}
}