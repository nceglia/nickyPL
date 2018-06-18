package com.compiler;

public class Symbol {

    private SymbolType kind;
    private String specials = "><+-*///=!";
    private String specialSingles = ",.{}()[];:";
    private String value = "";

    public Symbol(char symbol) {
        value = Character.toString(symbol);
        if (value.matches("\\d+")) {
            kind = SymbolType.digit;
        } else if (value.matches("[a-zA-Z]")) {
            kind = SymbolType.letter;
        } else if (specials.contains(value)) {
            kind = SymbolType.special;
        } else if (specialSingles.contains(value)) {
            kind = SymbolType.single;
        } else if (value.matches("\\s")) {
            if (symbol == '\n') {
                kind = SymbolType.eol;
            } else {
                kind = SymbolType.whitespace;
            }
        } else {
            kind = SymbolType.undefined;
        }
    }

    public Symbol(SymbolType given) {
        kind = given;
    }

    public SymbolType getSymbolType() {
        return kind;
    }

    public String getSymbolValue() {
        return value;
    }
}
