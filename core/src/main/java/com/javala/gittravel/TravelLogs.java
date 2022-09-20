package com.javala.gittravel;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

// 코드 관리자를 위한 코멘트:
// 초기화된 branch 상태에 대해 Valid와 Invalid 상태가 존재한다.
// 다음 조건들을 만족했을 때 branch 상태가 Valid하다고 한다.
// - branch/commits 파일이 존재하며 빈 파일이 아니다.
// - branch/head 파일이 존재하며 branch/commits 파일 안에 있는 값 하나와 대응한다.
//
// TravelLogs의 public 함수를 호출하기 전과 후, branch가 Initialzied되어있다면 상태는
// 항상 Valid하게 유지해야 한다.
// TODO: Handle error for corrupt data files.
/**
 * {@code TravelLogs}는 한 프로젝트에 대해 깃 로그 기록을 담당하는 클래스이다.
 * 
 * <p>
 * TravelLogs는 프로젝트의 여러 branch의 로그들을 기록하며 각 branch마다 <em>head</em> 또한 기록하는데
 * 이때 head는 해당 branch에 대해 어느 위치를 마지막으로 보았는지를 의미한다.
 * 
 * <p>
 * 여러 branch를 유지하기 때문에 현재 어느 branch를 보고 있는지 기록해야 하는데 이를 <em>current
 * branch</em>라고 한다.
 * 
 * <p>
 * TravelLogs는 특정 branch에 대해 <em>초기화</em> 상태를 정의한다.
 * 초기화 상태란 TravelLogs가 branch에 대해 commits와 head를 최소 한 번 기록한 상태를 말한다.
 * 많은 메소드들이 호출 전제 조건으로 branch가 초기화된 상태임을 요구한다. 자세한 내용은 각 메소드의 문서를 확인한다.
 */
public class TravelLogs {
    private final Path dataDir;
    private final Path currentBranchFile;
    private final String projectName;

    @VisibleForTesting
    TravelLogs(String systemDataDirPath, String projectName) {
        this(systemDataDirPath, projectName, FileSystems.getDefault());
    }

    @VisibleForTesting
    TravelLogs(String systemDataDirPath, String projectName, FileSystem fileSystem) {
        Preconditions.checkArgument(!systemDataDirPath.isBlank(), "systemDataDirPath cannot be blank.");
        Preconditions.checkArgument(!projectName.isBlank(), "projectName cannot be blank.");
        Preconditions.checkNotNull(fileSystem, "fileSystem cannot be null.");

        dataDir = fileSystem.getPath(systemDataDirPath, GIT_TRAVEL_DATA_DIRNAME, projectName);
        currentBranchFile = dataDir.resolve(CURRENT_BRANCH_FILENAME);
        this.projectName = projectName;
    }

    /**
     * {@code projectName} 프로젝트의 로그를 기록하는 TravelLogs 객체를 생성해 리턴한다.
     * 
     * 로그가 기록되는 디렉토리는 OS마다 다르다. Windows의 경우 %LOCALAPPDATA%\git-travel-data에 기록되며
     * Mac과 리눅스의 경우 $HOME/.data/git-travel-data에 기록된다.
     * 
     * @param projectName 로그를 기록할 프로젝트 이름, {@link String#isBlank() blank}이면
     *                    안된다.
     * @return {@code projectName} 프로젝트의 로그를 기록하는 TravelLogs 객체
     * @throws TravelLogsException 시스템이 OS를 판별할 수 없을 때
     */
    public static TravelLogs create(String projectName) throws TravelLogsException {
        final FileSystem fileSystem = FileSystems.getDefault();
        String systemDataDirPath = getDefaultSystemDatadir(fileSystem).toString();
        return new TravelLogs(systemDataDirPath, projectName);
    }

    private static final String GIT_TRAVEL_DATA_DIRNAME = "git-travel-data";
    private static final String COMMIT_FILENAME = "commits";
    private static final String HEAD_FILENAME = "head";
    private static final String CURRENT_BRANCH_FILENAME = ".current-branch";

    @VisibleForTesting
    static Path getDefaultSystemDatadir(FileSystem fileSystem) throws TravelLogsException {
        final String os = System.getProperty("os.name").toLowerCase();

        // Windows.
        if (os.indexOf("win") >= 0) {
            return fileSystem.getPath(System.getenv("LOCALAPPDATA"));
        }

        // Mac or Linux.
        if (os.indexOf("mac") >= 0 || os.indexOf("linux") >= 0) {
            return fileSystem.getPath(System.getenv("HOME"), ".data");
        }

        throw new TravelLogsException("Cannot determine OS.");
    }

