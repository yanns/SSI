package ssi.parser;


public class Section {

    public final ParseState parseState;
    public String content;

    public Section(ParseState parseState) {
        this.parseState = parseState;
    }

    public Section(ParseState parseState, String content) {
        this.parseState = parseState;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Section [parseState=" + parseState + ", content=" + content
                + "]";
    }

}
