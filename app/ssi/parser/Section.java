package ssi.parser;


public class Section {

    public final ParseState parseState;
    public byte[] content;

    public Section(ParseState parseState) {
        this.parseState = parseState;
    }

    public Section(ParseState parseState, byte[] content) {
        this.parseState = parseState;
        this.content = content;
    }

    public Section(ParseState parseState, String content) {
        this.parseState = parseState;
        this.content = content.getBytes();
    }

    public String getContentAsString() {
        return new String(content);
    }

    @Override
    public String toString() {
        return "Section [parseState=" + parseState + (content != null? ", content=" + new String(content) : "")
                + "]";
    }

}
