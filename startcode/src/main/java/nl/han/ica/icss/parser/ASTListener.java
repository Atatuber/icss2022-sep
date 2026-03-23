package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.types.ExpressionType;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private final AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private final IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }


	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {

		VariableReference reference = new VariableReference(ctx.getChild(0).getText()); // Raw reference
		String value = ctx.getChild(2).getText();

		ExpressionType type = getExpressionType(value);
		Expression expression = getExpression(type, value);

		VariableAssignment assignment = new VariableAssignment();
		assignment.addChild(reference);
		assignment.addChild(expression);


		ast.root.addChild(assignment);
	}

	@Override
	public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
		Stylerule rule = new Stylerule();

		// Push the current node rule to the stack
		currentContainer.push(rule);

		// Add selector to AST
		Selector selector = getSelector(ctx.getChild(0).getText());
		rule.addChild(selector);
	}

	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		ASTNode parent = currentContainer.peek();
		Expression expression = new VariableReference(ctx.expression().getText());

		IfClause ifClause = new IfClause();
		ifClause.addChild(expression);

		parent.addChild(ifClause);
		currentContainer.push(ifClause);
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		ASTNode parent = currentContainer.peek();

		ElseClause elseClause = new ElseClause();
		parent.addChild(elseClause);
		currentContainer.push(elseClause);
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode parent = currentContainer.peek();

		// Add declaration to AST
		String rawProperty = ctx.getChild(0).getText();
		Declaration declaration = new Declaration(rawProperty);

		// Add the declaration to current container and make declaration current
		parent.addChild(declaration);
		currentContainer.push(declaration);
	}

	@Override
	public void enterLiteralExpression(ICSSParser.LiteralExpressionContext ctx) {
		String value = ctx.getText();

		ASTNode parent = currentContainer.peek();

		ExpressionType expressionType = getExpressionType(value);
		Expression expression = getExpression(expressionType, value);
		parent.addChild(expression);
	}

	@Override
	public void enterVariableExpression(ICSSParser.VariableExpressionContext ctx) {
		String value = ctx.getText();

		ASTNode parent = currentContainer.peek();
		VariableReference reference = new VariableReference(value);
		parent.addChild(reference);
	}

	@Override
	public void enterNum(ICSSParser.NumContext ctx) {
		ASTNode parent = currentContainer.peek();
		String text = ctx.getText();

		if(ctx.variableReference() != null) {
			parent.addChild(new VariableReference(text));
		} else {
			parent.addChild(getExpression(getExpressionType(text), text));
		}
	}

	@Override
	public void enterMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		if(ctx.MUL().isEmpty()) return;

		String value = ctx.getChild(0).getText();
		String value2 = ctx.getChild(2).getText();
		Expression multiply = new MultiplyOperation();

		multiply.addChild(getExpression(getExpressionType(value), value));
		multiply.addChild(getExpression(getExpressionType(value2), value2));

		currentContainer.peek().addChild(multiply);
	}

	@Override
	public void enterAddSubtractOperation(ICSSParser.AddSubtractOperationContext ctx) {
		if(ctx.PLUS().isEmpty() && ctx.MIN().isEmpty()) return;

		ASTNode parent = currentContainer.peek();

		if(!ctx.PLUS().isEmpty()) {
			Expression addOperation = new AddOperation();
			parent.addChild(addOperation);
			currentContainer.push(addOperation);
		} else {
			Expression subtractOperation = new SubtractOperation();
			parent.addChild(subtractOperation);
			currentContainer.push(subtractOperation);
		}
	}

	@Override
	public void exitAddSubtractOperation(ICSSParser.AddSubtractOperationContext ctx) {
		if (currentContainer.peek() instanceof AddOperation || currentContainer.peek() instanceof SubtractOperation) {
			currentContainer.pop();
		}
	}

	@Override
	public void exitMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		if (currentContainer.peek() instanceof MultiplyOperation) {
			currentContainer.pop();
		}
	}

	@Override
	public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
		Stylerule rule = (Stylerule) currentContainer.pop();
		ast.root.addChild(rule);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.pop();
	}

	private Selector getSelector(String text) {
		if(text.startsWith(".")) {
			return new ClassSelector(text);
		} else if(text.startsWith("#")) {
			return new IdSelector(text);
		} else { // TODO: Better check for this tag selector
			return new TagSelector(text);
		}
	}

	private Expression getExpression(ExpressionType type, String literal) {
        return switch (type) {
            case COLOR -> new ColorLiteral(literal);
            case PERCENTAGE -> new PercentageLiteral(literal);
            case PIXEL -> new PixelLiteral(literal);
            case SCALAR -> new ScalarLiteral(literal);
            case BOOL -> new BoolLiteral(literal);
            default -> null;
        };
	}

	private ExpressionType getExpressionType(String text) {
		if (text.startsWith("#")) {
			return ExpressionType.COLOR;
		}
		if (text.endsWith("%")) {
			return ExpressionType.PERCENTAGE;
		}
		if (text.endsWith("px")) {
			return ExpressionType.PIXEL;
		}
		if (isInteger(text)) {
			return ExpressionType.SCALAR;
		}
		if (text.trim().equals("TRUE") || text.trim().equals("FALSE")) {
			return ExpressionType.BOOL;
		}
		return ExpressionType.UNDEFINED;
	}

	private boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
}