package liuyuyang.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Intentional B.5 fixture. This branch must never be merged.
 */
class B5RequiredCheckFailureTest {

    @Test
    void intentionallyFailsToVerifyBranchProtection() {
        assertEquals(1, 2, "B.5 fixture: required Server check must fail");
    }
}
