package com.javala.gittravel;

import com.google.common.collect.ImmutableList;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** {@link CommandLineOptionsParser} Test */
@RunWith(JUnit4.class)
public class CommandLineOptionsParserTest {
    @Test
    public void defaults() {
        CommandLineOptions options = CommandLineOptionsParser.parse(ImmutableList.of());
        assertEquals(options.help(), false);
        assertEquals(options.commandType(), CommandType.NONE);
        assertEquals(options.branchName(), Optional.empty());
        assertEquals(options.travelBack(), false);
        assertEquals(options.travelCount(), 1);
        assertEquals(options.moveDestination(), Optional.empty());
    }

    @Test
    public void help() {
        assertEquals(CommandLineOptionsParser.parse(Arrays.asList("--help")).help(), true);
        assertEquals(CommandLineOptionsParser.parse(Arrays.asList("-help")).help(), true);
        assertEquals(CommandLineOptionsParser.parse(Arrays.asList("-h")).help(), true);
    }

    @Test
    public void init() {
        assertEquals(CommandType.INIT, CommandLineOptionsParser.parse(Arrays.asList("init")).commandType());
        assertEquals(CommandType.INIT, CommandLineOptionsParser.parse(Arrays.asList("Init")).commandType());
    }

    @Test
    public void move() {
        assertEquals(CommandType.MOVE, CommandLineOptionsParser.parse(Arrays.asList("move")).commandType());
        assertEquals(CommandType.MOVE, CommandLineOptionsParser.parse(Arrays.asList("Move")).commandType());
    }

    @Test
    public void here() {
        assertEquals(CommandType.HERE, CommandLineOptionsParser.parse(Arrays.asList("HERE")).commandType());
        assertEquals(CommandType.HERE, CommandLineOptionsParser.parse(Arrays.asList("here")).commandType());
    }

    @Test
    public void travel() {
        assertEquals(CommandType.TRAVEL, CommandLineOptionsParser.parse(Arrays.asList("travel")).commandType());
        assertEquals(CommandType.TRAVEL, CommandLineOptionsParser.parse(Arrays.asList("Travel")).commandType());
    }

    @Test
    public void setBranchNameInitOnly() {
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("init")).branchName());
        assertEquals(Optional.of("main"), CommandLineOptionsParser.parse(Arrays.asList("init", "main")).branchName());
        assertEquals(Optional.of("master"), CommandLineOptionsParser.parse(Arrays.asList("init", "master")).branchName());
        assertEquals(Optional.of("branchName"), CommandLineOptionsParser.parse(Arrays.asList("init", "branchName")).branchName());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("here")).branchName());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("move", "main")).branchName());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("move", "master")).branchName());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("travel", "1")).branchName());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("travel", "-b", "1")).branchName());
    }

    @Test
    public void travelBack() {
        assertEquals(true, CommandLineOptionsParser.parse(Arrays.asList("travel", "--back")).travelBack());
        assertEquals(true, CommandLineOptionsParser.parse(Arrays.asList("travel", "-back")).travelBack());
        assertEquals(true, CommandLineOptionsParser.parse(Arrays.asList("travel", "-b")).travelBack());
        assertEquals(false, CommandLineOptionsParser.parse(Arrays.asList("travel")).travelBack());
        assertEquals(false, CommandLineOptionsParser.parse(Arrays.asList("travel", "3")).travelBack());
        assertEquals(false, CommandLineOptionsParser.parse(Arrays.asList("travel", "10")).travelBack());
        assertEquals(true, CommandLineOptionsParser.parse(Arrays.asList("travel", "-b", "3")).travelBack());
        assertEquals(true, CommandLineOptionsParser.parse(Arrays.asList("travel", "-b", "10")).travelBack());
    }

    @Test
    public void travelCount() {
        assertEquals(1, CommandLineOptionsParser.parse(Arrays.asList("travel", "--back")).travelCount());
        assertEquals(1, CommandLineOptionsParser.parse(Arrays.asList("travel", "-back")).travelCount());
        assertEquals(1, CommandLineOptionsParser.parse(Arrays.asList("travel", "-b")).travelCount());
        assertEquals(3, CommandLineOptionsParser.parse(Arrays.asList("travel", "3")).travelCount());
        assertEquals(10, CommandLineOptionsParser.parse(Arrays.asList("travel", "10")).travelCount());
        assertEquals(3, CommandLineOptionsParser.parse(Arrays.asList("travel", "-b", "3")).travelCount());
        assertEquals(12, CommandLineOptionsParser.parse(Arrays.asList("travel", "-b", "12")).travelCount());
    }

    @Test
    public void setCommitHashInMoveOnly() {
        assertEquals(Optional.of("start"), CommandLineOptionsParser.parse(Arrays.asList("move", "start")).moveDestination());
        assertEquals(Optional.of("end"), CommandLineOptionsParser.parse(Arrays.asList("move", "end")).moveDestination());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("move")).moveDestination());

        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("init", "master")).moveDestination());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("travel", "1")).moveDestination());
        assertEquals(Optional.empty(), CommandLineOptionsParser.parse(Arrays.asList("travel", "-b", "1")).moveDestination());
    }
}
