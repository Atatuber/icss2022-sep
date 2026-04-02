package nl.han.ica.icss.transforms;


import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private final IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues.addFirst(new HashMap<>());
        transformNode(ast.root);
    }

    private void transformNode(ASTNode node) {
        if (node == null) return;

        ArrayList<ASTNode> body = getBody(node);

        boolean opensScope = node instanceof Stylesheet || node instanceof Stylerule;

        if (opensScope && !(node instanceof Stylesheet)) {
            variableValues.addFirst(new HashMap<>());
        }

        if (body != null) {
            for (int i = 0; i < body.size(); i++) {
                ASTNode child = body.get(i);

                if (child instanceof IfClause ifClause) {
                    transformIfElse(node, ifClause);
                    i--; // Get on current scope, because if scope got deleted.
                    continue;
                }

                if (child instanceof VariableAssignment assignment) {
                    handleVariableAssignment(assignment);
                    continue;
                }

                if (child instanceof Declaration declaration) {
                    handleDeclaration(declaration);
                    continue;
                }

                transformNode(child);
            }
        } else {
            for (ASTNode child : node.getChildren()) {
                transformNode(child);
            }
        }

        if (opensScope && !(node instanceof Stylesheet)) {
            variableValues.removeFirst();
        }
    }

    // TR01
    private void handleDeclaration(Declaration declaration) {
        declaration.expression = getLiteral(declaration.expression);
    }

    // TR02
    private void transformIfElse(ASTNode parent, IfClause ifClause) {
        boolean condition = isClauseTrue(ifClause.conditionalExpression);

        ArrayList<ASTNode> replacement = condition
                ? ifClause.body
                : (ifClause.elseClause != null
                   ? ifClause.elseClause.body : new ArrayList<>());

        replaceBody(parent, ifClause, replacement);
    }

    // ----- HELPER FUNCTIONS ------

    private Literal getLiteral(Expression expression) {
        if (expression instanceof BoolLiteral) return new BoolLiteral(((BoolLiteral) expression).value);
        if (expression instanceof PercentageLiteral)
            return new PercentageLiteral(((PercentageLiteral) expression).value);
        if (expression instanceof PixelLiteral) return new PixelLiteral(((PixelLiteral) expression).value);
        if (expression instanceof ScalarLiteral) return new ScalarLiteral(((ScalarLiteral) expression).value);
        if (expression instanceof ColorLiteral) return new ColorLiteral(((ColorLiteral) expression).value);

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

    private void handleVariableAssignment(VariableAssignment assignment) {
        Literal value = getLiteral(assignment.expression);
        if (value == null) return;

        assignment.expression = value;
        variableValues.get(0).put(assignment.name.name, value);
    }

    private void replaceBody(ASTNode parent, ASTNode replacing, ArrayList<ASTNode> replacement) {
        ArrayList<ASTNode> body = getBody(parent);

        if (body == null) return;

        int index = body.indexOf(replacing); // Specific index to keep order

        body.remove(index);
        body.addAll(index, replacement);
    }

    private ArrayList<ASTNode> getBody(ASTNode node) {
        if (node instanceof Stylesheet stylesheet) return stylesheet.body;
        if (node instanceof Stylerule stylerule) return stylerule.body;
        if (node instanceof IfClause ifClause) return ifClause.body;
        if (node instanceof ElseClause elseClause) return elseClause.body;

        return null;
    }

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
        if (first instanceof ScalarLiteral scalar && second instanceof ScalarLiteral secondScalar) {
            return new ScalarLiteral(evaluate(scalar.value, secondScalar.value, "*"));
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
}
