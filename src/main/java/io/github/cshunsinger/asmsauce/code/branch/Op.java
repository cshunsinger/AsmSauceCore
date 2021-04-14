package io.github.cshunsinger.asmsauce.code.branch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Enumeration of the condition operations for comparing one or two operands in a condition.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Op {
    /**
     * Equality comparison.
     */
    EQ(IFNE, IF_ICMPNE, IFNONNULL, IF_ACMPNE),
    /**
     * Inequality comparison.
     */
    NE(IFEQ, IF_ICMPEQ, IFNULL, IF_ACMPEQ),
    /**
     * Greater-or-equal comparison.
     */
    GE(IFLT, IF_ICMPLT, null, null),
    /**
     * Less-or-equal comparison.
     */
    LE(IFGT, IF_ICMPGT, null, null),
    /**
     * Greater-than comparison.
     */
    GT(IFLE, IF_ICMPLE, null, null),
    /**
     * Less-than comparison.
     */
    LT(IFGE, IF_ICMPGE, null, null),
    /**
     * Inverse equality comparison.
     */
    NOT_EQ(IFEQ, IF_ICMPEQ, IFNULL, IF_ACMPEQ),
    /**
     * Inverse inequality comparison.
     */
    NOT_NE(IFNE, IF_ICMPNE, IFNONNULL, IF_ACMPNE),
    /**
     * Inverse greater-or-equal comparison.
     */
    NOT_GE(IFGE, IF_ICMPGE, null, null),
    /**
     * Inverse less-or-equal comparison.
     */
    NOT_LE(IFLE, IF_ICMPLE, null, null),
    /**
     * Inverse greater-than comparison.
     */
    NOT_GT(IFGT, IF_ICMPGT, null, null),
    /**
     * Inverse less-than comparison.
     */
    NOT_LT(IFLT, IF_ICMPLT, null, null);

    /**
     * The opcode used when comparing a single primitive operand to zero.
     */
    private final int singlePrimitiveOpcode;
    /**
     * The opcode used when comparing two primitive operands with each other.
     */
    private final int doublePrimitiveOpcode;
    /**
     * For ops which are valid for reference types, this is the opcode for checking if a reference is null.
     * For ops which do not work for reference types, this will be null.
     */
    private final Integer nullCheckOpcode;
    /**
     * For ops which are valid for reference types, this is the opcode for comparing equality between two references.
     * For ops which do not work for reference types, this will be null.
     */
    private final Integer referenceOpcode;
}