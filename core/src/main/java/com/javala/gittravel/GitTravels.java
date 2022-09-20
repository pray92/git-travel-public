package com.javala.gittravel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;


/**
 * {@code GitTravels}는 <em>JGit</em> 라이브러리를 활용한 <em>git-travel</em> 핵심 유틸 클래스입니다.
 * 
 * <p>
 * <em>JGit</em>에서 모든 커밋 해시들의 정보를 가져올 수 있습니다. 그렇기 때문에 로그 기록대신에 
 * 자체 기능을 활용해 현 브랜치의 커밋 해시 목록을 가져와 이를 기반한 기능을 제공합니다.
 */
public class GitTravels {

    /**
     * <em>HEAD</em>를 기준으로 최대 5개의 목록을 콘솔에 표시합니다.
     * 
     * <p>
     * 가장 끝에 있는 커밋 해시에 checkout된 경우, 이전 커밋의 목록을 2개 표시하고,
     * 가장 초기 커밋 해시에 checkout된 경우, 이후 커밋의 목록을 2개 표시합니다.
     * 
     * <p>
     * 그 밖엔 checkout된 커밋 해시를 기준으로 이전과 이후 커밋을 2개씩 표시합니다.
     * 
     * @throws IOException .git 폴더를 찾을 수 없을 때
     * @throws GitAPIException JGit에서 에러가 발생할 때
     */
    public static void here() throws IOException, GitAPIException {
        try (Git git = git()) {
            final String headCommitHash = getHeadCommitHash(git).get();
            final List<RevCommit> logs = getAllLogs(git);

            final int currentHeadIndex = getCurrentHeadIndex(headCommitHash, logs);
            final int start = Math.max(currentHeadIndex - 2, 0);
            final int end = Math.min(currentHeadIndex + 2, logs.size() - 1);
            for(int logIndex = start; logIndex <= end; ++logIndex){
                RevCommit log = logs.get(logIndex);
                if(headCommitHash.equals(log.name()))
                    System.out.println(
                        ConsoleColors.RED + getGitLogMessage(log).get() + ConsoleColors.RESET
                    );    
                else
                    System.out.println(getGitLogMessage(log).get());
            }
            
        } catch(IOException | GitAPIException ex) {
            throw ex;
        }
    }

    /**
     * 현재 <em>HEAD</em>를 기준으로 횟수만큼 위치의 커밋해시로 checkout합니다.
     * 
     * <p> 
     * 커밋 해시 목록들은 가장 최근 등록된 순으로 목록을 가져옵니다.
     * 그래서 인덱스가 낮을 수록 가장 최근 것입니다.
     * 
     * <p>
     * 매개변수가 양수일 경우, {현 커밋 인덱스 + step} 번째 이전 커밋 해시로 checkout하고
     * 매개변수가 음수라면 {현 커밋 인덱스 - step} 번째, 최근 커밋 해시로 checkout합니다.
     * 
     * @param step 이동할 커밋 인덱스
     * @throws IOException .git 폴더를 찾을 수 없을 때
     * @throws GitAPIException JGit에서 에러가 발생할 때
     */
    public static void travel(int step) throws IOException, GitAPIException {
        try (Git git = git()) {
            final String headCommitHash = getHeadCommitHash(git).get();
            final List<RevCommit> logs = getAllLogs(git);

            final int currentHeadIndex = getCurrentHeadIndex(headCommitHash, logs);
            final int travelIndex = getIndexFromOffset(0, logs.size() - 1, currentHeadIndex, step);
            
            git.checkout().setName(logs.get(travelIndex).name()).call();
            System.out.println("Travel to : " + getGitLogMessage(logs.get(travelIndex)).get());
        } catch(IOException | GitAPIException ex) {
            throw ex;
        }
    }

    /**
     * 해당 커밋 해시로 checkout합니다.
     * 
     * <p>
     * 특정 키워드가 존재하며, 이를 통해 커밋 해시의 가장 최신과, 가장 초기의 revision으로 checkout합니다.
     * 
     * <p>
     * Keyword)
     * start, begin : 가장 초기 revision으로 checkout
     * last, end : 가장 최근 revision으로 checkout
     * 
     * @param moveDestination 커밋 해시 또는 관련 키워드
     * @throws IOException .git 폴더를 찾을 수 없을 때
     * @throws GitAPIException JGit에서 에러가 발생할 때
     */
    public static void move(String moveDestination) throws IOException, GitAPIException  {
        try (Git git = git()) {
            final List<RevCommit> logs = getAllLogs(git);
            switch(moveDestination) {
                case "start":
                case "begin":
                    git.checkout().setName(logs.get(logs.size() - 1).name()).call();
                    System.out.println("Move to : " + getGitLogMessage(logs.get(logs.size() - 1)).get());
                    break;
                case "end":
                case "last":
                    git.checkout().setName(logs.get(0).name()).call();
                    System.out.println("Move to : " + getGitLogMessage(logs.get(0)).get());
                    break;
                default:
                    git.checkout().setName(moveDestination).call();
            }
        } catch(IOException | GitAPIException ex) {
            throw ex;
        }
    }