    /**
     * 주어진 {@code branch} 가 <em>초기화</em>된 상태이면 true, 아니면 false를 리턴한다.
     * 
     * @param branch 초기화된 상태인지 확인할 branch, {@link String#isBlank() blank}이면 안된다.
     * @return 주어진 {@code branch} 가 초기화된 상태이면 true, 아니면 false
     */
    public boolean isInitialized(String branch) {
        Preconditions.checkArgument(!branch.isBlank(), "branch cannot be blank.");
        return Files.isDirectory(dataDir.resolve(branch));
    }

    /**
     * <em>current branch</em> 기록이 존재하면 true, 아니면 false를 리턴한다.
     * 
     * @return current branch 기록이 존재하면 true, 아니면 false
     */
    public boolean existsCurrentBranch() {
        return Files.isRegularFile(currentBranchFile);
    }

    /**
     * 주어진 {@code commit}이 {@code branch} 로그 안에 존재하면 true, 아니면 false를 리턴한다.
     * 
     * @param branch 로그를 확인할 branch, <em>초기화</em> 상태여야 한다
     * @param commit {@code branch} 로그에 존재하는지 확인할 commit
     * @return 주어진 {@code commit}이 {@code branch} 로그 안에 존재하면 true, 아니면 false
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public boolean isValidCommit(String branch, String commit) throws IOException {
        Preconditions.checkState(isInitialized(branch), constructNotInitalizedMessage(branch));
        return readCommits(branch).contains(commit);
    }

    /**
     * 주어진 {@code branch} 로그에 {@code commits}를 기록한다.
     * 
     * 기존에 {@code branch}에 기록한 로그가 없다면 자동으로 <em>초기화</em>가 되며 head는 {@code commits}의
     * 첫번째 원소로 설정된다.
     * 
     * 이미 {@code branch}에 기록한 로그가 존재하면 로그를 {@code commits}로 업데이트하고 head는 그대로 유지된다.
     * 단, 기존 head가 새로 업데이트되는 {@code commits}안에 존재하지 않을 시에는 {@code commits}의 첫번째
     * 원소로 재설정된다.
     * 
     * @param branch  {@code commits}를 기록할 branch
     * @param commits {@code branch}에 기록할 commits, {@link List#isEmpty() empty}이면
     *                안된다
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public void writeCommits(String branch, List<String> commits) throws IOException {
        Preconditions.checkArgument(!commits.isEmpty(), "commits cannot be empty.");

        Path commitsFile = dataDir.resolve(branch).resolve(COMMIT_FILENAME);
        Path headFile = dataDir.resolve(branch).resolve(HEAD_FILENAME);

        if (!isInitialized(branch)) {
            if (!existsCurrentBranch()) {
                writeCurrentBranch(branch);
            }
            Files.createDirectories(commitsFile.getParent());
            Files.write(commitsFile, commits);

            Files.createDirectories(headFile.getParent());
            writeHeadInternal(branch, commits.get(0));
        } else {
            Files.write(commitsFile, commits);
            String head = readHead(branch);
            if (!commits.contains(head)) {
                writeHeadInternal(branch, commits.get(0));
            }
        }
    }

    /**
     * 주어진 {@code branch}의 로그를 리턴한다.
     * 
     * @param branch 로그를 읽을 branch, {@code branch}는 <em>초기화</em>된 상태여야 한다
     * @return 주어진 {@code branch}의 로그
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public List<String> readCommits(String branch) throws IOException {
        Preconditions.checkState(isInitialized(branch), constructNotInitalizedMessage(branch));
        Path commitsFile = dataDir.resolve(branch).resolve(COMMIT_FILENAME);
        return Files.readAllLines(commitsFile);
    }

    /**
     * 주어진 {@code branch}의 <em>head</em>를 리턴한다.
     * 
     * @param branch head를 읽을 branch, {@code branch}는 <em>초기화</em>된 상태여야 한다.
     * @return 주어진 {@code branch}의 head
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public String readHead(String branch) throws IOException {
        Preconditions.checkState(isInitialized(branch), constructNotInitalizedMessage(branch));
        Path headFile = dataDir.resolve(branch).resolve(HEAD_FILENAME);
        return Files.readAllLines(headFile).get(0);
    }

    /**
     * <em>current branch</em>에 기록된 로그의 시작 커밋으로 <em>head</em>를 설정한다.
     * 
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public void writeHeadToStart() throws IOException {
        String branch = readCurrentBranch();
        List<String> commits = readCommits(branch);
        writeHeadInternal(branch, commits.get(commits.size() - 1));
    };

    /**
     * <em>current branch</em>에 기록된 로그의 마지막 커밋으로 <em>head</em>를 설정한다.
     * 
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public void writeHeadToEnd() throws IOException {
        String branch = readCurrentBranch();
        List<String> commits = readCommits(branch);
        writeHeadInternal(branch, commits.get(0));
    };

    /**
     * 주어진 {@code commit}으로 <em>head</em>를 설정한다.
     * 
     * @param commit head로 설정할 commit
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public void writeHeadToCommit(String commit) throws IOException {
        String branch = readCurrentBranch();
        Preconditions.checkArgument(isValidCommit(branch, commit), "Commit %s doesn't exist in branch log.", commit);
        writeHeadInternal(branch, commit);
    }

    /**
     * 현재 <em>head</em> 인덱스를 기준으로 해당 횟수 만큼 이동한다.
     *
     * @param count 이동할 횟수 
     * @throws IOException 내부적으로 {@code IOException} 이 발생했을 때
     */
    public void writeHeadToCount(int count) throws IOException {
        Preconditions.checkArgument(0 < count, "Travel count parameter should be bigger than 0 : %d", count);

        String branch = readCurrentBranch();
        String headCommit = readHead(branch);
        Preconditions.checkArgument(isValidCommit(branch, headCommit), "Commit %s doesn't exist in branch log.", headCommit);
        
        List<String> commits = readCommits(branch);
        int headIndex = commits.indexOf(headCommit);
        writeHeadInternal(branch, commits.get(Math.max(0, headIndex - count)));
    }

