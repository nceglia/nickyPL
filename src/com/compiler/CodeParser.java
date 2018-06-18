package com.compiler;

import java.io.IOException;
import java.util.ArrayList;

public class CodeParser {

    private CodeScanner input;
    private Token token;
    private Registers registers;
    private Integer lineNumber;
    private IntermediateRepresentation IR;

    public CodeParser(String sourceCode) throws IOException {
        input = new CodeScanner(sourceCode);
        token = new Token();
        registers = new Registers(100);
        lineNumber = 0;
    }

    public void Next() {
        input.Next();
        token = input.GetToken();
        while (token.GetTokenType() == TokenType.eolToken || token.GetTokenType() == TokenType.commentToken) {
            if (token.GetTokenType() == TokenType.eolToken) {
                this.lineNumber ++;
            }
            input.Next();
        }
    }

    public IntermediateRepresentation Parse() {
        Next();
        IntermediateRepresentation MainIR = Function(Boolean.TRUE);
        return MainIR;
    }


    public IntermediateRepresentation Function(Boolean procedure) {
        IntermediateRepresentation routine = new IntermediateRepresentation();
        BasicBlock block = new BasicBlock();
        Result function = Factor(block);
        TokenType functionComplete;
        switch (function.kind) {
            case mainEntry:
                System.out.println("Parsing Main Function @ " + lineNumber.toString());
                functionComplete = TokenType.periodToken;
                break;
            case variable:
                routine.SetAddress(function.address);
                functionComplete = TokenType.semiToken;
                if (procedure) {
                    System.out.println("Procedure Definition @ " + lineNumber.toString());
                } else {
                    System.out.println("Function Definition @ " + lineNumber.toString());
                }
                ArrayList<Result> arguments = new ArrayList<>();
                if (token.GetTokenType() == TokenType.openparenToken) {
                    Next();
                    while (token.GetTokenType() != TokenType.closeparenToken) {
                        Result argument = Factor(block);
                        arguments.add(argument);
                        if (argument.kind != ResultType.variable) {
                            System.out.println("Invalid Function Arguments @ " + lineNumber.toString());
                            break;
                        }
                        if (token.GetTokenType() == TokenType.closeparenToken) {
                            break;
                        }
                        if (token.GetTokenType() != TokenType.commaToken) {
                            System.out.println("Expected , @ " + lineNumber.toString());
                            break;
                        }
                        Next();
                    }
                }
                Next();
                if (token.GetTokenType() != TokenType.semiToken) {
                    System.out.println("Expected ; @ " + lineNumber.toString());
                }
                Next();
                routine.AddArguments(arguments);
                break;
            default:
                System.out.println("Invalid Function Declaration @ " + lineNumber.toString());
                return routine;
        }
        while (token.GetTokenType() == TokenType.varToken || token.GetTokenType() == TokenType.arrToken) {
            Next();
            ArrayList<Result> scope = Declaration();
            routine.AddVariables(scope);
        }
        while (token.GetTokenType() == TokenType.funcToken) {
            Next();
            IntermediateRepresentation subroutine = Function(Boolean.FALSE);
            Integer address = subroutine.GetAddress();
            routine.AddSubroutine(address, subroutine);
        }
        while (token.GetTokenType() == TokenType.procToken) {
            Next();
            IntermediateRepresentation subroutine = Function(Boolean.TRUE);
            Integer address = subroutine.GetAddress();
            routine.AddSubroutine(address, subroutine);
        }
        BasicBlocks CFG = Body();
        routine.SetCFG(CFG);
        if (token.GetTokenType() != functionComplete) {
            System.out.println("Expected Signal " + functionComplete + " @ " + lineNumber.toString());
        }
        Next();
        return routine;
    }


