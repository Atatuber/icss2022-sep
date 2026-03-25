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
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        currentContainer.push(ast.root);
    }

    @Override
    public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment assignment = new VariableAssignment();
        assignment.addChild(new VariableReference(ctx.CAPITAL_IDENT().getText()));

        if (ctx.literal() != null) {
            String literal = ctx.literal().getText();
            assignment.addChild(createLiteralExpression(literal));
        } else if (ctx.addSubtractOperation() != null) {
            assignment.addChild(buildAddSubtractExpression(ctx.addSubtractOperation()));
        }

        currentContainer.push(assignment);
    }

    @Override
    public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
        currentContainer.push(new Stylerule());
    }

    @Override
    public void enterSelector(ICSSParser.SelectorContext ctx) {
        currentContainer.peek().addChild(getSelector(ctx.getText()));
    }

    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        ASTNode parent = currentContainer.peek();
        // VariableAssignment
        if (ctx.propertyName() != null) {
            String rawProperty = ctx.propertyName().getText();
            Declaration declaration = new Declaration(rawProperty);
            parent.addChild(declaration);
            currentContainer.push(declaration);
        }
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
    public void exitLiteralExpression(ICSSParser.LiteralExpressionContext ctx) {
        currentContainer.peek().addChild(createLiteralExpression(ctx.getText()));
    }

    @Override
    public void exitVariableExpression(ICSSParser.VariableExpressionContext ctx) {
        currentContainer.peek().addChild(new VariableReference(ctx.getText()));
    }

    @Override
    public void exitAddSubtractOperation(ICSSParser.AddSubtractOperationContext ctx) {
        currentContainer.peek().addChild(buildAddSubtractExpression(ctx));
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
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        if(ctx.propertyName() != null) {
            currentContainer.pop();
        }
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment assignment = (VariableAssignment) currentContainer.pop();
        currentContainer.peek().addChild(assignment);
    }

    @Override
    public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
        Stylerule rule = (Stylerule) currentContainer.pop();
        ast.root.addChild(rule);
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        currentContainer.pop();
    }

    // ---- HELPER FUNCTIONS ----

    private Expression buildAddSubtractExpression(ICSSParser.AddSubtractOperationContext ctx) {
        Expression expression = buildMultiplyExpression(ctx.multiplyOperation(0));

        for (int i = 1; i < ctx.multiplyOperation().size(); i++) {
            String op = ctx.getChild(2 * i - 1).getText();

            Expression rhs = buildMultiplyExpression(ctx.multiplyOperation(i));

            Operation operation = buildBinaryExpression(op);
            operation.lhs = expression;
            operation.rhs = rhs;

            expression = operation;
        }

        return expression;
    }

    private Expression buildMultiplyExpression(ICSSParser.MultiplyOperationContext ctx) {
        if (ctx.MUL().isEmpty()) {
            return makeOperand(ctx.getText());
        }

        Expression left = makeOperand(ctx.operand(0).getText());

        for (int i = 1; i < ctx.operand().size(); i++) {
            Expression right = makeOperand(ctx.operand(i).getText());
            MultiplyOperation next = new MultiplyOperation();

            next.lhs = left;
            next.rhs = right;

            left = next;
        }

        return left;
    }

    private Expression makeOperand(String text) {
        Expression expression = getExpression(getExpressionType(text), text);
        if (expression == null) {
            return new VariableReference(text);
        }

        return expression;
    }

    public Operation buildBinaryExpression(String op) {
        return switch (op) {
            case "+" -> new AddOperation();
            case "-" -> new SubtractOperation();
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }

    private Selector getSelector(String text) {
        if (text.startsWith(".")) {
            return new ClassSelector(text);
        } else if (text.startsWith("#")) {
            return new IdSelector(text);
        } else { // TODO: Better check for this tag selector
            return new TagSelector(text);
        }
    }

    private Expression createLiteralExpression(String literal) {
        return getExpression(getExpressionType(literal), literal);
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
        } catch (NumberFormatException e) {
            return false;
        }
    }
}