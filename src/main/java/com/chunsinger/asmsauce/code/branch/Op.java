package com.chunsinger.asmsauce.code.branch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static aj.org.objectweb.asm.Opcodes.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Op {
    EQ(IFNE, IF_ICMPNE, IFNONNULL, IF_ACMPNE),
    NE(IFEQ, IF_ICMPEQ, IFNULL, IF_ACMPEQ),
    GE(IFLT, IF_ICMPLT, null, null),
    LE(IFGT, IF_ICMPGT, null, null),
    GT(IFLE, IF_ICMPLE, null, null),
    LT(IFGE, IF_ICMPGE, null, null),
    NOT_EQ(IFEQ, IF_ICMPEQ, IFNULL, IF_ACMPEQ),
    NOT_NE(IFNE, IF_ICMPNE, IFNONNULL, IF_ACMPNE),
    NOT_GE(IFGE, IF_ICMPGE, null, null),
    NOT_LE(IFLE, IF_ICMPLE, null, null),
    NOT_GT(IFGT, IF_ICMPGT, null, null),
    NOT_LT(IFLT, IF_ICMPLT, null, null);

    private final int singlePrimitiveOpcode;
    private final int doublePrimitiveOpcode;
    private final Integer nullCheckOpcode;
    private final Integer referenceOpcode;
}