    public ArrayList<Result> Declaration() {
        BasicBlock block = new BasicBlock();
        ArrayList<Result> scope = new ArrayList<Result>();
        while (token.GetTokenType() != TokenType.semiToken) {
            Result variable = Factor(block);
            if (variable.kind == ResultType.variable) {
                if (variable.dimensions.size() > 0) {
                    System.out.println("Declared Array Variable With Address " + variable.address + " and dimensions " + variable.dimensions.size() + " @ " + lineNumber.toString());
                } else {
                    System.out.println("Declared Variable With Address " + variable.address + " @ " + lineNumber.toString());
                }
                scope.add(variable);
            } else {
                System.out.println("Invalid Declaration Type @ " + this.lineNumber.toString() + " : " + variable.message);
            }
            switch (token.GetTokenType()) {
                case commaToken:
                    Next();
                    break;
                case semiToken:
                    break;
                default:
                    System.out.println(token.GetTokenType());
                    System.out.println("Invalid Declaration Syntax @ " + lineNumber.toString());
                    return scope;
            }
        }
        Next();
        return scope;
    }

    public BasicBlocks Body() {
        BasicBlocks graph = new BasicBlocks();
        if (token.GetTokenType() == TokenType.beginToken) {
            while (token.GetTokenType() != TokenType.endToken) {
                Next();
                Statement(graph);
            }
        }
        Next();
        return graph;
    }


    public void Statement(BasicBlocks graph) {
        switch (token.GetTokenType()) {
            case letToken:
                System.out.println("Variable Assignment @ " + lineNumber.toString());
                Next();
                BasicBlock block = Assignment();
                graph.AddBlock(block);
                break;
            case whileToken:
                System.out.println("While Statement @ " + lineNumber.toString());
                Next();
                BasicBlock whileCondition = Condition();
                graph.AddBlock(whileCondition);
                While(graph);
                break;
            case ifToken:
                System.out.println("If Statement @ " + lineNumber.toString());
                Next();
                BasicBlock ifCondition = Condition();
                graph.AddBlock(ifCondition);
                If(graph);
                break;
            case callToken:
                System.out.println("Function Call @ " + lineNumber.toString());
                Next();
                BasicBlock callBlock = Call();
                graph.AddBlock(callBlock);
                break;
            case returnToken:
                Next();
                Return(graph);
                break;
            default:
                break;
        }
    }

    public void While(BasicBlocks graph) {
        BasicBlocks leftGraph = new BasicBlocks();
        BasicBlocks rightGraph = null;
        if (token.GetTokenType() == TokenType.doToken) {
            while (token.GetTokenType() != TokenType.odToken) {
                Next();
                Statement(leftGraph);
            }
        }
        BasicBlock jumpBlock = new BasicBlock();
        Instruction instruction = new Instruction("jmp");
        jumpBlock.AddInstruction(instruction);
        leftGraph.AddBlock(jumpBlock);
        graph.AddBranch(leftGraph,rightGraph);
    }

    public void If(BasicBlocks graph) {
        BasicBlocks leftGraph = new BasicBlocks();
        BasicBlocks rightGraph = new BasicBlocks();

        if (token.GetTokenType() == TokenType.thenToken) {
            while (token.GetTokenType() != TokenType.fiToken && token.GetTokenType() != TokenType.elseToken) {
                Next();
                Statement(leftGraph);
            }
        }
        System.out.println(token.GetTokenType());
        if (token.GetTokenType() == TokenType.elseToken) {
            while (token.GetTokenType() != TokenType.fiToken) {
                Next();
                Statement(rightGraph);
            }
        }
        graph.AddBranch(leftGraph,rightGraph);
    }

    public BasicBlock Call() {
        BasicBlock block = new BasicBlock();
        Result function = Factor(block);
        ArrayList<Result> args = new ArrayList<>();
        if (token.GetTokenType() == TokenType.openparenToken) {
            while (token.GetTokenType() != TokenType.closeparenToken) {
                Next();
                Result arg = Factor(block);
                args.add(arg);
                if (token.GetTokenType() != TokenType.commaToken && token.GetTokenType() != TokenType.closeparenToken) {
                    System.out.println("Invalid Arguments @ " + lineNumber.toString());
                }
            }
        }
//        ArrayList<Instruction> instructions = block.GetInstructions();
        BasicBlock callBlock = new BasicBlock(function, args);
        return callBlock;
    }

