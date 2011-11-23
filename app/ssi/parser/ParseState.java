package ssi.parser;

public enum ParseState {
    PLAIN_TEXT,

    /**
     * <!--#
     */
    DYNAMIC_COMMENT,

    /**
     * <!--#expression -->
     */
    EXPRESSION,

    WHICH_EXPRESSION,

    EXPRESSION_END,

    /**
     * <!--#include virtual="test.inc" -->
     */
    INCLUDE,

    /**
     * <!--#if expr="" -->
     */
    IF,

    /**
     * <!--#else -->
     */
    ELSE,

    /**
     * <!--#endif -->
     */
    ENDIF,

    /**
     * <!--#echo var="" -->
     */
    ECHO,
}
