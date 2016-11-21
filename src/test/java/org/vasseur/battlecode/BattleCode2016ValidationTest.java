package org.vasseur.battlecode;

import org.soneira.score.junit.annotations.Persist;
import org.soneira.score.junit.ScoreBlockJUnit4ClassRunner;
import org.soneira.score.junit.annotations.InjectImpl;
import org.soneira.score.junit.annotations.Score;
import org.soneira.score.junit.persistence.Couchbase;
import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vasseur.battlecode.IBattleCode2016;

@RunWith(ScoreBlockJUnit4ClassRunner.class)
//@Persist(Couchbase.class)
public class BattleCode2016ValidationTest {


    @InjectImpl
    protected IBattleCode2016 battleCode2016;


    @Test
    @Score(10)
    public void checkSum_For2and2_then4() {
        // Setup
        int expected = 4;

        // Test
        int result = battleCode2016.sum(2,2);

        // Assertions
        Assertions.assertThat(result).isEqualTo(expected);
    }


    @Test
    @Score(50)
    public void checkSum_For2and2_then4_withMorePoints() {
        // Setup
        int expected = 4;

        // Test
        int result = battleCode2016.sum(2,2);

        // Assertions
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test(timeout = 1)
    @Score(value = 500, maxTimeOnly = true)
    public void checkSum_longTretment() throws InterruptedException {
        // Setup
        int expected = 9;

        for (int i = 0; i < 100000; i++) {
            // System.out.println("EO");
        }
        // Test
        int result = battleCode2016.sum(2,2);

        // Assertions
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test(timeout = 1000)
    @Score(value = 500, maxTimeOnly = true)
    public void checkSum_longTretmentOk_butWrongScore() throws InterruptedException {
        // Setup
        int expected = 9;

        // Test
        int result = battleCode2016.sum(2,2);

        // Assertions
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test(timeout = 1000)
    @Score(value = 500, maxTimeOnly = true)
    public void checkSum_longTretmentOk() throws InterruptedException {
        // Setup
        int expected = 4;

        // Test
        int result = battleCode2016.sum(2,2);

        // Assertions
        Assertions.assertThat(result).isEqualTo(expected);
    }
}
