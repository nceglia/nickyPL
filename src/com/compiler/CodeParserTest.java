package com.compiler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.HashMap;

public class CodeParserTest {

    CodeParser parser;

    @BeforeEach
    void setUp() throws IOException {
        parser = new CodeParser("/Users/nicky/development/Compiler/tests/custom001.txt");
    }


//    @Test
//    public void testNext() {
//        parser.Next();
//        while(parser.GetTokenType() != TokenType.eofToken) {
//            System.out.println(parser.GetTokenType());
//            parser.Next();
//        }
//        System.out.println(parser.GetTokenType());
//        System.out.println("Complete\n\n");
//    }
//
//    @Test
//    public void testTable() {
//        parser.Next();
//        while(parser.GetTokenType() != TokenType.eofToken) {
//            parser.Next();
//        }
//        Integer id = parser.GetIDFromString("a");
//        System.out.println(id);
//        String identifier = parser.GetStringFromID(2);
//        System.out.println(identifier);
//        System.out.println("Complete\n\n");
//    }
//
//    @Test
//    public void testExpression() {
//        parser.Next();
//        Result expression = parser.Expression();
//        System.out.println(expression.kind);
//        for (Instruction i: parser.GetInstructions()) System.out.println(i.Emit());
//    }

    @Test
    public void testIR() throws IOException, InterruptedException {
        System.out.println("****************** PARSING CODE *********************");
        IntermediateRepresentation IR = parser.Parse();
        System.out.println("*************** CODE PARSING COMPLETE *****************");
        Graph cfg = IR.GetCFG();
        cfg.Draw("CFG.dot","CFG.png");
        cfg.DrawFrontier("DOM.dot","DOM.png");
        System.out.println("");
        System.out.println("************** OPTIMIZATION LOG **********************");
        cfg.ApplyCSE();
        cfg.Draw("CFG-CSE.dot","CFG-CSE.png");
        cfg.RenameVariables();
        cfg.Draw("CFG-SSA.dot","CFG-SSA.png");
        cfg.ApplyConstantPropagation();
        cfg.PlacePhi();
        cfg.Draw("CFG-SSA-Phi.dot","CFG-SSA-Phi.png");
        System.out.println("************** Allocating 8 Registers ***************");
        RegisterAllocator allocator = new RegisterAllocator(cfg);
        HashMap<String,Integer> registers = allocator.GetRegisters();
        cfg.WriteAssembly(registers);
        cfg.Draw("Final.dot","Final.png");
        System.out.println("************** ");

    }

//    @Test
//    public void testTerm() {
//
//    }
//
//    @Test
//    public void testExpression() {
//
//    }

    @AfterEach
    void tearDown() {
    }
}