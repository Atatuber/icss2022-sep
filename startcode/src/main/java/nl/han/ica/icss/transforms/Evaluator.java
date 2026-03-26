package nl.han.ica.icss.transforms;


import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.HashMap;

public class Evaluator implements Transform {

    private final IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        storeBooleanVariables(ast.root);
    }


    // ----- HELPER FUNCTIONS ------

    private Literal getBoolLiteral(Expression expression) {
        if (expression instanceof BoolLiteral) return new BoolLiteral(((BoolLiteral) expression).value);

        return null;
    }

    private boolean isClauseTrue(Expression expression) {
        if (expression instanceof BoolLiteral bool) {
            return bool.value;
        } else if (expression instanceof VariableReference reference) {
            for(int i = 0; i < variableValues.getSize(); i++) {
                if(variableValues.get(i).containsKey(reference.name)) {
                    Literal value = variableValues.get(i).get(reference.name);
                    if (value instanceof BoolLiteral bool) return bool.value;
                }
            }
        }

        return false;
    }

    public void storeBooleanVariables(ASTNode node) {
        for(ASTNode child : node.getChildren()) {
            if(child instanceof VariableAssignment assignment) {
                Expression expression = assignment.expression;
                if (expression instanceof BoolLiteral literal) {
                    HashMap<String, Literal> map = new HashMap<>();
                    map.put(assignment.name.name, getBoolLiteral(literal));
                    variableValues.addFirst(map);
                }
            }
        }
    }
}
