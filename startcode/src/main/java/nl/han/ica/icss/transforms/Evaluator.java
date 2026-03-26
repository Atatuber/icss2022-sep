package nl.han.ica.icss.transforms;


import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        //variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        //variableValues = new HANLinkedList<>();
        editNode(null, ast.root);
    }

    public void editNode(ASTNode parent, ASTNode node) {
        if (node instanceof IfClause ifClause) {

            boolean expression = isClauseTrue(ifClause.conditionalExpression);
            if(expression) {
                simplifyClause(parent, node);
            } else if (ifClause.getElseClause() != null) {
                simplifyClause(parent, ifClause.getElseClause());
            }

            removeClause(parent, ifClause);
            return;
        }

        for (ASTNode child : node.getChildren()) {
            editNode(node, child);
        }
    }

    private boolean isClauseTrue(Expression expression) {
        if (expression instanceof BoolLiteral bool) {
            return bool.value;
        }

        return false;
    }

    // ---- HELPER FUNCTIONS ----

    public void simplifyClause(ASTNode parent, ASTNode clause) {
        if (clause instanceof IfClause ifClause) {
            for (int i = 0; i < ifClause.body.size(); i++) {
                ASTNode child = ifClause.body.get(i);
                checkChildrenClause(parent, child);
            }
        } else if (clause instanceof ElseClause elseClause) {
            for (int i = 0; i < elseClause.body.size(); i++) {
                ASTNode child = elseClause.body.get(i);
                checkChildrenClause(parent, child);
            }
        }
    }

    public void checkChildrenClause(ASTNode parent, ASTNode child) {
        if (child instanceof IfClause childIf) {

            if (isClauseTrue(childIf.conditionalExpression)) {
                simplifyClause(parent, childIf);
            } else if (childIf.getElseClause() != null) {
                simplifyClause(parent, childIf.getElseClause());
            }

        } else if (child instanceof ElseClause childElse) {
            simplifyClause(parent, childElse);
        } else {
            parent.addChild(child);
        }
    }

    public void removeClause(ASTNode parent, ASTNode clause) {
        if (clause instanceof IfClause || clause instanceof ElseClause) {
            if (parent instanceof Stylerule stylerule) {
                stylerule.body.remove(clause);
            } else if (parent instanceof IfClause ifClause) {
                ifClause.body.remove(clause);
            } else if (parent instanceof ElseClause elseClause) {
                elseClause.body.remove(clause);
            }
        }
    }

}
