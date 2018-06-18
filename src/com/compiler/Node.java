package com.compiler;

import java.util.ArrayList;
import java.util.Collections;

public class Node {
    public String name;
    public Node left;
    public Node right;
    public Node next;
    public ArrayList<Node> previous;

    public ArrayList<Instruction> instructions;

    public Node(String name) {
        this.name = name;
        this.instructions = new ArrayList<>();
        this.left = null;
        this.right = null;
        this.next = null;
        this.previous = new ArrayList<>();
    }

    public Node(String name, ArrayList<Instruction> instructions) {
        this.name = name;
        this.instructions = instructions;
        this.left = null;
        this.right = null;
        this.next = null;
        this.previous = null;
        this.previous = new ArrayList<>();
    }

    public ArrayList<String> GetAssignments() {
        ArrayList<String> assignments = new ArrayList<>();
        for (Instruction i : this.instructions) {
            if(!i.instruction.equals("phi")) {
                if (i.assignment != null) {
                    assignments.add(i.assignment);
                }
            }
        }
        return assignments;
    }

    public ArrayList<String> GetVariables() {
        ArrayList<String> variables = new ArrayList<>();
        for (Instruction instruction : this.instructions) {
            if (!instruction.instruction.equals("phi")) {
                for (String operand : instruction.operands) {
                    variables.add(operand);
                }
            }
        }
        return variables;
    }
    public void AddPhi(ArrayList<String> variables) {
        ArrayList<String> roots = new ArrayList<>();
        for (String var : variables) {
            String varComp[] = var.split("--");
            roots.add(varComp[0]);
        }
        ArrayList<String> visited = new ArrayList<>();
        for (String root : roots) {
            if (Collections.frequency(roots,root) > 1 && !visited.contains(root)) {
                ArrayList<String> args = new ArrayList<>();
                for (String var : variables) {
                    if (var.split("--")[0].equals(root)) {
                        args.add(var);
                    }
                }
                ArrayList<String> finalArgs = new ArrayList<>();
                Integer maxIndex = -1;
                String maxArg = null;
                for (String arg : args) {
                    Integer valueNum = Integer.parseInt(arg.split("--")[1]);
                    if (valueNum > maxIndex) {
                        maxIndex = valueNum;
                        maxArg  = arg;
                    }
                }
                finalArgs.add(maxArg);
                args.remove(maxArg);
                Integer topIndex = -1;
                String topArg = null;
                for (String arg : args) {
                    Integer valueNum = Integer.parseInt(arg.split("--")[1]);
                    if (valueNum > topIndex) {
                        topIndex = valueNum;
                        topArg  = arg;
                    }
                }
                finalArgs.add(topArg);
                Instruction phiInstruction = new Instruction("phi",finalArgs);
                this.instructions.add(0,phiInstruction);
                visited.add(root);
            }
        }
        for (String root : visited) {
            for (Instruction instruction : this.instructions) {
                if (!instruction.instruction.equals("phi")) {
                    for (int index = 0; index < instruction.operands.size(); index++) {
                        if (instruction.operands.get(index).split("--")[0].equals(root)) {
                            Integer newIncr = Integer.parseInt(instruction.operands.get(index).split("--")[1]) + 1;
                            String newOperand = instruction.operands.get(index).split("--")[0] + "--" + newIncr;
                            instruction.operands.set(index, newOperand);
                        }
                    }
                }
            }
            ArrayList<Node> queue = new ArrayList<>();
            visited = new ArrayList<>();
            queue.add(this);
            while(queue.size() > 0) {
                Node current = queue.get(0);
                queue.remove(current);
                if (visited.contains(current.name)) {
                    continue;
                }
                visited.add(current.name);
                if (current.left != null) {
                    for (Instruction instruction : current.left.instructions) {
                        if (instruction.instruction != "phi") {
                            for (int index = 0; index < instruction.operands.size(); index++) {
                                if (instruction.operands.get(index).split("--")[0].equals(root)) {
                                    Integer newIncr = Integer.parseInt(instruction.operands.get(index).split("--")[1]) + 1;
                                    String newOperand = instruction.operands.get(index).split("--")[0] + "--" + newIncr;
                                    instruction.operands.set(index, newOperand);
                                }
                            }
                        }
                    }
                    queue.add(this.left);
                }
                if (current.right != null) {
                    for (Instruction instruction : current.right.instructions) {
                        if (instruction.instruction != "phi") {
                            for (int index = 0; index < instruction.operands.size(); index++) {
                                if (instruction.operands.get(index).split("--")[0].equals(root)) {
                                    Integer newIncr = Integer.parseInt(instruction.operands.get(index).split("--")[1]) + 1;
                                    String newOperand = instruction.operands.get(index).split("--")[0] + "--" + newIncr;
                                    instruction.operands.set(index, newOperand);
                                }
                            }
                        }
                    }
                    queue.add(this.right);
                }
                if (current.next != null) {
                    for (Instruction instruction : current.next.instructions) {
                        if (instruction.instruction != "phi") {
                            for (int index = 0; index < instruction.operands.size(); index++) {
                                if (instruction.operands.get(index).split("--")[0].equals(root)) {
                                    Integer newIncr = Integer.parseInt(instruction.operands.get(index).split("--")[1]) + 1;
                                    String newOperand = instruction.operands.get(index).split("--")[0] + "--" + newIncr;
                                    instruction.operands.set(index, newOperand);
                                }
                            }
                        }
                    }
                    queue.add(this.next);
                }
            }
        }

    }
    public String GetInstructionSet() {
        String instructionset = "";
        for (Instruction instr : this.instructions) {
            instructionset += instr.Emit() + "\n";
        }
        return instructionset;
    }

    public ArrayList<Instruction> GetInstructions() {
        return this.instructions;
    }


}
