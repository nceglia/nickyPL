package com.compiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeReader {
    private int current;
    private Symbol symbol;

    private char[] characters;

    public CodeReader(String sourceCode) throws IOException {
        Path source = Paths.get(sourceCode);
        String code = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        characters = code.toCharArray();
        current = 0;
        symbol =  new Symbol(SymbolType.undefined);
    }

    public void Next() {
        if (current < characters.length) {
            symbol = new Symbol(characters[current]);
            current = current + 1;
        } else {
            symbol = new Symbol(SymbolType.eof);
        }
    }

    public Symbol GetSymbol() {
        return symbol;
    }
}

