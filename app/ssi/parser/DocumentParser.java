package ssi.parser;

import static ssi.parser.ParseState.*;

public class DocumentParser {

    public static class Builder {
        private char[] expressionBegin = "<!--#".toCharArray();
        private char[] expressionEnd = "-->".toCharArray();
        private char[] includeExpression = "include virtual=\"".toCharArray();
        private char[] paramExpression = "param name=\"".toCharArray();
        private char[] endParamExpression = "endparam".toCharArray();
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

        public Builder setParamExpression(String paramExpression) {
            this.paramExpression = paramExpression.toCharArray();;
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
        final boolean hasContent;
        final char[] expression;
        boolean possible;

        public PossibleExpression(ParseState parseState, boolean hasContent, char[] expression) {
            this.parseState = parseState;
            this.hasContent = hasContent;
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
    private final char expressionBegin_0;
    private final char[] expressionEnd;
    private final char[] includeExpression;
    private final char[] paramExpression;
    private final char[] endParamExpression;
    private final char[] ifExpression;
    private final char[] elseExpression;
    private final char[] endIfExpression;
    private final char[] echoExpression;

    // initial state
    private final Document document = new Document();
    private ParseState currentParseState = PLAIN_TEXT;
    private int expressionToParsedIndex = 0;
    private final ByteArrayBuilder plainBuffer;
    private final ByteArrayBuilder inCommentBuffer = new ByteArrayBuilder(100);
    private final ByteArrayBuilder internExpressionBuffer = new ByteArrayBuilder(100);
    private Section sectionToPushWhenExpressionEnd;
    private final PossibleExpression[] expressionCandidates = new PossibleExpression[7];

    //
    // constructors
    //

    public DocumentParser() {
        this(new Builder());
        initExpressionCandidates();
    }

    public DocumentParser(Builder builder) {
        this.expressionBegin = builder.expressionBegin;
        this.expressionBegin_0 = this.expressionBegin[0];
        this.expressionEnd = builder.expressionEnd;
        this.includeExpression = builder.includeExpression;
        this.paramExpression = builder.paramExpression;
        this.endParamExpression = builder.endParamExpression;
        this.ifExpression = builder.ifExpression;
        this.elseExpression = builder.elseExpression;
        this.endIfExpression = builder.endIfExpression;
        this.echoExpression = builder.echoExpression;
        this.plainBuffer = new ByteArrayBuilder(builder.plainBufferCapacity);
        initExpressionCandidates();
    }

    private void initExpressionCandidates() {
        expressionCandidates[0] = new PossibleExpression(INCLUDE,   true,  includeExpression);
        expressionCandidates[1] = new PossibleExpression(PARAM,     true,  paramExpression);
        expressionCandidates[2] = new PossibleExpression(END_PARAM, false, endParamExpression);
        expressionCandidates[3] = new PossibleExpression(IF,        true,  ifExpression);
        expressionCandidates[4] = new PossibleExpression(ELSE,      false, elseExpression);
        expressionCandidates[5] = new PossibleExpression(ENDIF,     false, endIfExpression);
        expressionCandidates[6] = new PossibleExpression(ECHO,      true,  echoExpression);
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
                if (c == expressionBegin_0) {
                    inCommentBuffer.clear();
                    inCommentBuffer.append(c);
                    expressionToParsedIndex = 1;
                    currentParseState = DYNAMIC_COMMENT;
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
                                            document.add( PLAIN_TEXT, plainBuffer.getByteBuffer() );
                                            plainBuffer.clear();
                                        }
                                        expressionToParsedIndex = 0;
                                        final ParseState parseState = possibleExpression.parseState;
                                        if (possibleExpression.hasContent) {
                                            currentParseState = parseState;
                                            internExpressionBuffer.clear();
                                        } else {
                                            sectionToPushWhenExpressionEnd = new Section(parseState);
                                            currentParseState = EXPRESSION_END;
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
                case PARAM:
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
                            document.add( sectionToPushWhenExpressionEnd );
                            currentParseState = PLAIN_TEXT;
                            plainBuffer.clear();
                            inCommentBuffer.clear();
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
        return this;
    }


    public Document finish() {
        if (currentParseState == PLAIN_TEXT && plainBuffer.length != 0) {
            document.add( PLAIN_TEXT, plainBuffer.getByteBuffer() );
        } else if (inCommentBuffer.length != 0) {
            plainBuffer.append(inCommentBuffer);
            document.add( PLAIN_TEXT, plainBuffer.getByteBuffer() );
        }
        return document;
    }

}
