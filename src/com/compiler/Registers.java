package com.compiler;

import java.util.Arrays;

public class Registers {

    boolean[] registers;

    public Registers(Integer size) {
        registers = new boolean[size];
        Arrays.fill(registers, Boolean.FALSE);
    }

    public Integer GetNextAvailable() {
        Integer register = -1;
        for (Integer i = 0; i < registers.length; i++) {
            if (registers[i] == Boolean.FALSE) {
                register = i;
                registers[i] = Boolean.TRUE;
                break;
            }
        }
        return register;
    }

    public void Release(Integer i) {
        registers[i] = Boolean.TRUE;
    }
}