    /**
     * 프로세스 실행 위치를 기준으로 .git의 정보를 초기화한 {@link Git} 변수를 반환합니다.
     * 
     * @return .git의 정보를 초기화한 {@link Git} 변수
     * @throws IOException .git 폴더를 찾을 수 없을 때
     * @throws GitAPIException JGit에서 에러가 발생할 때
     */
    public static final Git git() throws IOException, GitAPIException {
        return new Git(new FileRepository(getDotGitDir()));
    }

    /**
     * <em>.git</em> 경로 파일 변수를 반환합니다.
     * 
     * @return <em>.git</em> 경로 파일 변수
     * @throws IOException 해당 경로를 못 찾을 때
     */
    public static final File getDotGitDir() throws IOException {
        return new File(getGitRootDir(), ".git");
    }

    static final String getProjectName() throws IOException {
        final File gitRootDir = getGitRootDir();
        return gitRootDir.getName();
    }

    /**
     * git-travel 실행 경로를 기준으로 git 설정 경로, <em>.git</em> 파일의 <em>위치</em>를 찾습니다.
     * 
     * <p>
     * 경로를 찾을 때까지 상위 경로로 타고 올라가 찾습니다.
     * 
     * @return <em>.git</em> 절대 경로
     * @throws IOException
     */
    private static final File getGitRootDir() throws IOException {
        File directory = new File(".").getCanonicalFile();
        while (false == new File(directory, ".git").exists()) {
            directory = directory.getParentFile();
            if (null == directory) {
                throw new IOException("Project is not a git directory.");
            }
        }
        return directory;
    }

    /**
     * 
     * @param git <em>.git</em> 정보 변수
     * @return 현재 <em>HEAD</em> 커밋 해시
     * @throws IOException 현재 브랜치의 <em>HEAD</em>를 가져오지 못했을 때
     */
    public static final Optional<String> getHeadCommitHash(Git git) throws IOException {
        Ref head = git.getRepository().exactRef(Constants.HEAD);
		if (null == head) {
			return Optional.empty();
		}

		if (head.isSymbolic()) {
			return Optional.of(head.getTarget().getObjectId().name());
		}

		ObjectId objectId = head.getObjectId();
		if (objectId != null) {
			return Optional.of(objectId.name());
		}
        return Optional.empty();
    }

    /** Checkout 브랜치의 모든 log 목록을 가져옵니다. */
    private static List<RevCommit> getAllLogs(Git git) throws IOException, GitAPIException {
        Iterable<RevCommit> iterators = git.log().all().call();
        List<RevCommit> logs = new ArrayList<RevCommit>();
        iterators.forEach(logs::add);
        return logs;
    }

    /** 해당 로그의 메시지를 반환합니다. */
    private static Optional<String> getGitLogMessage(RevCommit log) {
        Optional<String> ret = Optional.empty();
        ret = Optional.of(Strings.lenientFormat(
            "%s %s", 
            log.name().substring(0, 6), 
            log.getShortMessage()
        ));
        return ret;
    }

    /** 현재 <em>HEAD</em>의 인덱스를 반환합니다. */
    @VisibleForTesting
    static final int getCurrentHeadIndex(
        final String headCommitHash, 
        final List<RevCommit> logs
    ) {
        for(int nHeadIndex = 0; nHeadIndex < logs.size(); ++nHeadIndex) {
            RevCommit log = logs.get(nHeadIndex);
            if(headCommitHash.equals(log.name())){
                return nHeadIndex;
            }
        }
        return -1;
    }
    
    /** (min <= {offset + step} <= max) 값을 반환합니다.  */
    @VisibleForTesting
    static final int getIndexFromOffset(
        final int min, 
        final int max, 
        final int offset, 
        final int step
    ) {
        return (0 < step) 
                    ? Math.min(offset + step, max) 
                    : Math.max(offset + step, min);
    }

}
