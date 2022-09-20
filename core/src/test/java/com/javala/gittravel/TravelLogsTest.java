package com.javala.gittravel;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.List;

public class TravelLogsTest {
    @Test
    public void initializeBranch() {
        FileSystem memoryFileSystem = Jimfs.newFileSystem(Configuration.unix());

        TravelLogs travelLogs = new TravelLogs("/data", "project", memoryFileSystem);
        List<String> commits = Arrays.asList("commitA", "commitB", "commitC");
        assertEquals(false, travelLogs.isInitialized("branch-A"));
        try {
            travelLogs.writeCommits("branch-A", commits);
        } catch (IOException exception) {
            fail("Test failed due to " + exception);
        }
        assertEquals(true, travelLogs.isInitialized("branch-A"));
    }

    @Test
    public void writeReadCommits() {
        FileSystem memoryFileSystem = Jimfs.newFileSystem(Configuration.unix());
        TravelLogs travelLogs = new TravelLogs("/data", "project", memoryFileSystem);
        List<String> commits = Arrays.asList("commitA", "commitB", "commitC", "commitD", "commitE");
        try {
            travelLogs.writeCommits("branch-A", commits);
            List<String> readCommits = travelLogs.readCommits("branch-A");
            assertArrayEquals(commits.toArray(), readCommits.toArray());

            String head = travelLogs.readHead("branch-A");
            assertEquals(commits.get(0), head);

            travelLogs.writeHeadToStart();
            head = travelLogs.readHead("branch-A");
            assertEquals(commits.get(commits.size() - 1), head);

            travelLogs.writeHeadToEnd();
            head = travelLogs.readHead("branch-A");
            assertEquals(commits.get(0), head);

            travelLogs.writeHeadToCommit("commitB");
            head = travelLogs.readHead("branch-A");
            assertEquals("commitB", head);

            travelLogs.writeHeadToCommit("commitC");
            travelLogs.writeHeadToCount(2);
            head = travelLogs.readHead("branch-A");
            assertEquals("commitA", head);

            travelLogs.writeHeadToCommit("commitC");
            travelLogs.writeHeadBackToCount(2);
            head = travelLogs.readHead("branch-A");
            assertEquals("commitE", head);

            // Over-count travel
            travelLogs.writeHeadToCommit("commitC");
            travelLogs.writeHeadToCount(31);
            head = travelLogs.readHead("branch-A");
            assertEquals("commitA", head);

            travelLogs.writeHeadToCommit("commitC");
            travelLogs.writeHeadBackToCount(31);
            head = travelLogs.readHead("branch-A");
            assertEquals("commitE", head);
        } catch (IOException exception) {
            fail("Test failed due to " + exception);
        }
    }

    @Test
    public void writeReadBranch() {
        FileSystem memoryFileSystem = Jimfs.newFileSystem(Configuration.unix());
        TravelLogs travelLogs = new TravelLogs("/data", "project", memoryFileSystem);
        List<String> branchACommits = Arrays.asList("commitA", "commitB", "commitC");
        List<String> branchBCommits = Arrays.asList("commitD", "commitE", "commitF");
        try {
            travelLogs.writeCommits("branch-A", branchACommits);
            travelLogs.writeCommits("branch-B", branchBCommits);
            String currentBranch = travelLogs.readCurrentBranch();
            assertEquals("branch-A", currentBranch);
            travelLogs.switchCurrentBranch("branch-B");
            currentBranch = travelLogs.readCurrentBranch();
            assertEquals("branch-B", currentBranch);
        } catch (IOException exception) {
            fail("Test failed due to " + exception);
        }
    }
}
