package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.operations.AddOperation;
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
        if(node == null) return;

        boolean openendScope = false;

        if(node instanceof IfClause || node instanceof ElseClause) {
            variableTypes.addFirst(new HashMap<>());
            openendScope = true;
        }

        if(node instanceof VariableAssignment) {
            addAssignments(((VariableAssignment) node).name.name);
        }
        if(node instanceof VariableReference) {
            checkReferences(((VariableReference) node).name, (VariableReference) node);
        }
        if(node instanceof Operation) {
            checkAdditionSemantics((Operation) node);
        }

        for(ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        if(openendScope) {
            variableTypes.removeFirst();
        }
    }

    public void addAssignments(String nodeName) {
        variableTypes.get(0).put(nodeName, ExpressionType.UNDEFINED);
    }

    public void checkReferences(String nodeName, VariableReference current) {
        if(!variableTypes.get(0).containsKey(nodeName)) {
            current.setError("Variable '" + nodeName + "' not found. Please use an existing variable.");
        }
    }

    public void checkAdditionSemantics(Operation node) {
        if(node.lhs.toString().contains("#")) {
            node.setError("Invalid operation '" + node.lhs.toString() + "'. Please use correct format for operations.");
        } else if( node.rhs.toString().contains("#")) {
            node.setError("Invalid operation '" + node.rhs.toString() + "'. Please use correct format for operations.");
        }
    }
}
