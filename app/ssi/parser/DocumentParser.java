package ssi.parser;

import static ssi.parser.ParseState.*;

import java.util.Stack;

public class DocumentParser {

    public static class Builder {
        private char[] expressionBegin = "<!--#".toCharArray();
        private char[] expressionEnd = "-->".toCharArray();
        private char[] includeExpression = "include virtual=\"".toCharArray();
        private char[] ifExpression = "if expr=\"".toCharArray();
        private char[] elseExpression = "else".toCharArray();
        private char[] endIfExpression = "endif".toCharArray();
        private char[] echoExpression = "echo var=\"".toCharArray();
        private int plainBufferCapacity = 1024;

        public Builder setExpressionBegin(String expressionBegin) {
            this.expressionBegin = expressionBegin.toCharArray();;
            return this;
        }

        public Builder setExpressionEnd(String expressionEnd) {
            this.expressionEnd = expressionEnd.toCharArray();;
            return this;
        }

        public Builder setIncludeExpression(String includeExpression) {
            this.includeExpression = includeExpression.toCharArray();;
            return this;
        }

        public Builder setIfExpression(String ifExpression) {
            this.ifExpression = ifExpression.toCharArray();;
            return this;
        }

        public Builder setElseExpression(String elseExpression) {
            this.elseExpression = elseExpression.toCharArray();;
            return this;
        }

        public Builder setEndIfExpression(String endIfExpression) {
            this.endIfExpression = endIfExpression.toCharArray();;
            return this;
        }

        public Builder setEchoExpression(String echoExpression) {
            this.echoExpression = echoExpression.toCharArray();
            return this;
        }

        public Builder setPlainBufferCapacity(int plainBufferCapacity) {
            this.plainBufferCapacity = plainBufferCapacity;
            return this;
        }
    }

    //
    // attributes
    //

    private static class PossibleExpression {
        final ParseState parseState;
        final char[] expression;
        boolean possible;

        public PossibleExpression(ParseState parseState, char[] expression) {
            this.parseState = parseState;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return "PossibleExpression [parseState=" + parseState
                    + ", expression=" + new String(expression)
                    + ", possible=" + possible + "]";
        }

    }

    private final char[] expressionBegin;
    private final char[] expressionEnd;
    private final char[] includeExpression;
    private final char[] ifExpression;
    private final char[] elseExpression;
    private final char[] endIfExpression;
    private final char[] echoExpression;

    // initial state
    private final Document document = new Document();
    private int documentIndex = 0;
    private ParseState currentParseState = PLAIN_TEXT;
    private int expressionToParsedIndex = 0;
    private final ByteArrayBuilder plainBuffer;
    private final ByteArrayBuilder inCommentBuffer = new ByteArrayBuilder(100);
    private final ByteArrayBuilder internExpressionBuffer = new ByteArrayBuilder(100);
    private Stack<Section> stack = new Stack<Section>();
    private Section sectionToPushWhenExpressionEnd;
    private final PossibleExpression[] expressionCandidates = new PossibleExpression[5];

    //
    // constructors
    //

    public DocumentParser() {
        this(new Builder());
        initExpressionCandidates();
    }

    public DocumentParser(Builder builder) {
        this.expressionBegin = builder.expressionBegin;
        this.expressionEnd = builder.expressionEnd;
        this.includeExpression = builder.includeExpression;
        this.ifExpression = builder.ifExpression;
        this.elseExpression = builder.elseExpression;
        this.endIfExpression = builder.endIfExpression;
        this.echoExpression = builder.echoExpression;
        this.plainBuffer = new ByteArrayBuilder(builder.plainBufferCapacity);
        initExpressionCandidates();
    }

    private void initExpressionCandidates() {
        expressionCandidates[0] = new PossibleExpression(INCLUDE, includeExpression);
        expressionCandidates[1] = new PossibleExpression(IF,      ifExpression);
        expressionCandidates[2] = new PossibleExpression(ELSE,    elseExpression);
        expressionCandidates[3] = new PossibleExpression(ENDIF,   endIfExpression);
        expressionCandidates[4] = new PossibleExpression(ECHO,    echoExpression);
    }

    //
    // implementation
    //

    public DocumentParser parse(final String content) {
        return parse(content.getBytes());
    }

    public DocumentParser parse(final byte[] content) {
        return parse(content, content.length);
    }

