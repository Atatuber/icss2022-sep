package nl.han.ica.icss.parser;



import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.types.ExpressionType;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.EmptyStackException;

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
		super.exitStylesheet(ctx);
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
		Stylerule rule = (Stylerule) currentContainer.peek();

		// Add key-value declaration to AST
		String[] rawDeclaration = ctx.getChild(0).getText().split("[:;]");

		// Make declaration properties for declaration
		String rawProperty =  rawDeclaration[0];
		String value = rawDeclaration[1];
		ExpressionType expressionType = getExpressionType(value);
		Expression expression = getExpression(expressionType, value);

		// Add declaration to AST
		Declaration declaration = new Declaration(rawProperty);
		declaration.addChild(expression);

		// Add the declaration to the rule
		rule.addChild(declaration);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule rule = (Stylerule) currentContainer.pop();
		ast.root.addChild(rule);
	}

	private Selector getSelector(String text) {
		if(text.startsWith(".")) {
			return new ClassSelector(text);
		} else if(text.startsWith("#")) {
			return new IdSelector(text);
		} else { // TODO: Update this so it correctly checks HTML tags (example.. a, html)
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
		return ExpressionType.UNDEFINED;
	}
}