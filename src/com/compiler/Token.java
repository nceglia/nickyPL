package com.compiler;


public class Token {

    TokenType kind;
    String value;
    public Token() {
        kind = TokenType.invalidToken;
        value = "";
    }

    public void SetToken(String token) {
        value = token;
        switch (token) {
            case "*":
                kind = TokenType.timesToken;
                break;
            case "/":
                kind = TokenType.divToken;
                break;
            case "+":
                kind = TokenType.plusToken;
                break;
            case "-":
                kind = TokenType.minusToken;
                break;
            case "==":
                kind = TokenType.eqlToken;
                break;
            case "!=":
                kind = TokenType.neqToken;
                break;
            case "<":
                kind = TokenType.lssToken;
                break;
            case ">=":
                kind = TokenType.geqToken;
                break;
            case "<=":
                kind = TokenType.leqToken;
                break;
            case ">":
                kind = TokenType.gtrToken;
                break;
            case ".":
                kind = TokenType.periodToken;
                break;
            case ",":
                kind = TokenType.commaToken;
                break;
            case "[":
                kind = TokenType.openbracketToken;
                break;
            case "]":
                kind = TokenType.closebracketToken;
                break;
            case ")":
                kind = TokenType.closeparenToken;
                break;
            case "<-":
                kind = TokenType.becomesToken;
                break;
            case "then":
                kind = TokenType.thenToken;
                break;
            case "do":
                kind = TokenType.doToken;
                break;
            case "(":
                kind = TokenType.openparenToken;
                break;
            case "number":
                kind = TokenType.number;
                break;
            case "identifier":
                kind = TokenType.ident;
                break;
            case ";":
                kind = TokenType.semiToken;
                break;
            case "}":
                kind = TokenType.endToken;
                break;
            case "od":
                kind = TokenType.odToken;
                break;
            case "fi":
                kind = TokenType.fiToken;
                break;
            case "else":
                kind = TokenType.elseToken;
                break;
            case "let":
                kind = TokenType.letToken;
                break;
            case "call":
                kind = TokenType.callToken;
                break;
            case "if":
                kind = TokenType.ifToken;
                break;
            case "while":
                kind = TokenType.whileToken;
                break;
            case "return":
                kind = TokenType.returnToken;
                break;
            case "var":
                kind = TokenType.varToken;
                break;
            case "array":
                kind = TokenType.arrToken;
                break;
            case "function":
                kind = TokenType.funcToken;
                break;
            case "procedure":
                kind = TokenType.procToken;
                break;
            case "{":
                kind = TokenType.beginToken;
                break;
            case "main":
                kind = TokenType.mainToken;
                break;
            case "end of file":
                kind = TokenType.eofToken;
                break;
            case "//":
                kind = TokenType.commentToken;
                break;
            default:
                kind = TokenType.invalidToken;
                break;
        }
    }

    public void SetTokenType(TokenType kind) {
        this.kind = kind;
    }

    public TokenType GetTokenType() {
        return kind;
    }

    public String GetTokenValue() {
        return value;
    }
    public Integer GetTokenValueAsInt() {
        return Integer.parseInt(value);
    }
}
