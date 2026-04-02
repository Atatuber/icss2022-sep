package nl.han.ica.icss.generator;


import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

import java.util.HashMap;

public class Generator {

	private final IHANLinkedList<HashMap<String, Literal>> variableValues;

	private static final String SINGLE_SPACE = " ";
	private static final String DOUBLE_SPACE = "  "; // GE02

	public Generator() {
		variableValues = new HANLinkedList<>();
		variableValues.addFirst(new HashMap<>());
	}

	public String generate(AST ast) {
		StringBuilder builder = new StringBuilder();

		generateNode(ast.root, builder);

        return builder.toString();
	}

	// GE01
	public void generateNode(ASTNode node, StringBuilder builder) {
		if (node == null) return;

		boolean opensScope = node instanceof Stylerule || node instanceof IfClause || node instanceof ElseClause;

		if (opensScope) {
			variableValues.addFirst(new HashMap<>());
		}

		storeVariables(node);

		if(node instanceof Stylerule stylerule) {
			if(stylerule.body.isEmpty()) return;

			for(Selector selector : stylerule.selectors) {
				builder.append(selector).append(SINGLE_SPACE).append("{ \n");
			}

			for(ASTNode child : stylerule.getChildren()) {
				if(child instanceof Declaration declaration) {
					// GE02 Implementation
					builder.append(DOUBLE_SPACE).append(makeDeclarationString(declaration)).append("\n");
				}
			}

            builder.append("}\n");
		} else {
			for (ASTNode child : node.getChildren()) {
				generateNode(child, builder);
			}

			if(opensScope) {
				variableValues.removeFirst();
			}
		}
	}

	// ----- HELPER FUNCTIONS -----

	private String makeDeclarationString(Declaration node) {
		return node.property.name + ": " + makeCssValue(resolveLiteral(node.expression));
	}

	private String makeCssValue(Literal literal) {
		StringBuilder builder = new StringBuilder();
		if (literal instanceof ColorLiteral cl) builder.append(cl.value);
		if (literal instanceof PixelLiteral pl) builder.append(pl.value).append("px");
		if (literal instanceof PercentageLiteral pl) builder.append(pl.value).append("%");

		builder.append(";");

		return builder.toString();
	}

	private void storeVariables(ASTNode node) {
		if (node instanceof VariableAssignment assignment) {
			Literal value = resolveLiteral(assignment.expression);
			variableValues.get(0).put(assignment.name.name, value);
		}
	}

	private Literal resolveLiteral(Expression expression) {
		if (expression instanceof Literal literal) return literal;

		if (expression instanceof VariableReference reference) {
			for (int i = 0; i < variableValues.getSize(); i++) {
				if (variableValues.get(i).containsKey(reference.name)) {
					return variableValues.get(i).get(reference.name);
				}
			}
		}

		return null;
	}
}
