package nl.han.ica.icss.transforms;


import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;

public class Evaluator implements Transform {

    private final IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        storeVariables(ast.root);
        checkExpression(null, ast.root);
    }


    public void checkExpression(ASTNode parent, ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            checkExpression(node, child);
        }

        evaluateExpression(parent, node);
    }

    public void evaluateExpression(ASTNode parent, ASTNode node) {

        if (!(node instanceof Operation op) || parent == null) return;

        Literal left = getLiteral(op.lhs);
        Literal right = getLiteral(op.rhs);

        if (node instanceof MultiplyOperation) {
            Literal newLiteral = evaluateLiteral(left, right, "*");
            replaceChild(parent, node, newLiteral);
        }

        if (node instanceof AddOperation) {
            Literal newLiteral = evaluateLiteral(left, right, "+");
            replaceChild(parent, node, newLiteral);
        }

        if (node instanceof SubtractOperation) {
            Literal newLiteral = evaluateLiteral(left, right, "-");
            replaceChild(parent, node, newLiteral);
        }
    }


    // ----- HELPER FUNCTIONS ------

    private Literal evaluateLiteral(Literal first, Literal second, String operation) {
        if ("+".equals(operation) || "-".equals(operation)) {
            return evaluateAddOrSubtract(first, second, operation);
        }

        if ("*".equals(operation)) {
            return evaluateWithScalar(first, second);
        }

        return null;
    }

    private Literal evaluateAddOrSubtract(Expression first, Expression second, String operation) {
        if (first instanceof PixelLiteral pixelFirst && second instanceof PixelLiteral pixelSecond) {
            return new PixelLiteral(evaluate(pixelFirst.value, pixelSecond.value, operation));
        }
        if (first instanceof PercentageLiteral percentageFirst && second instanceof PercentageLiteral percentageSecond) {
            return new PercentageLiteral(evaluate(percentageFirst.value, percentageSecond.value, operation));
        }

        return null;
    }

    private Literal evaluateWithScalar(Literal first, Literal second) {
        if (first instanceof PixelLiteral pixel && second instanceof ScalarLiteral scalar) {
            return new PixelLiteral(evaluate(pixel.value, scalar.value, "*"));
        }
        if (first instanceof PercentageLiteral percentage && second instanceof ScalarLiteral scalar) {
            return new PercentageLiteral(evaluate(percentage.value, scalar.value, "*"));
        }
        if (second instanceof PixelLiteral pixel && first instanceof ScalarLiteral scalar) {
            return new PixelLiteral(evaluate(pixel.value, scalar.value, "*"));
        }
        if (second instanceof PercentageLiteral percentage && first instanceof ScalarLiteral scalar) {
            return new PercentageLiteral(evaluate(percentage.value, scalar.value, "*"));
        }

        return null;
    }

    private int evaluate(int first, int second, String operation) {
        return switch (operation) {
            case "+" -> first + second;
            case "-" -> first - second;
            case "*" -> first * second;
            default -> throw new ArithmeticException();
        };
    }

    private Literal getLiteral(Expression expression) {
        if (expression instanceof BoolLiteral) return new BoolLiteral(((BoolLiteral) expression).value);
        if (expression instanceof PercentageLiteral)
            return new PercentageLiteral(((PercentageLiteral) expression).value);
        if (expression instanceof PixelLiteral) return new PixelLiteral(((PixelLiteral) expression).value);
        if (expression instanceof ScalarLiteral) return new ScalarLiteral(((ScalarLiteral) expression).value);

        if (expression instanceof VariableReference reference) {
            for (int i = 0; i < variableValues.getSize(); i++) {
                if (variableValues.get(i).containsKey(reference.name)) {
                    return getLiteral(variableValues.get(i).get(reference.name));
                }
            }
        }

        if (expression instanceof Operation op) {
            Literal left = getLiteral(op.lhs);
            Literal right = getLiteral(op.rhs);

            if (left == null || right == null) return null;

            if (op instanceof MultiplyOperation) {
                return evaluateLiteral(left, right, "*");
            }
            if (op instanceof AddOperation) {
                return evaluateLiteral(left, right, "+");
            }
            if (op instanceof SubtractOperation) {
                return evaluateLiteral(left, right, "-");
            }
        }

        return null;
    }

    private boolean isClauseTrue(Expression expression) {
        if (expression instanceof BoolLiteral bool) {
            return bool.value;
        } else if (expression instanceof VariableReference reference) {
            for (int i = 0; i < variableValues.getSize(); i++) {
                if (variableValues.get(i).containsKey(reference.name)) {
                    Literal value = variableValues.get(i).get(reference.name);
                    if (value instanceof BoolLiteral bool) return bool.value;
                }
            }
        }

        return false;
    }

    private void storeVariables(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableAssignment assignment) {
                Expression expression = assignment.expression;
                HashMap<String, Literal> map = new HashMap<>();
                map.put(assignment.name.name, getLiteral(expression));
                variableValues.addFirst(map);
            }
            storeVariables(child);
        }
    }

    private void replaceChild(ASTNode parent, ASTNode oldChild, Literal newChild) {
        if (parent instanceof Declaration d && d.expression == oldChild) {
            d.expression = newChild;
        } else if (parent instanceof VariableAssignment v && v.expression == oldChild) {
            v.expression = newChild;
        } else if (parent instanceof Operation op) {
            if (op.lhs == oldChild) op.lhs = newChild;
            else if (op.rhs == oldChild) op.rhs = newChild;
        }
    }


}
