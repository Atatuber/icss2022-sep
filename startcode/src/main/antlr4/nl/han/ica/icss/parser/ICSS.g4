grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
stylesheet: (variableAssignment | styleRule)+ EOF;

// Variables
variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR literal SEMICOLON;
variableReference: CAPITAL_IDENT;
propertyName: LOWER_IDENT;

// Operations

operation: addOperation | subtractOperation;

addOperation: multiplyOperation (PLUS multiplyOperation)*;
subtractOperation: multiplyOperation (MIN multiplyOperation)*;
multiplyOperation: lhs (MUL rhs)*;

lhs: variableReference | SCALAR | PIXELSIZE | PERCENTAGE;
rhs: variableReference | SCALAR | PIXELSIZE | PERCENTAGE;

// If Statements

ifClause: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE body CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE body CLOSE_BRACE;

body: (declaration | ifClause)+;

// Styling

styleRule : selector OPEN_BRACE (ifClause | declaration)+ CLOSE_BRACE;

declaration : propertyName COLON expression SEMICOLON;

expression: variableReference #VariableExpression | operation #OperationExpression | literal #LiteralExpression ;

literal: COLOR | PIXELSIZE | PERCENTAGE | SCALAR | TRUE | FALSE;

selector: ID_IDENT | CLASS_IDENT | LOWER_IDENT;