    public DocumentParser parse(final byte[] content, final int contentLength) {
        for (int i = 0; i < contentLength; i++) {

            final byte c = content[i];
            if (currentParseState == PLAIN_TEXT) {
                if (c == expressionBegin[0]) {
                    boolean isExpressionCandidate = true;
                    if (contentLength > i + 1) {
                        // avoid considering non-comment HTML tag as expression candidates
                        byte c1 = content[i + 1];
                        if (c1 != expressionBegin[1])
                            isExpressionCandidate = false;
                    }
                    if (isExpressionCandidate) {
                        inCommentBuffer.clear();
                        inCommentBuffer.append(c);
                        expressionToParsedIndex = 1;
                        currentParseState = DYNAMIC_COMMENT;
                    } else {
                        plainBuffer.append(c);
                    }
                } else {
                    plainBuffer.append(c);
                }
            } else {
                // in comment
                inCommentBuffer.append(c);

                switch (currentParseState) {
                case DYNAMIC_COMMENT:
                    if (c == expressionBegin[expressionToParsedIndex]) {
                        expressionToParsedIndex += 1;
                        if (expressionToParsedIndex == expressionBegin.length) {
                            currentParseState = ParseState.EXPRESSION;
                            expressionToParsedIndex = 0;
                            for (PossibleExpression possibleExpression : expressionCandidates)
                                possibleExpression.possible = true;
                        }
                    } else {
                        // not a comment
                        currentParseState = PLAIN_TEXT;
                        plainBuffer.append(inCommentBuffer);
                    }
                    break;

                case EXPRESSION:
                    if (expressionToParsedIndex == 0 && c == ' ') {
                        // ignore whitespace
                    } else {
                        // reduce list of possible expression
                        boolean oneExpressionPossible = false;
                        boolean expressionFound = false;
                        for (PossibleExpression possibleExpression : expressionCandidates) {
                            if (possibleExpression.possible) {
                                if (possibleExpression.expression.length <= expressionToParsedIndex
                                        || c != possibleExpression.expression[expressionToParsedIndex]) {
                                    possibleExpression.possible = false;
                                } else {
                                    oneExpressionPossible = true;
                                    if (expressionToParsedIndex == possibleExpression.expression.length - 1) {
                                        // we found the expression
                                        expressionFound = true;
                                        if (plainBuffer.length != 0) {
                                            stack.push(new Section( PLAIN_TEXT, plainBuffer.getByteBuffer() ) );
                                            plainBuffer.clear();
                                        }
                                        expressionToParsedIndex = 0;
                                        final ParseState parseState = possibleExpression.parseState;
                                        if (parseState == ELSE || parseState == ENDIF) {
                                            sectionToPushWhenExpressionEnd = new Section(parseState);
                                            currentParseState = EXPRESSION_END;
                                        } else {
                                            currentParseState = parseState;
                                            internExpressionBuffer.clear();
                                        }
                                    }
                                }
                            }
                        }
                        if (!oneExpressionPossible) {
                            // not an expression
                            currentParseState = PLAIN_TEXT;
                            plainBuffer.append(inCommentBuffer);
                        } else if (!expressionFound) {
                            expressionToParsedIndex += 1;
                        }
                    }
                    break;

                case INCLUDE:
                case IF:
                case ECHO:
                    if (c == '"') {
                        // end of url or expr
                        sectionToPushWhenExpressionEnd = new Section( currentParseState, internExpressionBuffer.getByteBuffer() );
                        currentParseState = EXPRESSION_END;
                        expressionToParsedIndex = 0;
                    } else {
                        internExpressionBuffer.append(c);
                    }
                    break;

                case EXPRESSION_END:
                    if (expressionToParsedIndex == 0 && c == ' ') {
                        // ignore whitespace
                    } else if (c == expressionEnd[expressionToParsedIndex]) {
                        expressionToParsedIndex += 1;
                        if (expressionToParsedIndex == expressionEnd.length) {
                            stack.push(sectionToPushWhenExpressionEnd);
                            currentParseState = PLAIN_TEXT;
                            plainBuffer.clear();
                        }
                    } else {
                        // invalid end comment
                        plainBuffer.append(inCommentBuffer);
                        currentParseState = PLAIN_TEXT;
                    }
                    break;
                }
            }
        }
        while (!stack.isEmpty()) {
            document.sections.add(documentIndex, stack.pop());
        }
        documentIndex = document.sections.size();
        return this;
    }

    public Document finish() {
        if (currentParseState == PLAIN_TEXT) {
            stack.push(new Section(PLAIN_TEXT, plainBuffer.getByteBuffer()));
        } else if (inCommentBuffer.length != 0) {
            plainBuffer.append(inCommentBuffer);
            stack.push(new Section(PLAIN_TEXT, plainBuffer.getByteBuffer()));
        }
        while (!stack.isEmpty()) {
            document.sections.add(documentIndex, stack.pop());
        }
        return document;
    }

}
