package com.compiler;

public enum TokenType {
    errorToken(0),
    timesToken(1),
    divToken(2),
    plusToken(11),
    minusToken(12),
    eqlToken(20),
    neqToken(21),
    lssToken(22),
    geqToken(23),
    leqToken(24),
    gtrToken(25),
    periodToken(30),
    commaToken(31),
    openbracketToken(32),
    closebracketToken(34),
    closeparenToken(35),
    becomesToken(40),
    thenToken(41),
    doToken(42),
    openparenToken(50),
    number(60),
    ident(61),
    semiToken(70),
    endToken(80),
    odToken(81),
    fiToken(82),
    elseToken(90),
    letToken(100),
    callToken(101),
    ifToken(102),
    whileToken(103),
    returnToken(104),
    varToken(110),
    arrToken(111),
    funcToken(112),
    procToken(113),
    beginToken(150),
    mainToken(200),
    eofToken(255),
    invalidToken(300),
    commentToken(301),
    eolToken(302);
    private Integer value;

    TokenType(Integer value) {
        this.value = value;
    }

    public Integer value() {
        return value;
    }

}
