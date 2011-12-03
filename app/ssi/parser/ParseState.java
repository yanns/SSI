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
     * <!--#include virtual="/intern-uri" -->
     */
    INCLUDE,

    /**
     * <!--#param name="" -->
     */
    PARAM,

    /**
     * <!--#endparam -->
     */
    END_PARAM,

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