    public void Return(BasicBlocks graph) {
        BasicBlock block = new BasicBlock();
        Result value = Expression(block);
        graph.AddReturn(value);
    }


    public BasicBlock Assignment() {
        BasicBlock block = new BasicBlock();
        Result x = Factor(block);
        if (x.kind == ResultType.variable && token.GetTokenType() == TokenType.becomesToken) {
            TokenType op = token.GetTokenType();
            Next();
            Result y = Expression(block);
            if (y.kind == ResultType.funcEntry) {
                block = Call();
                Instruction instruction = new Instruction("ret",x);
                block.AddInstruction(instruction);
            } else {
                ArrayList<Instruction> instructions = Combine(op,x,y);
                block.AddInstructions(instructions);
            }
        }
        return block;
    }

    public BasicBlock Condition() {
        BasicBlock block = new BasicBlock();
        Result x = Expression(block);
        TokenType relOp = token.GetTokenType();
        while (relOp == TokenType.eqlToken ||  relOp == TokenType.neqToken || relOp == TokenType.leqToken || relOp == TokenType.geqToken || relOp == TokenType.gtrToken || relOp == TokenType.lssToken) {
            Next();
            Result y = Expression(block);
            ArrayList<Instruction> instructions = Combine(relOp,x,y);
            block.AddInstructions(instructions);
            relOp = token.GetTokenType();
        }
        return block;
    }

    public Result Expression(BasicBlock block) {
        Result x = Term(block);
        while (token.GetTokenType() == TokenType.plusToken || token.GetTokenType() == TokenType.minusToken) {
            TokenType op = token.GetTokenType();
            Next();
            Result y = Term(block);
            ArrayList<Instruction> instructions = Combine(op,x,y);
            block.AddInstructions(instructions);
        }
        return x;
    }


    public Result Term(BasicBlock block) {
        Result x = Factor(block);
        while (token.GetTokenType() == TokenType.timesToken || token.GetTokenType() == TokenType.divToken) {
            TokenType op = token.GetTokenType();
            Next();
            Result y = Factor(block);
            ArrayList<Instruction> instructions = Combine(op,x,y);
            block.AddInstructions(instructions);
        }
        return x;
    }


    public Result Factor(BasicBlock block) {
        Result result = new Result();
        switch(token.GetTokenType()) {
            case ident:
                result.kind = ResultType.variable;
                result.address = input.GetIDFromString(token.GetTokenValue());
                result.name = token.GetTokenValue();
                Next();
                while (token.GetTokenType() == TokenType.openbracketToken) {
                    Next();
                    Result index = Expression(block);
                    if (index.kind  == ResultType.constant) {
                        result.offset.add(index.value.toString());
                    } else if (index.kind == ResultType.variable) {
                        result.offset.add(index.name);
                    } else {
                        System.out.println("Array indices must be constant or variable");
                        result.kind = ResultType.error;
                    }
                    Next();
                }
                break;
            case number:
                result.kind = ResultType.constant;
                result.value = token.GetTokenValueAsInt();
                Next();
                break;
            case openparenToken:
                Next();
                result = Expression(block);
                if (token.GetTokenType() == TokenType.closeparenToken) {
                    Next();
                } else {
                    result.kind = ResultType.error;
                }
                break;
            case openbracketToken:
                Next();
                result.kind = ResultType.variable;
                while (token.GetTokenType() != TokenType.ident) {
                    while (token.GetTokenType() == TokenType.number) {
                        Result size = Expression(block);
                        if (size.kind == ResultType.constant) {
                            if (token.GetTokenType() == TokenType.closebracketToken) {
                                result.dimensions.add(size.value);
                                Next();
                                if (token.GetTokenType() == TokenType.openbracketToken) {
                                    Next();
                                } else {
                                    break;
                                }
                            } else {
                                result.kind = ResultType.error;
                                break;
                            }
                        } else {
                            result.kind = ResultType.error;
                            break;
                        }
                    }
                }
                result.address = input.GetIDFromString(token.GetTokenValue());
                result.name = token.GetTokenValue();
                Next();
                break;
            case mainToken:
                result.kind = ResultType.mainEntry;
                Next();
                break;
            case callToken:
                result.kind = ResultType.funcEntry;
                Next();
            case eofToken:
                break;
            default:
                result.kind = ResultType.error;
                result.message = "Undefined Error: " + token.GetTokenValue();
                break;
        }
        return result;
    }


