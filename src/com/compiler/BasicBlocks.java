package com.compiler;

import java.util.ArrayList;

public class BasicBlocks {

    private BasicBlock root;
    private ArrayList<BasicBlock> blocks;
    private Result returnVal;

    public BasicBlocks() {
        this.root = new BasicBlock();
        this.blocks = new ArrayList<BasicBlock>();
        this.blocks.add(this.root);
    }

    public void AddBlock(BasicBlock block) {
        if (block.function != null) {
            this.blocks.add(block);
            return;
        }
        for (Instruction i : block.GetInstructions()) {
            if (i.Emit().contains("cmp") ) {
                BasicBlock comparisonBlock = new BasicBlock();
                comparisonBlock.AddInstruction(i);
                this.blocks.add(comparisonBlock);
                BasicBlock nextBlock = new BasicBlock();
                //this.blocks.add(nextBlock);
            } else {
                this.blocks.get(this.blocks.size()-1).AddInstruction(i);
            }
        }
    }

    public void AddReturn(Result value) {
        this.returnVal = value;
    }

    public Result GetReturn() {
        return this.returnVal;
    }

    public void AddBranch(BasicBlocks leftGraph, BasicBlocks rightGraph) {
        BasicBlock current = this.blocks.get(this.blocks.size() - 1);
        if (current.GetLeft() == null){
            current.SetLeft(leftGraph);
        }
        if (current.GetRight() == null) {
            current.SetRight(rightGraph);
        }
        BasicBlock joinBlock = new BasicBlock();
        this.blocks.add(joinBlock);
    }

    public ArrayList<BasicBlock> Blocks() {
        return this.blocks;
    }

    public ArrayList<BasicBlock> Traverse() {
        ArrayList<BasicBlock> blocks = new ArrayList<>();
        for (BasicBlock block: this.blocks) {
            blocks.add(block);
            if (block.GetRight() != null) {
                for (BasicBlock leftBlock : block.GetRight().Traverse()) {
                    blocks.add(leftBlock);
                }
            }
            if (block.GetLeft() != null) {
                for (BasicBlock rightBlock : block.GetLeft().Traverse()) {
                    blocks.add(rightBlock);
                }
            }

        }
        return blocks;
    }
}
