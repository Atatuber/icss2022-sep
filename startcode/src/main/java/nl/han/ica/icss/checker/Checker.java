package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());
        checkNode(ast.root);

    }

    public void checkNode(ASTNode node) {
        if (node == null) return;

        boolean openendScope = false;

        if (node instanceof Stylerule || node instanceof IfClause || node instanceof ElseClause) {
            variableTypes.addFirst(new HashMap<>());
            openendScope = true;
        }

        if (node instanceof VariableAssignment) {
            addAssignment((VariableAssignment) node);
        }
        if (node instanceof VariableReference ref) {
            checkReference(ref);
        }
        if (node instanceof Operation operation && isNodeOperation(node)) {
            checkColorInOperation(operation);

            if (node instanceof AddOperation || node instanceof SubtractOperation) {
                checkOperation(operation, Check.ADDITION);
            } else {
                checkOperation(operation, Check.MULTIPLICATION);
            }
        }

        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        if (openendScope) {
            variableTypes.removeFirst();
        }
    }

    public void addAssignment(VariableAssignment node) {
        variableTypes.get(0).put(node.name.name, getExpressionType(node.expression));
    }

    // CH01
    public void checkReference(VariableReference node) {

        for (int i = 0; i < variableTypes.getSize(); i++) {
            System.out.println(variableTypes.get(i).containsKey(node.name));
            if (variableTypes.get(i).containsKey(node.name)) {
                return;
            }
        }

        node.setError("Variable '" + node.name + "' not found. Please use an existing variable.");
    }

    // CH02
    public void checkOperation(Operation node, Check checkType) {
        ExpressionType left = getExpressionType(node.lhs);
        ExpressionType right = getExpressionType(node.rhs);

        switch (checkType) {
            case Check.ADDITION -> {
                if (!isAddOrSubtractValid(left, right))
                    node.setError("Additions and subtractions are only allowed with: Pixel + Pixel or Percentage + Percentage. Please try again.");
            }
            case Check.MULTIPLICATION -> {
                if (!isMultiplicationValid(left, right)) {
                    node.setError("Multiplication is invalid. Please try again with at least one Scalar number.");
                }
            }
        }
    }

    private boolean isAddOrSubtractValid(ExpressionType left, ExpressionType right) {
        return (left == ExpressionType.PIXEL && right == ExpressionType.PIXEL)
                || (left == ExpressionType.PERCENTAGE && right == ExpressionType.PERCENTAGE);
    }

    private boolean isMultiplicationValid(ExpressionType left, ExpressionType right) {
        return (left == ExpressionType.SCALAR && (right == ExpressionType.PIXEL || right == ExpressionType.PERCENTAGE))
                || (right == ExpressionType.SCALAR && (left == ExpressionType.PIXEL || left == ExpressionType.PERCENTAGE));
    }

    private ExpressionType getExpressionType(ASTNode node) {

        if (node instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }
        if (node instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }
        if (node instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        }
        if (node instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        if (node instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }

        if (node instanceof VariableReference ref) {
            for (int i = 0; i < variableTypes.getSize(); i++) {
                System.out.println(variableTypes.get(i));
                if (variableTypes.get(i).containsKey(ref.name)) {
                    return variableTypes.get(i).get(ref.name);
                }
            }
        }

        if (node instanceof MultiplyOperation multiplyOperation) {
            if ((multiplyOperation.lhs instanceof ScalarLiteral) && !(multiplyOperation.rhs instanceof ScalarLiteral)) {
                return getExpressionType(multiplyOperation.rhs);
            } else {
                return getExpressionType(multiplyOperation.lhs);
            }
        }

        return ExpressionType.UNDEFINED;
    }

    // CH03 - TODO: Isn't working as intended, need to fix
    public void checkColorInOperation(Operation node) {
        if (operationContainsColor(node)) {
            node.setError("Invalid operation '" + node.lhs.toString() + "'. Please use correct format for operations.");
        }
    }

    private boolean isNodeOperation(ASTNode node) {
        return node instanceof MultiplyOperation || node instanceof AddOperation || node instanceof SubtractOperation;
    }

    private boolean operationContainsColor(Operation node) {
        return node.lhs != null && node.lhs.toString().contains("#")
                || node.rhs != null && node.rhs.toString().contains("#");
    }
}