    public Integer AllocateRegister() {
        return registers.GetNextAvailable();
    }

    public void DeAllocateRegister(Result result) {
        registers.Release(result.register);
    }

    public ArrayList<Instruction> Load(Result result) {
        ArrayList<Instruction> instructions = new ArrayList<>();
        if (result.kind == ResultType.variable) {
            //result.register = AllocateRegister();
            //Instruction node = new Instruction("LOAD", result);
            //instructions.add(node);
            //result.kind = ResultType.register;
        }
//        } else if (result.kind == ResultType.constant) {
////            if (result.value == 0) {
////                result.register = 0;
////            } else {
//            result.register = AllocateRegister();
//            Instruction node = new Instruction("ADD", result);
//            instructions.add(node);
////            }
//            result.kind = ResultType.register;
//        }
        return instructions;
    }

    public ArrayList<Instruction> Combine(TokenType op, Result x, Result y) {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        if (x.kind == ResultType.constant && y.kind == ResultType.constant) {
            switch(op) {
                case plusToken:
                    x.value += y.value;
                    break;
                case minusToken:
                    x.value -= y.value;
                    break;
                case timesToken:
                    x.value *= y.value;
                    break;
                case divToken:
                    x.value /= y.value;
                    break;
            }
        } else {
//            ArrayList<Instruction> load_instructions = Load(x);
//            for (Instruction instruction : load_instructions) {
//                instructions.add(instruction);
//            }
//            if (x.register == 0) {
//                x.register = AllocateRegister();
//                Instruction node = new Instruction(op, x);
//                instructions.add(node);
//            }
            if (x.kind == ResultType.variable && y.kind == ResultType.constant) {
                Instruction node = new Instruction(op,x,y);
                instructions.add(node);
                ArrayList<Instruction> load_instructions = Load(x);
                for (Instruction instruction : load_instructions) {
                    instructions.add(instruction);
                }
            } else if (x.kind == ResultType.constant && y.kind == ResultType.variable) {
                Instruction node = new Instruction(op,y,x);
                instructions.add(node);
                ArrayList<Instruction> load_instructions = Load(y);
                for (Instruction instruction : load_instructions) {
                    instructions.add(instruction);
                }
                x.kind = ResultType.variable;
                x.value = y.value;
                x.address = y.address;
                x.register = y.register;
                x.name = y.name;
            } else if (x.kind == ResultType.variable && y.kind == ResultType.variable) {
                if (x.address != y.address) {
                    Instruction node = new Instruction(op, x, y);
                    instructions.add(node);
                }
            } else if (x.kind == ResultType.variable && y.kind == ResultType.funcEntry) {
                Instruction node = new Instruction(TokenType.becomesToken,x);
                instructions.add(node);
            } else {
                System.out.println("Unknown");
            }
//            if (y.kind == ResultType.constant) {
//                Instruction node = new Instruction(op, x, y);
//                instructions.add(node);
//            } else {
//                Load(y);
//                Instruction node = new Instruction(op, x, y);
//                instructions.add(node);
//                DeAllocateRegister(y);
//            }
//            if (x.kind == ResultType.constant) {
//                Result tmp = x;
//                x = y;
//                y = tmp;
//                //Instruction node = new Instruction(op, x ,y);
//            }
//            Instruction node = new Instruction(op, x ,y);
//            instructions.add(node);
        }
        return instructions;
    }


}