    /**
     * 현재 <em>head</em> 인덱스를 기준으로 해당 횟수 만큼 <em>뒤로</em> 이동한다.
     *
     * @param count 뒤로 이동할 횟수 
     * @throws IOException 내부적으로 {@code IOException} 이 발생했을 때
     */
    public void writeHeadBackToCount(int count) throws IOException {
        Preconditions.checkArgument(0 < count, "Travel count parameter should be bigger than 0 : %d", count);

        String branch = readCurrentBranch();
        String headCommit = readHead(branch);
        Preconditions.checkArgument(isValidCommit(branch, headCommit), "Commit %s doesn't exist in branch log.", headCommit);
        
        List<String> commits = readCommits(branch);
        int headIndex = commits.indexOf(headCommit);
        writeHeadInternal(branch, commits.get(Math.min(commits.size() - 1, headIndex + count)));
    }

    private void writeHeadInternal(String branch, String commit) throws IOException {
        Path headFile = dataDir.resolve(branch).resolve(HEAD_FILENAME);
        if (!Files.isRegularFile(headFile)) {
            Files.createDirectories(headFile.getParent());
        }
        Files.writeString(headFile, commit + System.lineSeparator());
    }

    /**
     * <em>current branch</em>에 기록된 branch를 리턴한다.
     * 
     * @return current branch에 기록된 branch, current branch에 기록된 branch가 존재해야 한다
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public String readCurrentBranch() throws IOException {
        Preconditions.checkState(existsCurrentBranch(), "No branches are initalized for project %s.", projectName);
        return Files.readAllLines(currentBranchFile).get(0);
    }

    /**
     * <em>current branch</em>를 주어진 {@code branch}로 설정한다.
     * 
     * @param branch current branch로 설정할 branch, 주어진 {@code branch}는 초기화된 상태여야 한다
     * @throws IOException 내부적으로 {@code IOException}이 발생했을 때
     */
    public void switchCurrentBranch(String branch) throws IOException {
        Preconditions.checkState(isInitialized(branch), constructNotInitalizedMessage(branch));
        writeCurrentBranch(branch);
    }

    private void writeCurrentBranch(String branch) throws IOException {
        if (!Files.isRegularFile(currentBranchFile)) {
            Files.createDirectories(currentBranchFile.getParent());
        }
        Files.writeString(currentBranchFile, branch + System.lineSeparator());
    }

    private String constructNotInitalizedMessage(String branch) {
        return String.format("%s branch is not initalized.", branch);
    }
}
