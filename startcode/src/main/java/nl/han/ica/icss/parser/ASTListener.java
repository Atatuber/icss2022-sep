package nl.han.ica.icss.parser;



import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
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
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
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
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule rule = new Stylerule();

		// Push the current node rule to the stack
		currentContainer.push(rule);

		// Add selector to AST
		Selector selector = getSelector(ctx.getChild(0).getText());
		rule.addChild(selector);
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
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
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

	// TODO: Add all use-cases
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

	// TODO: Add all use-cases
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