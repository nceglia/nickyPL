package com.compiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class CodeScanner {

    private CodeReader input;
    private Token token;
    private Integer id;
    private HashMap<String,Integer> stringToID;
    private HashMap<Integer,String> idToString;


    public CodeScanner(String sourceCode) throws IOException {
        input = new CodeReader(sourceCode);
        token = new Token();
        stringToID = new HashMap<String,Integer>();
        idToString = new HashMap<Integer,String>();
        id = 0;
        input.Next();
    }

    public void Next() {
        String symbolStream = "";

        Symbol symbol = input.GetSymbol();
        SymbolType thisType = symbol.getSymbolType();
        SymbolType nextType = symbol.getSymbolType();
        while (nextType == thisType) {
            if (thisType == SymbolType.eof) {
                break;
            }
            symbolStream += symbol.getSymbolValue().toString();
            input.Next();
            symbol = input.GetSymbol();
            nextType = symbol.getSymbolType();
            if (thisType == SymbolType.letter && nextType == SymbolType.digit) {
                nextType = SymbolType.letter;
            }
            if (nextType == SymbolType.single) {
                break;
            }
        }
        token.SetToken(symbolStream);
        if (token.GetTokenType() == TokenType.commentToken) {
            while (nextType != SymbolType.eol) {
                if (thisType == SymbolType.eof) {
                    break;
                }
                symbolStream += symbol.getSymbolValue();
                input.Next();
                symbol = input.GetSymbol();
                nextType = symbol.getSymbolType();
            }
            token.SetToken(symbolStream);
            token.SetTokenType(TokenType.commentToken);
        }
        if (token.GetTokenType() == TokenType.invalidToken) {
            switch (thisType) {
                case digit:
                    token.SetTokenType(TokenType.number);
                    break;
                case letter:
                    token.SetTokenType(TokenType.ident);
                    break;
                case eof:
                    token.SetTokenType(TokenType.eofToken);
                    break;
                case eol:
                    token.SetTokenType(TokenType.eolToken);
                    break;
                case whitespace:
                    Next();
                    break;
                default:
                    break;
            }
        }
        if (token.GetTokenType() == TokenType.ident) {
            if (!stringToID.containsKey( token.GetTokenValue())) {
                stringToID.put(token.GetTokenValue(),id);
                idToString.put(id,token.GetTokenValue());
                id ++;
            }
        }
    }

    public String GetStringFromID(Integer id) {
        return idToString.get(id);
    }

    public Integer GetIDFromString(String ident) {
        return stringToID.get(ident);
    }


    public Token GetToken() {
        return token;
    }
}
