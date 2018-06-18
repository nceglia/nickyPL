package com.compiler;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Instruction {

    String instruction;
    String assignment;
    String jump;
    ArrayList<String> operands;
    Result x;
    Result y;

    public Instruction(TokenType op, Result result) {
        this.x = result;
        this.y = null;
        this.instruction = OpToCode(op);
        this.operands = GetOperands();
    }

    public Instruction(TokenType op) {
        this.x = null;
        this.y = null;
        this.instruction = OpToCode(op);
        this.operands = GetOperands();
    }


    public Instruction(String op) {
        this.x = null;
        this.y = null;
        this.instruction = op;
        this.operands = GetOperands();
    }

    public Instruction(String op, Result result) {
        this.x = result;
        this.y = null;
        this.instruction = op;
        this.operands = GetOperands();
    }

    public Instruction(String op, ArrayList<String> operands) {
        this.x = null;
        this.y = null;
        this.instruction = op;
        this.operands = operands;
    }

    public Instruction(TokenType op, Result x, Result y) {
        this.x = x;
        this.y = y;
        this.instruction = OpToCode(op);
        this.operands = GetOperands();
    }

    public void AddAssign(String assignment) {
        this.assignment = assignment;
    }

    private String OpToCode(TokenType op) {
        String code;
        switch(op) {
            case plusToken:
                code = "add";
                break;
            case minusToken:
                code = "sub";
                break;
            case divToken:
                code = "div";
                break;
            case timesToken:
                code = "mul";
                break;
            case becomesToken:
                code = "mov";
                break;
            case lssToken:
                code = "cmp";
                this.jump = "jge";
                break;
            case gtrToken:
                code = "cmp";
                this.jump = "jle";
                break;
            case geqToken:
                code = "cmp";
                this.jump = "jl";
                break;
            case leqToken:
                code = "cmp";
                this.jump = "jg";
                break;
            case eqlToken:
                code = "cmp";
                this.jump = "jne";
                break;
            case neqToken:
                code = "cmp";
                this.jump = "je";
                break;
            default:
                code = "unk";
                break;
        }
        return code;
    }

    public ArrayList<String> GetOperands() {
        ArrayList<String> operands = new ArrayList<String>();
        if (this.x != null) {
            switch(this.x.kind) {
                case variable:
                    if (x.offset.size() > 0) {
                        String offsetting = "";
                        for (String offset : x.offset) {
                            offsetting += " " + offset;
                        }
                        operands.add("[" + this.x.name + "+" + offsetting + "]");
                    } else {
                        operands.add(this.x.name);
                    }
                    break;
                case register:
                    operands.add("R" + this.x.register.toString());
                    break;
                case constant:
                    operands.add(this.x.value.toString());
                    break;
                default:
                    System.out.println("Operand Error - " + this.x.message);
                    break;
            }
        }
        if (this.y != null) {
            switch(this.y.kind) {
                case variable:
                    if (y.offset.size() > 0) {
                        String offsetting = "";
                        for (String offset : y.offset) {
                            offsetting += " " + offset;
                        }
                        operands.add("[" + this.y.name + "+" + offsetting + "]");
                    } else {
                        operands.add(this.y.name);
                    }
                    break;
                case register:
                    operands.add("R" + this.y.register.toString());
                    break;
                case constant:
                    operands.add(this.y.value.toString());
                    break;
                default:
                    System.out.println("Operand Error - " + this.y.message);
                    break;
            }
        }
        return operands;
    }

    public String Emit() {
        String full_instruction = instruction;
        for (String operand: this.operands) {
            full_instruction += " " + operand;
        }
        if (this.jump != null) {
            full_instruction += "\n"+this.jump;
        }
        if (this.assignment != null) {
            full_instruction = this.assignment + " <- " + full_instruction;
        }
        return full_instruction;
    }
}
