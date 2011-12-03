package ssi.parser;

import java.util.ArrayList;
import java.util.List;

public class Document {

    //
    // attributes
    //

    public final List<Section> sections = new ArrayList<Section>();

    //
    // helpers
    //

    public Document add(Section section) {
        sections.add(section);
        return this;
    }

    public Document add(ParseState parseState, String content) {
        sections.add(new Section(parseState, content));
        return this;
    }

    public Document add(ParseState parseState, byte[] content) {
        sections.add(new Section(parseState, content));
        return this;
    }

    public Document add(ParseState parseState) {
        sections.add(new Section(parseState));
        return this;
    }

}
