package com.compiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Graph {
    public Node root;
    public String rootName;
    private int nameIndex;
    HashMap<String, Node> nodes;

    public Graph() {
        this.nameIndex = 0;
        rootName = GetName();
        this.root = new Node(rootName);
        this.nodes = new HashMap<>();
        this.nodes.put(this.rootName,this.root);
    }

    public String Root() {
        return this.rootName;
    }

    public String AddJoin(BasicBlock block, String previousLeft, String previousRight) {
        String name = GetName();
        Node nextNode = new Node(name, block.GetInstructions());
        this.nodes.put(name,nextNode);
        this.nodes.get(previousLeft).next = nextNode;
        this.nodes.get(previousRight).next = nextNode;
        nextNode.previous.add(this.nodes.get(previousLeft));
        nextNode.previous.add(this.nodes.get(previousRight));
        return name;
    }

    public String GetName() {
        char[] names = "abcdefghijklmnopqrstuvwxyz123456789".toCharArray();
        String name = new StringBuilder().append(names[nameIndex]).toString();
        nameIndex ++;
        return name;
    }

    public String AddBlock(BasicBlock block, String previous, boolean left, boolean right) {
        String name = GetName();
        Node nextNode = new Node(name, block.GetInstructions());
        this.nodes.put(name,nextNode);
        if (left) {
            this.nodes.get(previous).left = nextNode;
            nextNode.previous.add(this.nodes.get(previous));
        } else if (right) {
            this.nodes.get(previous).right = nextNode;
            nextNode.previous.add(this.nodes.get(previous));
        } else {
            this.nodes.get(previous).next = nextNode;
            nextNode.previous.add(this.nodes.get(previous));
        }
        return name;
    }

    public Node GetNode(String name) {
        return this.nodes.get(name);
    }

    private void Collapse(Node node, ArrayList<String> visited) {
        if (node == null) {
            return;
        }
        if (node.GetInstructions().size() == 0) {
            if (node.next != null) {
                if (node.previous.size() == 0) {
                    this.nodes.remove(this.root.name);
                    this.root = node.next;
                } else {
                    for (Node prev : node.previous) {
                        if (prev.left != null) {
                            if (prev.left.name.equals(node.name)) {
                                prev.left = node.next;
                            }
                        }
                        if (prev.right != null) {
                            if (prev.right.name.equals(node.name)) {
                                prev.right = node.next;
                            }
                        }
                        if (prev.next != null) {
                            if (prev.next.name.equals(node.name)) {
                                prev.next = node.next;
                            }
                        }
                    }
                    this.nodes.remove(node.name);
                }
            }
        }
        visited.add(node.name);
        if (node.left != null && !visited.contains(node.left.name)) {
            Collapse(node.left, visited);
        }
        if (node.right != null && !visited.contains(node.right.name)) {
            Collapse(node.right, visited);
        }
        if (node.next != null && !visited.contains(node.next.name)) {
            Collapse(node.next, visited);
        }

    }

    public void Compress() {
        ArrayList<String> visited = new ArrayList<>();
        Collapse(this.root, visited);
    }


    public void DFS(Node root, ArrayList<String> visited, ArrayList<String> invalid) {
        if (root == null) {
            return;
        } else {
            if (invalid.contains(root.name)) {
                return;
            } else {
                visited.add(root.name);
                if (root.left != null && !visited.contains(root.left.name)) {
                    DFS(root.left, visited, invalid);
                }
                if (root.right != null && !visited.contains(root.right.name)) {
                    DFS(root.right, visited, invalid);
                }
                if (root.next != null && !visited.contains(root.next.name)) {
                    DFS(root.next, visited, invalid);
                }
            }

        }

    }

    public HashMap<String, ArrayList<String>> DominanceFrontier () {
        ArrayList<String> nodes = new ArrayList<>();
        ArrayList<String> invalid = new ArrayList<>();
        HashMap<String,ArrayList<String>> frontier = new HashMap<>();
        DFS(this.root, nodes, invalid);
        ArrayList<String> previous = new ArrayList<>();
        for (String node : nodes) {
            invalid = new ArrayList<>();
            ArrayList<String> reachable = new ArrayList<>();
            invalid.add(node);
            previous.add(node);
            DFS(this.root,reachable,invalid);
            for (String original : nodes) {
                if (!original.equals(node)) {
                    if (!reachable.contains(original)) {
                        if (frontier.get(node) == null) {
                            ArrayList<String> dominators = new ArrayList<>();
                            dominators.add(original);
                            frontier.put(node, dominators);
                        } else {
                            frontier.get(node).add(original);
                        }
                    }
                }
            }
          for (String p : previous) {
                if (!p.equals(node)) {
                    if (frontier.get(p) != null && frontier.get(node) != null) {
                        for (String dom : frontier.get(node)) {
                            if (frontier.get(p).contains(dom)) {
                                frontier.get(p).remove(dom);
                            }
                        }
                    }
                }
            }
        }
        return frontier;
    }

    public void DrawFrontier(String dotfile, String png) throws IOException, InterruptedException {
        HashMap<String,ArrayList<String>> frontier = DominanceFrontier();
        PrintWriter writer = new PrintWriter(dotfile, "UTF-8");
        writer.println("digraph CFG {");
        Iterator it = frontier.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String label = pair.getKey().toString();
            ArrayList<String> domSet = frontier.get(pair.getKey());
            writer.println(label + " [shape=oval]");
            for (String domId : domSet) {
                if (domId != label) {
                    writer.println(label + " -> " + domId);
                }
            }
        }
        writer.println("}");
        writer.close();
        Process exec = Runtime.getRuntime().exec(new String[] { "dot", "-Tpng",dotfile,"-o",png});
        exec.waitFor();
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

    public void PaintVars(Node root, ArrayList<String> visited, ArrayList<String> variables) {
        ArrayList<Node> queue = new ArrayList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            root = queue.get(0);
            if (!visited.contains(root.name)) {
                for (Instruction instruction : root.GetInstructions()) {
                    ArrayList<String> operands = instruction.operands;
                    if (operands.size() == 2) {
                        if (!isInteger(operands.get(0))) {
                            int occurrences = Collections.frequency(variables, operands.get(0));
                            if (!instruction.instruction.equals("cmp") && !instruction.instruction.equals("write")) {
                                variables.add(operands.get(0));
                                String incOperand = operands.get(0) + "--" + occurrences;
                                int nextOcc = occurrences + 1;
                                instruction.AddAssign(operands.get(0) + "--" +nextOcc);
                                operands.set(0, incOperand);
                            } else {
                                int last_occurence = occurrences - 1;
                                String incOperand = operands.get(0) + "--" + occurrences;
                                operands.set(0, incOperand);
                            }

                        }
                        if (!isInteger(operands.get(1))) {
                            int occurrences = Collections.frequency(variables, operands.get(1));
                            if (!instruction.instruction.equals("cmp") && !instruction.instruction.equals("write")) {
                                //variables.add(operands.get(1));
                                String incOperand = operands.get(1) + "--" + occurrences;
                                operands.set(1, incOperand);
                            } else {
                                int last_occurence = occurrences - 1;
                                String incOperand = operands.get(1) + "--" + occurrences;
                                operands.set(1, incOperand);
                            }
                        }
                    } else if (operands.size() == 1) {
                        if (!isInteger(operands.get(0))) {
                            int occurrences = Collections.frequency(variables, operands.get(0));
                            if (!instruction.instruction.equals("cmp") && !instruction.instruction.equals("write")) {
                                variables.add(operands.get(0));
                                String incOperand = operands.get(0) + "--" + occurrences;
                                operands.set(0, incOperand);
                            } else {
                                int last_occurence = occurrences - 1;
                                if (last_occurence < 0) {
                                    last_occurence = 0;
                                }
                                String incOperand = operands.get(0) + "--" + occurrences;
                                operands.set(0, incOperand);
                            }
                        }
                    }
                }
                if (root.left != null && !visited.contains(root.left.name)) {
                    queue.add(root.left);
                }
                if (root.right != null && !visited.contains(root.right.name)) {
                    queue.add(root.right);
                }

                if (root.next != null && !visited.contains(root.next.next)) {
                    queue.add(root.next);
                }
                visited.add(root.name);

            }
            queue.remove(root);
        }
    }



    public void ApplyCSE() {
        ArrayList<String> expressions = new ArrayList<>();
        HashMap<String,ArrayList<String>> frontier = DominanceFrontier();
        ArrayList<Node> path = new ArrayList<>();
        ArrayList<Node> visited = new ArrayList<>();
        path.add(this.root);
        while (!path.isEmpty()) {
            Node root = path.get(path.size() - 1);
            path.remove(root);
            if (visited.contains(root)) {
                continue;
            }
            String label = root.name;
            ArrayList<String> domSet = frontier.get(label);
            ArrayList<Instruction> toRemove = new ArrayList<>();
            for (int index = 0; index < root.GetInstructions().size(); index++) {
                Instruction i = root.GetInstructions().get(index);
                String cmd = i.Emit();
                if (expressions.contains(cmd)) {
                    if (!label.equals(this.root.name)) {
                        toRemove.add(i);
                    }
                } else {
                    if (domSet != null) {
                        expressions.add(cmd);
                    }
                }
            }
            for (Instruction i : toRemove) {
                System.out.println("Removing Instruction " + i.Emit() + " from " + label);
                root.GetInstructions().remove(i);
            }

            if (domSet != null) {
                for (String domId : domSet) {
                    if (domId != label) {
                        Node node = this.nodes.get(domId);
                        path.add(node);
                    }
                }
            }
        }
    }

    public void ApplyConstantPropagation() {
        HashMap<String,ArrayList<String>> frontier = DominanceFrontier();
        HashMap<String,String> propagations = new HashMap<>();
        ArrayList<Node> path = new ArrayList<>();
        ArrayList<Node> visited = new ArrayList<>();
        path.add(this.root);
        while (!path.isEmpty()) {
            HashMap<String, Instruction> evaluations = new HashMap<>();
            Node root = path.get(0);
            path.remove(root);
            if (visited.contains(root)) {
                continue;
            }
            visited.add(root);
            for (int index = 0; index < root.GetInstructions().size(); index++) {
                Instruction i = root.GetInstructions().get(index);
                if (i.assignment != null) {
                    evaluations.put(i.assignment,i);
                }
            }
            ArrayList<String> seen = new ArrayList<>();
            ArrayList<Integer> toRemove = new ArrayList<>();
            for (int index = 0; index < root.GetInstructions().size(); index++) {
                Instruction i = root.GetInstructions().get(index);
                if (i.instruction.equals("mov")) {
                    if (i.operands.size() > 0) {
                        if (propagations.containsKey(i.operands.get(0))) {
                            String arg = i.operands.get(0);
                            System.out.println("Propagated " + propagations.get(arg));
                            i.operands.set(0,propagations.get(arg));
                            propagations.remove(arg);
                        }
                    }
                    if (i.operands.size() == 2) {
                        if (evaluations.containsKey(i.operands.get(1))) {
                            Instruction remap = evaluations.get(i.operands.get(1));
                            System.out.println(i.Emit() + " mapping to " + remap.Emit() + " in " + root.name);
                            root.GetInstructions().set(index,remap);
                        } else {
                            System.out.println("Propagating " + i.Emit());
                            propagations.put(i.assignment,i.operands.get(1));
                            toRemove.add(index);
                        }
                    }
                }
            }
            for (int remove : toRemove) {
                root.GetInstructions().remove(remove);
            }
            for (int index = 0; index < root.GetInstructions().size(); index++ ) {
                Instruction i = root.GetInstructions().get(index);
                if (!seen.contains(i.Emit())) {
                    seen.add(i.Emit());
                } else {
                    root.GetInstructions().remove(index);
                }
            }

            if (frontier.get(root.name) != null) {
                for (String domId : frontier.get(root.name)) {
                    if (domId != root.name) {
                        path.add(this.nodes.get(domId));
                    }
                }
            }
        }

    }

    public void RenameVariables() {
        ArrayList<String> visited = new ArrayList<>();
        ArrayList<String> variables = new ArrayList<>();
        PaintVars(this.root,visited,variables);
    }

    public void PlacePhi() {
        ApplyConstantPropagation();
        ArrayList<String> nodes = new ArrayList<>();
        ArrayList<String> invalid = new ArrayList<>();
        DFS(this.root,nodes,invalid);
        HashMap<String, ArrayList<String>> frontier = DominanceFrontier();
        HashMap<String, ArrayList<String>> inverted = new HashMap<>();
        for (String node : nodes) {
            if (frontier.containsKey(node)) {
                for (String dom : frontier.get(node)) {
                    if (inverted.containsKey(dom)) {
                        inverted.get(dom).add(node);
                    } else {
                        ArrayList<String> dominators = new ArrayList<>();
                        dominators.add(node);
                        inverted.put(dom,dominators);
                    }
                }
            }
        }
        ArrayList<String> phiPlacement = new ArrayList<>();
        for (String node : nodes) {
                if (!frontier.containsKey(node)) {
                    ArrayList<String> vars = new ArrayList<>();
                    ArrayList<String> idomSet = new ArrayList<>();
                    ArrayList<String> below = new ArrayList<>();
                    Node current = this.nodes.get(node);
                    if (current.left != null) {
                        below.add(current.left.name);
                    }
                    if (current.right != null) {
                        below.add(current.right.name);
                    }
                    if (current.next != null) {
                        below.add(current.next.name);
                    }
                    for (String other : nodes) {
                        if (!other.equals(node)) {
                            Node above = this.nodes.get(other);
                            if (above.left != null && above.left.name.equals(node) && !below.contains(above.left.name)) {
                                idomSet.add(above.name);
                            }
                            if (above.right != null && above.right.name.equals(node) && !below.contains(above.right.name)) {
                                idomSet.add(above.name);
                            }
                            if (above.next != null && above.next.name.equals(node) && !below.contains(above.next.name)) {
                                idomSet.add(above.name);
                            }
                        }
                    }
                    for (String dom : idomSet) {
                        Node dominator = this.nodes.get(dom);
                        for (String var: dominator.GetAssignments()) {
                            if (!isInteger(var)) {
                                vars.add(var);
                            }
                        }
                        if (dominator.left != null && !below.contains(dominator.left.name)) {
                            for (String var : dominator.left.GetAssignments()) {
                                if (!isInteger(var)) {
                                    vars.add(var);
                                }
                            }
                        }
                        if (dominator.right != null && !below.contains(dominator.right.name)) {
                            for (String var : dominator.right.GetAssignments()) {
                                if (!isInteger(var)) {
                                    vars.add(var);
                                }
                            }
                        }
                        if (dominator.next != null && !below.contains(dominator.next.name)) {
                            for (String var : dominator.next.GetAssignments()) {
                                if (!isInteger(var)) {
                                    vars.add(var);
                                }
                            }
                        }
                    }

                    if (current.next != null && !phiPlacement.contains(current.next.name)) {
                        current.next.AddPhi(vars);
                        phiPlacement.add(current.next.name);
                    }
                    else if (current.left != null && !phiPlacement.contains(current.left.name)) {
                        current.left.AddPhi(vars);
                        phiPlacement.add(current.left.name);
                    }
                    else if (current.right != null && !phiPlacement.contains(current.right.name)) {
                        current.right.AddPhi(vars);
                        phiPlacement.add(current.right.name);
                    }

                }
        }
    }

    public void WriteAssembly(HashMap<String,Integer> registers) throws IOException, InterruptedException {
        Node root = this.root;
        HashMap<String,ArrayList<String>> frontier = this.DominanceFrontier();
        ArrayList<Node> visited = new ArrayList<>();
        ArrayList<Node> queue = new ArrayList<>();
        Integer line = 0;
        queue.add(root);
        Integer addressCount = 0;
        PrintWriter writer = new PrintWriter("final.asm", "UTF-8");
        writer.println("; /usr/local/bin/nasm -f macho64 64.asm && ld -macosx_version_min 10.7.0 -lSystem -o 64 64.o && ./64");
        writer.println("global start");
        writer.println(".section text");
        ArrayList<String> vars = new ArrayList<>();
        ArrayList<String> init = new ArrayList<>();
        while (!queue.isEmpty()) {
            Node node = queue.get(0);
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            queue.remove(node);
            ArrayList<Instruction> toRemove = new ArrayList<>();

            for (Instruction i : node.GetInstructions()) {
                String inst[] =  i.Emit().split("\\s+");
                if (inst.length == 5) {
                    Integer assignNum = Integer.parseInt(inst[0].split("--")[1]);
                    Integer op1Num = Integer.parseInt(inst[3].split("--")[1]);
                    if (assignNum <= op1Num) {
                        assignNum = op1Num + 1;
                        i.assignment = inst[0].split("--")[0] + "--" + assignNum;
                        if (registers.containsKey(i.assignment)) {
                            i.assignment = "R" + registers.get(i.assignment);
                        } else {
                            i.assignment ="A" + addressCount;
                            addressCount++;
                        }
                        vars.add(i.assignment);
                        if (registers.containsKey(inst[3])) {
                            i.operands.set(0,"R" + registers.get(inst[3]));
                        } else {
                            i.operands.set(0, "A" + addressCount);
                            addressCount++;
                        }
                        vars.add(i.operands.get(0));
                        if (registers.containsKey(inst[4])) {
                            i.operands.set(1,"R" + registers.get(inst[4]));
                        } else {
                            i.operands.set(1, "A" + addressCount);
                            addressCount++;
                        }
                        vars.add(i.operands.get(1));
                    } else {
                        if (registers.containsKey(inst[0])) {
                            i.assignment = "R" + registers.get(inst[0]);
                        } else {
                            i.assignment = "A" + addressCount;
                            addressCount++;
                        }
                        vars.add(i.assignment);
                        if (registers.containsKey(inst[3])) {
                            i.operands.set(0,"R" + registers.get(inst[3]));
                        } else {
                            i.operands.set(0, "A" + addressCount);
                            addressCount++;
                        }
                        vars.add(i.operands.get(0));
                        if (registers.containsKey(inst[4])) {
                            i.operands.set(1,"R" + registers.get(inst[4]));
                        } else {
                            i.operands.set(1, "A" + addressCount);
                            addressCount++;
                        }
                        vars.add(i.operands.get(1));
                    }
                } else  {
                    if (i.GetOperands().size() > 0) {
                        if (registers.containsKey(inst[1])) {
                            i.operands.set(0, "R" + registers.get(inst[1]));
                        } else {
                            i.operands.set(0, "A" + addressCount);
                            addressCount++;
                        }
                        vars.add(i.operands.get(0));
                    }
                    if (i.GetOperands().size() > 1) {
                        if (registers.containsKey(inst[2])) {
                            i.operands.set(1, "R" + registers.get(inst[2]));
                        } else {
                            i.operands.set(1, "A" + addressCount);
                            addressCount++;
                        }
                        vars.add(i.operands.get(1));
                    } else {

                    }
                }
                if (i.Emit().contains("phi")) {
                    toRemove.add(i);
                }
            }
            writer.println(node.name + ":");
            for (Instruction i : toRemove) {
                node.instructions.remove(i);
            }
            for (String var : vars) {
                if (!init.contains(var)) {
                    writer.println("\tmov " + var + ", " + "0");
                    init.add(var);
                }
            }
            for (Instruction i : node.GetInstructions()) {
                if (i.operands.size() == 2) {
                    writer.println("\t"+i.instruction+"\t"+i.operands.get(0)+ ", " +i.operands.get(1));
                }
                else if (i.operands.size() == 1) {
                    writer.println("\t"+i.instruction+"\t"+i.operands.get(0));
                }
                else {
                    writer.println("\t"+i.instruction);
                }
                if (i.jump != null) {
                    if (node.right != null) {
                        writer.println(i.jump + " " + node.right.name);
                    }
                    else if (node.next != null) {
                        writer.println(i.jump + " " + node.next.name);
                    }
                    else if (node.left != null) {
                        writer.println("\t" + i.jump + " " + node.left.name);
                    }
                }
            }
            if (frontier.get(node.name) != null) {
                for (String next : frontier.get(node.name)) {
                    queue.add(this.nodes.get(next));
                }
            }
        }
        writer.close();
    }

    public void Draw(String dotfile, String png) throws IOException, InterruptedException {
        PrintWriter writer = new PrintWriter(dotfile, "UTF-8");
        writer.println("digraph CFG {");
        Iterator it = this.nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Node node = this.nodes.get(pair.getKey());
            String instructions = node.name + "\n" +node.GetInstructionSet().replaceAll("--","");
            writer.println(node.name + " [label= \""+ instructions +"\", shape=box]");
            if (node.left != null) {
                writer.println(node.name + " -> " + node.left.name);
            }
            if (node.right != null) {
                writer.println(node.name + " -> " + node.right.name);
            }
            if (node.next != null) {
                writer.println(node.name + " -> " + node.next.name);
            }
            //it.remove();
        }
        writer.println("}");
        writer.close();
        Process exec = Runtime.getRuntime().exec(new String[] { "dot", "-Tpng",dotfile,"-o",png});
        exec.waitFor();
    }

}
