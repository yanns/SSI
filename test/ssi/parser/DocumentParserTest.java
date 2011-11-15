package ssi.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DocumentParserTest {

    @Test
    public void testPlainSection() {
        Document result = new DocumentParser().parse("hello world <!-- comment -->").finish();
        assertNotNull(result.sections);
        assertEquals(1, result.sections.size());

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(0).parseState);
        assertEquals("hello world <!-- comment -->", result.sections.get(0).content);
    }

    @Test
    public void testIncludeExpression() {
        Document result = new DocumentParser().parse("hello world<!--#include virtual=\"my-url\"-->end").finish();
        includeExpressionTests(result);

        result = new DocumentParser().parse("hello world<!--# include virtual=\"my-url\" ").parse("-->end").finish();
        includeExpressionTests(result);


        DocumentParser documentParser = new DocumentParser();
        documentParser.parse("hello world<!--#incl").parse("ude virtual=\"my-url\"-->end");
        includeExpressionTests(documentParser.finish());

        documentParser = new DocumentParser();
        documentParser.parse("hel").parse("lo world<!--#incl").parse("ude virtual=\"my-url\"-->end");
        includeExpressionTests(documentParser.finish());

        documentParser = new DocumentParser();
        documentParser.parse("hel").parse("lo world<").parse("!--#incl").parse("ude virtual=\"my-url\"").parse("-->end");
        includeExpressionTests(documentParser.finish());
    }

    @Test
    public void testWithOtherExpressionDelimiter() {
        DocumentParser documentParser = new DocumentParser(new DocumentParser.Builder().setExpressionBegin("<@").setExpressionEnd(">"));
        includeExpressionTests(documentParser.parse("hello world<@include virtual=\"my-url\">end").finish());
    }

    private void includeExpressionTests(Document result) {
        assertNotNull(result.sections);
        assertEquals(3, result.sections.size());
        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(0).parseState);
        assertEquals("hello world", result.sections.get(0).content);

        assertEquals(ParseState.INCLUDE, result.sections.get(1).parseState);
        assertEquals("my-url", result.sections.get(1).content);

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(2).parseState);
        assertEquals("end", result.sections.get(2).content);
    }

    @Test
    public void testExpressionErrors() {
        Document result = new DocumentParser().parse("hello world<!--#include virtual=\"my-url-->end").finish();
        assertNotNull(result.sections);
        assertEquals(2, result.sections.size());

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(0).parseState);
        assertEquals("hello world", result.sections.get(0).content);

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(1).parseState);
        assertEquals("<!--#include virtual=\"my-url-->end", result.sections.get(1).content);
    }

    @Test
    public void testIfExpression() {
        Document result = new DocumentParser().parse("hello world<!--#if expr=\"ze-expression\"-->end").finish();
        assertEquals(3, result.sections.size());

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(0).parseState);
        assertEquals("hello world", result.sections.get(0).content);

        assertEquals(ParseState.IF, result.sections.get(1).parseState);
        assertEquals("ze-expression", result.sections.get(1).content);

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(2).parseState);
        assertEquals("end", result.sections.get(2).content);
    }

    @Test
    public void testChainingExpression() {
        Document result = new DocumentParser().parse("hello world<!--#if expr=\"ze-expression\"--><!--#include virtual=\"my-url\"--><!--#endif-->end").finish();
        assertEquals(5, result.sections.size());

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(0).parseState);
        assertEquals("hello world", result.sections.get(0).content);

        assertEquals(ParseState.IF, result.sections.get(1).parseState);
        assertEquals("ze-expression", result.sections.get(1).content);

        assertEquals(ParseState.INCLUDE, result.sections.get(2).parseState);
        assertEquals("my-url", result.sections.get(2).content);

        assertEquals(ParseState.ENDIF, result.sections.get(3).parseState);

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(4).parseState);
        assertEquals("end", result.sections.get(4).content);
    }

}
