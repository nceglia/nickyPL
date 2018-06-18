package com.compiler;

import java.util.ArrayList;

public class BasicBlock {

    private ArrayList<Instruction> instructions;
    Result function;
    ArrayList<Result> arguments;
    BasicBlocks left;
    BasicBlocks right;

    public BasicBlock() {
        instructions = new ArrayList<>();
        left = null;
        right = null;
        arguments = new ArrayList<>();
        function = null;
    }

    public BasicBlock(Result function, ArrayList<Result> arguments) {
        instructions = new ArrayList<>();
        this.arguments = new ArrayList<>();
        left = null;
        right = null;
        this.function = function;
        if (arguments != null) {
            for (Result argument: arguments) {
                this.arguments.add(argument);
            }
        }
    }

    public void SetLeft(BasicBlocks block) {
        left = block;
    }

    public void SetRight(BasicBlocks block) {
        right = block;
    }

    public BasicBlocks GetRight() {
        return right;
    }

    public BasicBlocks GetLeft() {
        return left;
    }

    public void AddInstructions(ArrayList<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            this.instructions.add(instruction);
        }
    }

    public void AddInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public ArrayList<Instruction> GetInstructions() {
        return this.instructions;
    }

}
