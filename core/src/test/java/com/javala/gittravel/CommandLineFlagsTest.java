package com.javala.gittravel;

import static org.junit.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for command-line flags
 */
@RunWith(JUnit4.class)
public class CommandLineFlagsTest {
    @Test
    public void invalidCommands() throws UsageException {
        try {
            Main.processArgs();
            fail();
        } catch (UsageException e) {
            // expected
        }

        try {
            Main.processArgs("invalid command");
            fail();
        } catch (UsageException e) {
            // expected
        }

        try {
            Main.processArgs("-i");
            fail();
        } catch (UsageException e) {
            // expected
        }
    }

    @Test
    public void initCommand() throws UsageException {
        Main.processArgs("init");
        Main.processArgs("init", "main");
        Main.processArgs("init", "master");

        try {
            Main.processArgs("init", "main", "master");
            fail();
        } catch (UsageException e) {
            assertThat("", e.getMessage().contains("init"));
        }
    }

    @Test
    public void travelCommand() throws UsageException {
        Main.processArgs("travel");
        Main.processArgs("travel", "12");
        Main.processArgs("travel", "-b", "1");

        try {
            Main.processArgs("travel", "asdf");
            fail();
        } catch(UsageException e) {
            // expected
        }

        try {
            Main.processArgs("travel", "-b", "a");
            fail();
        } catch(UsageException e) {
            // expected
        }

        try {
            Main.processArgs("travel", "-b", "123", "asdf");
            fail();
        } catch(UsageException e) {
            // expected
        }

        try {
            Main.processArgs("travel", "123", "455");
            fail();
        } catch(UsageException e) {
            assertThat("", e.getMessage().contains("travel"));
        }
    }
    
    @Test
    public void travelBackInTravelOnly() throws UsageException {
        Main.processArgs("travel", "-b");
        try {
            Main.processArgs("move", "-b");
            fail();
        } catch(UsageException e){
            // expected
        }

        try {
            Main.processArgs("here", "-b");
            fail();
        } catch(UsageException e){
            // expected
        }

        try {
            Main.processArgs("init", "-b");
            fail();
        } catch(UsageException e){
            // expected
        }
    }

    @Test
    public void moveCommand() throws UsageException {
        Main.processArgs("move", "start");
        Main.processArgs("move", "last");
        Main.processArgs("move", "end");
        Main.processArgs("move", "commitHash");

        try {
            Main.processArgs("move");
            fail();
        } catch (UsageException e) {
            assertThat("", e.getMessage().contains("move"));
        }

        try {
            Main.processArgs("move", "start", "last");
            fail();
        } catch (UsageException e) {
            assertThat("", e.getMessage().contains("move"));
        }
    }

    @Test
    public void hereCommand() throws UsageException {
        Main.processArgs("here");
        try {
            Main.processArgs("here", "1");
            fail();
        } catch (UsageException e) {
            assertThat("", e.getMessage().contains("here"));
        }
        try {
            Main.processArgs("here", "1", "asdf");
            fail();
        } catch (UsageException e) {
            assertThat("", e.getMessage().contains("here"));
        }
    }
    
}
