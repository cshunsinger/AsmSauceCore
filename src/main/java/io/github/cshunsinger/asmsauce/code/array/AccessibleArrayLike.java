package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

/**
 * This interface represents any code builder which is capable of potentially generating bytecode that places an
 * array type onto the jvm stack.
 * Any code builder which includes this interface will be able to perform these array operations on the array value
 * they place on the stack (NOTE: build errors will occur if an attempt is made to perform an array operation on a
 * non-array type on the jvm stack).
 */
public interface AccessibleArrayLike extends CodeInsnBuilderLike {
    /**
     * If an array value was stacked, this will produce a code builder to fetch an element from the array and place it
     * onto the stack.
     * @param indexBuilder Code builder to place an int onto the stack representing the index in the array to access.
     * @return A code builder which generates bytecode to retrieve a value from an array at an index.
     * @throws IllegalArgumentException If indexBuilder is null.
     */
    default ArrayLoadInsn get(CodeInsnBuilderLike indexBuilder) {
        ArrayLoadInsn insn = new ArrayLoadInsn(indexBuilder);
        insn.setPrev(this);
        this.setNext(insn);
        return insn;
    }

    /**
     * If an array value was stacked, this will produce a code builder to store a value into the array at the desired
     * index. This instruction consumes the array, the array index, and the value from the stack.
     * @param indexBuilder Code builder to place an int onto the stack representing the index in the array to access.
     * @param valueBuilder Code builder to place a value onto the stack, which will be stored in the array.
     * @return A code builder which generates bytecode to store a value in an array at an index.
     * @throws IllegalArgumentException If indexBuilder is null
     * @throws IllegalArgumentException If valueBuilder is null.
     */
    default ArrayStoreInsn set(CodeInsnBuilderLike indexBuilder, CodeInsnBuilderLike valueBuilder) {
        ArrayStoreInsn insn = new ArrayStoreInsn(indexBuilder, valueBuilder);
        insn.setPrev(this);
        this.setNext(insn);
        return insn;
    }

    /**
     * If an array value was stacked, this will produce a code builder to fetch the array length and stack it. The
     * bytecode generated will pop the array off the the stack and push the array length onto the stack.
     * @return A code builder which generates bytecode to fetch the length of an array.
     */
    default ArrayLengthInsn length() {
        ArrayLengthInsn insn = new ArrayLengthInsn();
        insn.setPrev(this);
        this.setNext(insn);
        return insn;
    }
}