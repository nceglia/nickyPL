package com.compiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class RegisterAllocator {

    public Graph cfg;
    public Integer registers;

    public RegisterAllocator(Graph cfg) {
        this.cfg = cfg;
        this.registers = 8;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    public HashMap<String,Integer> GetRegisters() throws IOException, InterruptedException {
        ArrayList<String> allvalues = new ArrayList<>();
        HashMap<String,ArrayList<Integer>> ranges = new HashMap<>();
        Node root = this.cfg.root;
        HashMap<String,ArrayList<String>> frontier = this.cfg.DominanceFrontier();
        ArrayList<Node> visited = new ArrayList<>();
        ArrayList<Node> queue = new ArrayList<>();
        Integer line = 0;
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.get(0);
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            queue.remove(node);
            for (Instruction i : node.GetInstructions()) {
                ArrayList<String> values = new ArrayList<>();
                String inst[] =  i.Emit().split("\\s+");
                if (inst.length == 5) {
                    Integer assignNum = Integer.parseInt(inst[0].split("--")[1]);
                    Integer op1Num = Integer.parseInt(inst[3].split("--")[1]);
//                    Integer op2Num = Integer.parseInt(inst[4].split("--")[1]);
                    if (assignNum <= op1Num) {
                        assignNum = op1Num + 1;
                        i.assignment = inst[0].split("--")[0] + "--" + assignNum;
                        values.add(i.assignment);
                        values.add(inst[3]);
                        values.add(inst[4]);
                    } else {
                        values.add(inst[0]);
                        values.add(inst[3]);
                        values.add(inst[4]);
                    }
                } else if (inst.length == 4) {
                    //values.add(inst[1]);
                    //values.add(inst[2]);
                } else if (inst.length == 3) {
                    values.add(inst[1]);
                    values.add(inst[2]);
                }
                for (String val : values) {
                    if (!allvalues.contains(val)) {
                        allvalues.add(val);
                    }
                    if (!ranges.containsKey(val)) {
                        ArrayList<Integer> lines = new ArrayList<>();
                        ranges.put(val,lines);
                    }
                    ranges.get(val).add(line);
                }
                line ++;
            }
            //line ++;
            if (frontier.get(node.name) != null) {
                for (String next : frontier.get(node.name)) {
                    queue.add(this.cfg.nodes.get(next));
                }
            }
        }
        HashMap<String,ArrayList<String>> interfernce = new HashMap<>();
        System.out.println("****************Live Ranges******************");
        for (String val : allvalues) {
            if (val != null && !isInteger(val)) {
                if (ranges.get(val) != null) {
                    System.out.print(val.replace("--", "") + " => ");
                    for (Integer lineNum : ranges.get(val)) {
                        System.out.print(lineNum + " ");
                    }
                    System.out.print("\n");
                }
            }
        }
        System.out.println("************ Building Interference Graph **************");
        HashMap<String,ArrayList<String>> vertices = new HashMap<>();
        PrintWriter writer = new PrintWriter("INT.dot", "UTF-8");
        writer.println("graph CFG {");
        ArrayList<String> edges = new ArrayList<>();
        ArrayList<String> fedges = new ArrayList<>();
        boolean found = false;
        for (String val : allvalues) {
            ArrayList<String> conflict = new ArrayList<>();
            interfernce.put(val, conflict);
            if (val != null && !isInteger(val)) {
                writer.println(val.replace("--","") + " [shape=oval]");
                if (ranges.get(val) != null) {
                    for (Integer lineNum : ranges.get(val)) {
                        found = false;
                        for (String other : allvalues) {
                            if (!other.equals(val)) {
                                if (other != null && !isInteger(other)) {
                                    if (ranges.get(other) != null) {
                                        for (Integer otherNum : ranges.get(other)) {
                                            if (otherNum == lineNum) {
                                                //System.out.println(val + " interferes with " + other);
                                                String edge = val.replace("--","") + " -- " + other.replace("--","");
                                                String opposing = other.replace("--","") + " -- " + val.replace("--","");
                                                if (!edges.contains(edge) && !edges.contains(opposing)) {
                                                    if (!vertices.containsKey(val)) {
                                                        ArrayList<String> connections = new ArrayList<>();
                                                        vertices.put(val, connections);
                                                    }
                                                    if (!vertices.containsKey(other)) {
                                                        ArrayList<String> opConnections = new ArrayList<>();
                                                        vertices.put(other,opConnections);
                                                    }
                                                    vertices.get(val).add(other);
                                                    vertices.get(other).add(val);
                                                    writer.println(edge);
                                                    edges.add(edge);
                                                    fedges.add(edge);
                                                    edges.add(opposing);
                                                }
                                                interfernce.get(val).add(other);
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (found) {
                                break;
                            }
                        }
                    }
                }
            }

        }
        writer.println("}");
        writer.close();
        Process exec = Runtime.getRuntime().exec(new String[] { "dot", "-Tpng","INT.dot","-o","INT.png"});
        exec.waitFor();
        System.out.println("*********************** Coloring Interference Graoh **************************");
        writer = new PrintWriter("INT-Color.dot", "UTF-8");
        writer.println("graph CFG {");
        String[] colors = new String[9];
        colors[0] = "blue";
        colors[1] = "red";
        colors[2] = "green";
        colors[3] = "yellow";
        colors[4] = "purple";
        colors[5] = "coral";
        colors[6] = "cyan";
        colors[7] = "crimson";
        colors[8] = "black";
        HashMap<String,Integer> assignments = new HashMap<>();
        for (String val : allvalues) {
            if (val != null && !isInteger(val)) {
                assignments.put(val,0);
            }
        }
        boolean toColor;
        ArrayList<String> seen = new ArrayList<>();
        for (String val : allvalues) {
            if (val != null && !isInteger(val) && !seen.contains(val)) {
                if (vertices.get(val) != null) {
                    boolean assigned = false;
                    for (int i = 0; i < this.registers; i++) {
                        toColor = true;
                        for (String neighbor : vertices.get(val)) {
                            if (neighbor == val) {
                                continue;
                            }
                            if (neighbor != null && !isInteger(neighbor)) {
                                if (assignments.get(neighbor) == i) {
                                    toColor = false;
                                }
                            }
                        }
                        if (toColor) {
                            System.out.println(val + " " + i);
                            assignments.put(val,i);
                            assigned = true;
                            break;
                        }
                    }
                    if (!assigned) {
                        assignments.put(val,8);
                    }
                }
                writer.println(val.replace("--","") + " [shape=oval color=" + colors[assignments.get(val)] + "]");
                seen.add(val);
            }
        }
        for (String edge : fedges) {
            writer.println(edge);
        }
        writer.println("}");
        writer.close();
        Process exec2 = Runtime.getRuntime().exec(new String[] { "dot", "-Tpng","INT-Color.dot","-o","INT-Color.png"});
        exec2.waitFor();
        return assignments;
    }

}
