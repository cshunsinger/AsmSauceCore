package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

public interface AccessibleArrayLike extends CodeInsnBuilderLike {
    default ArrayLoadInsn get(CodeInsnBuilderLike indexBuilder) {
        ArrayLoadInsn insn = new ArrayLoadInsn(indexBuilder);
        insn.setPrev(this);
        this.setNext(insn);
        return insn;
    }

    default ArrayStoreInsn set(CodeInsnBuilderLike indexBuilder, CodeInsnBuilderLike valueBuilder) {
        ArrayStoreInsn insn = new ArrayStoreInsn(indexBuilder, valueBuilder);
        insn.setPrev(this);
        this.setNext(insn);
        return insn;
    }

    default ArrayLengthInsn length() {
        ArrayLengthInsn insn = new ArrayLengthInsn();
        insn.setPrev(this);
        this.setNext(insn);
        return insn;
    }
}