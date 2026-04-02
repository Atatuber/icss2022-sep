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

        if (node instanceof VariableAssignment assignment) {
            checkNode(assignment.expression);
            addAssignment(assignment);

            return;
        }

        checkNodeSemantics(node);

        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        if (openendScope) variableTypes.removeFirst();
    }

    public void checkNodeSemantics(ASTNode node) {
        if (node instanceof IfClause ifClause) {
            checkIfClause(ifClause);
        }
        if (node instanceof VariableReference ref) {
            checkReference(ref);
        }
        if (node instanceof Operation operation) {
            checkColorInOperation(operation);
            if (node instanceof AddOperation || node instanceof SubtractOperation) {
                checkOperation(operation, Check.ADDITION);
            } else {
                checkOperation(operation, Check.MULTIPLICATION);
            }
        }
        if (node instanceof Declaration declaration) {
            checkDeclaration(declaration);
        }
    }

    public void addAssignment(VariableAssignment node) {
        if (!node.expression.hasError()) {
            variableTypes.get(0).put(node.name.name, getExpressionType(node.expression));
        }
    }

    // CH01 and CH06
    public void checkReference(VariableReference node) {

        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(node.name)) {
                return;
            }
        }

        node.setError("Variable '" + node.name + "' not found. Please use an existing variable.");
    }

    // CH02
    public void checkOperation(Operation node, Check checkType) {
        if (node.hasError()) return;

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
        boolean pixelAddition = left == ExpressionType.PIXEL && right == ExpressionType.PIXEL;
        boolean percentageAddition = left == ExpressionType.PERCENTAGE && right == ExpressionType.PERCENTAGE;
        return pixelAddition || percentageAddition;
    }

    private boolean isMultiplicationValid(ExpressionType left, ExpressionType right) {
        boolean leftScalar = left == ExpressionType.SCALAR;
        boolean rightScalar = right == ExpressionType.SCALAR;
        return leftScalar || rightScalar;
    }

    // CH03
    public void checkColorInOperation(Operation node) {
        if (operationContainsColor(node)) {
            node.setError("Invalid operation with color '" + node.lhs.toString() + "'. Please use correct format for operations.");
        }
    }

    private boolean operationContainsColor(Operation node) {
        return node.lhs != null && getExpressionType(node.lhs) == ExpressionType.COLOR
                || node.rhs != null && getExpressionType(node.rhs) == ExpressionType.COLOR;
    }

    //CH04
    public void checkDeclaration(Declaration node) {
        if (node.hasError()) return;

        if (!isDeclarationValid(node)) {
            node.setError("Declaration '" + node + "' is invalid. Please use correct declarations.");
        }
    }

    private boolean isDeclarationValid(Declaration node) {
        String property = node.property.name;
        ExpressionType expressionType = getExpressionType(node.expression);

        boolean isColorProperty = property.equals("background-color") || property.equals("color");
        boolean isMetricProperty = property.equals("width") || property.equals("height");

        boolean isColorValid = isColorProperty && expressionType == ExpressionType.COLOR;
        boolean isMetricValid = isMetricProperty && expressionType == ExpressionType.PIXEL
                || isMetricProperty && expressionType == ExpressionType.PERCENTAGE;

        return isColorValid || isMetricValid;
    }

    //CH05
    public void checkIfClause(IfClause node) {
        ExpressionType expressionType = getExpressionType(node.conditionalExpression);

        if (expressionType != ExpressionType.BOOL) {
            node.setError("If clause has invalid condition: " + node.conditionalExpression + ". Please use a condition that is of type BOOLEAN.");
        }
    }

    private ExpressionType getExpressionType(ASTNode node) {

        if (node instanceof ColorLiteral) return ExpressionType.COLOR;
        if (node instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        if (node instanceof PixelLiteral) return ExpressionType.PIXEL;
        if (node instanceof ScalarLiteral) return ExpressionType.SCALAR;
        if (node instanceof BoolLiteral) return ExpressionType.BOOL;

        if (node instanceof VariableReference ref) {
            for (int i = 0; i < variableTypes.getSize(); i++) {
                if (variableTypes.get(i).containsKey(ref.name)) {
                    return variableTypes.get(i).get(ref.name);
                }
            }
        }

        if (node instanceof MultiplyOperation multiplyOperation) {
            ExpressionType left = getExpressionType(multiplyOperation.lhs);
            ExpressionType right = getExpressionType(multiplyOperation.rhs);

            if (left == ExpressionType.SCALAR && right == ExpressionType.SCALAR) return ExpressionType.SCALAR;
            if (left == ExpressionType.SCALAR) return right;
            if (right == ExpressionType.SCALAR) return left;

            return ExpressionType.UNDEFINED;
        }

        if (node instanceof AddOperation addOperation) {
            return getExpressionType(addOperation.lhs);
        }

        if (node instanceof SubtractOperation subtractOperation) {
            return getExpressionType(subtractOperation.lhs);
        }

        return ExpressionType.UNDEFINED;
    }
}
