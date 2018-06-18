package com.compiler;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;



public class IntermediateRepresentation {

    ArrayList<Result> scope;
    ArrayList<Result> arguments;
    Integer address;
    HashMap<Integer,IntermediateRepresentation> subroutines;
    BasicBlocks blocks;

    public IntermediateRepresentation() {
        scope = new ArrayList<Result>();
        arguments = new ArrayList<Result>();
        subroutines = new HashMap<Integer,IntermediateRepresentation>();
    }

    public void SetAddress(Integer address) {
        this.address = address;
    }

    public Integer GetAddress() {
        return this.address;
    }

    public void AddSubroutine(Integer address, IntermediateRepresentation subroutine) {
        this.subroutines.put(address,subroutine);
    }

    public void SetCFG(BasicBlocks graph) {
        this.blocks = graph;
    }

    public void AddVariables(ArrayList<Result> variables) {
        for (Result variable: variables) {
            scope.add(variable);
        }
    }


    public void AddArguments(ArrayList<Result> arguments) {
        for (Result argument: arguments) {
            this.arguments.add(argument);
        }
    }


    public String CFGGraphBuilder(BasicBlocks graph, String previous, Graph CFG, boolean left, boolean right) {
        String leastRecentLink = previous;
        Result lastReturn = null;
        for (BasicBlock block : graph.Blocks()) {
            if (block.function != null  && block.function.name != null) {
                if (block.function.name.equals("OutputNum")) {
                    System.out.println(block.arguments.size());
                    Instruction print = new Instruction("write", block.arguments.get(0));
                    block.AddInstruction(print);
                    previous = CFG.AddBlock(block, previous, left, right);
                } else if (block.function.name.equals("OtherBuiltIn")) {
                    //Put Other Built in functions here
                } else {
                    if (block.function.address != null) {
                        IntermediateRepresentation subIR = this.subroutines.get(block.function.address);
                        if (subIR != null) {
                            BasicBlock copyBlock = new BasicBlock();
                            for (int arg = 0; arg < block.arguments.size(); arg ++) {
                                Instruction instruction = new Instruction(TokenType.becomesToken, block.arguments.get(arg), subIR.arguments.get(arg));
                                copyBlock.AddInstruction(instruction);
                            }
                            lastReturn = subIR.blocks.GetReturn();
                            previous = CFG.AddBlock(copyBlock, previous, left, right);
                            previous = CFGGraphBuilder(subIR.blocks, previous, CFG, left, right);
                        }
                    }
                }
                //continue;
            }
            if (block.GetInstructions().size() > 0) {
                previous = CFG.AddBlock(block, previous, left, right);
            } else {
                continue;
            }
            for (int index = 0; index < block.GetInstructions().size(); index++) {
                Instruction instruction = block.GetInstructions().get(index);
                if (instruction.instruction.contains("ret")) {
                    if (lastReturn != null) {
                        ArrayList<String> operands = instruction.GetOperands();
                        operands.add(lastReturn.name);
                        Instruction newInstruction = new Instruction("mov",operands);
                        block.GetInstructions().set(index,newInstruction);

                    }
                }
            }
            String lastRight = null;
            String lastLeft = null;
            if (block.GetLeft() != null) {
                BasicBlocks leftBlocks = block.GetLeft();
                lastLeft = CFGGraphBuilder(leftBlocks, previous,  CFG,true,false);
            }
            if (block.GetRight() != null) {
                BasicBlocks rightBlocks = block.GetRight();
                lastRight = CFGGraphBuilder(rightBlocks, previous, CFG, false,true);
            }
            if(lastRight != null || lastRight != null) {
                if (lastLeft == null) {
                    lastLeft = previous;
                }
                if (lastRight == null) {
                    lastRight = previous;
                }
                BasicBlock join = new BasicBlock();
                previous = CFG.AddJoin(join, lastLeft, lastRight);
            }
            left = false;
            right = false;
            if (CFG.GetNode(previous).GetInstructionSet().contains("jmp")) {
                CFG.GetNode(previous).next = CFG.GetNode(leastRecentLink);
                leastRecentLink = previous;
            }
        }
        return previous;
    }

    public Graph GetCFG() {
        Graph CFG = new Graph();
        String rootName = CFG.Root();
        boolean left = false;
        boolean right = false;
        CFGGraphBuilder(this.blocks, rootName, CFG, left, right);
        CFG.Compress();
        return CFG;
    }
}
